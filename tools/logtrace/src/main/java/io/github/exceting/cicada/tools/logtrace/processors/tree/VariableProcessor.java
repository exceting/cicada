package io.github.exceting.cicada.tools.logtrace.processors.tree;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.annos.VarLog;
import io.github.exceting.cicada.tools.logtrace.context.LogTraceContext;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#VARIABLE}.
 * eg:
 * <pre>
 *     modifiers type name initializer ;
 *     modifiers type qualified-name.this
 * </pre>
 */
public class VariableProcessor extends TreeProcessor {

    static final String VAR_LOG = VarLog.class.getSimpleName();

    static final String VAR_LOG_DUR = "dur";

    public VariableProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    /**
     * Only process local variable.
     */
    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCVariableDecl)) {
            return;
        }
        JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) jcTree;
        if (jcVariableDecl.init != null) {
            getFactory().get(jcVariableDecl.init.getKind()).process(jcVariableDecl.init);
        }

        if (jcVariableDecl.getModifiers().getAnnotations() != null && jcVariableDecl.getModifiers().getAnnotations().size() > 0) {
            List<JCTree.JCAnnotation> hit = jcVariableDecl.getModifiers().getAnnotations()
                    .stream()
                    .filter(a -> VAR_LOG.equals(a.getAnnotationType().toString()))
                    .collect(Collectors.toList());
            if (hit.size() == 0) {
                return;
            }

            LogTraceContext.MethodConfig methodConfig = LogTraceContext.currentMethodConfig.get();
            // Get current block.
            LogTraceContext.MethodConfig.OriginCode originCode = methodConfig.getBlockStack().peek();
            if (originCode != null) {
                JCTree.JCAnnotation anno = hit.get(0); // Get 1st.
                boolean dur = false;
                if (anno.getArguments() != null) {
                    for (JCTree.JCExpression arg : anno.getArguments()) {
                        if (!(arg instanceof JCTree.JCAssign)) {
                            continue;
                        }
                        JCTree.JCAssign assign = (JCTree.JCAssign) arg;
                        if (VAR_LOG_DUR.equals(assign.lhs.toString())) {
                            dur = "true".equals(assign.rhs.toString());
                        }
                    }
                }
                originCode.getVars().put(jcVariableDecl.getName().toString(), new LogTraceContext.MethodConfig.VarConfig(dur));

                attachVarLog(jcVariableDecl.getName().toString(), dur, methodConfig,
                        originCode, jcVariableDecl, getTreeMaker(), getNames());
            }
        }
    }

    static void attachVarLog(String varName, boolean dur, LogTraceContext.MethodConfig methodConfig,
                             LogTraceContext.MethodConfig.OriginCode originCode,
                             JCTree jcTree, TreeMaker treeMaker, Names names) {
        Map<String, JCTree.JCExpression> newArgs = new LinkedHashMap<>();
        newArgs.put(varName, treeMaker.Ident(names.fromString(varName)));
        if (dur) {

            Name newParamName = names.fromString(getNewVarName("var_start_"));

            // Code: System.nanoTime()
            JCTree.JCMethodInvocation nanoTimeInvocation = treeMaker.Apply(null, treeMaker.Select(
                    treeMaker.Ident(names.fromString("System")),
                    names.fromString("nanoTime")), com.sun.tools.javac.util.List.nil());

            // Code: final long {a+UUID} = System.nanoTime()
            JCTree.JCVariableDecl jcStartTime = treeMaker.VarDef(treeMaker.Modifiers(Flags.FINAL, com.sun.tools.javac.util.List.nil()),
                    newParamName,
                    treeMaker.TypeIdent(TypeTag.LONG),
                    nanoTimeInvocation);

            originCode.addNewCode(new LogTraceContext.MethodConfig.NewCode(originCode.getOffset(), // Current line.
                    jcStartTime));

            // Code: (System.nanoTime() - {a+UUID}) / 1000000L
            newArgs.put("duration", treeMaker.Binary(JCTree.Tag.DIV,
                    treeMaker.Parens(treeMaker.Binary(JCTree.Tag.MINUS,
                            nanoTimeInvocation, treeMaker.Ident(newParamName))),
                    treeMaker.Literal(1000000L)));
        }
        originCode.addNewCode(new LogTraceContext.MethodConfig.NewCode(originCode.getOffset() + 1, // Next line.
                methodConfig.getLogContent()
                        .getNewCodeStatement(Tree.Kind.VARIABLE, jcTree,
                                "", newArgs, treeMaker, names)));
    }
}
