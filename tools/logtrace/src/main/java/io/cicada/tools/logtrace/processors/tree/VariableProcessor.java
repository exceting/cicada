package io.cicada.tools.logtrace.processors.tree;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.annos.VarLog;
import io.cicada.tools.logtrace.context.Context;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

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
     * Only process local variable, and the init value form conditional or method invoke.
     * eg:
     * <pre>
     *     int a = conditional ? value1 : value2;
     *     int b = getB();
     *     int c = 1; // Not process!
     * </pre>
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
            boolean needProcess = false;
            List<JCTree.JCAnnotation> hit = jcVariableDecl.getModifiers().getAnnotations()
                    .stream()
                    .filter(a -> {
                        // FIXME The fully qualified name of the VarLog annotation should be used for comparison here,
                        //  but it is not possible to retrieve its fully qualified name at this point.
                        return VAR_LOG.equals(a.getAnnotationType().toString());
                    })
                    .collect(Collectors.toList());
            if (hit.size() == 0) {
                return;
            }

            // Get current block.
            Context.MethodConfig.OriginCode originCode = Context.currentMethodConfig.get().getBlockStack().peek();
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

                Context.MethodConfig methodConfig = Context.currentMethodConfig.get();
                Map<String, JCTree.JCExpression> newArgs = new LinkedHashMap<>();
                newArgs.put(jcVariableDecl.getName().toString(), getTreeMaker().Ident(jcVariableDecl.getName()));
                if (dur) {

                    Name newParamName = getNames().fromString(String.format("b%s", UUID.randomUUID()).replace("-", "_"));

                    // Code: System.nanoTime()
                    JCTree.JCMethodInvocation nanoTimeInvocation = getTreeMaker().Apply(null, getTreeMaker().Select(
                            getTreeMaker().Ident(getNames().fromString("System")),
                            getNames().fromString("nanoTime")), com.sun.tools.javac.util.List.nil());

                    // Code: final long {a+UUID} = System.nanoTime()
                    JCTree.JCVariableDecl jcStartTime = getTreeMaker().VarDef(getTreeMaker().Modifiers(Flags.FINAL, com.sun.tools.javac.util.List.nil()),
                            newParamName,
                            getTreeMaker().TypeIdent(TypeTag.LONG),
                            nanoTimeInvocation);

                    originCode.addNewCode(new Context.MethodConfig.NewCode(originCode.getOffset() - 1, // Prev line.
                            jcStartTime));

                    // Code: (System.nanoTime() - {a+UUID}) / 1000000L
                    newArgs.put("duration", getTreeMaker().Binary(JCTree.Tag.DIV,
                            getTreeMaker().Parens(getTreeMaker().Binary(JCTree.Tag.MINUS,
                                    nanoTimeInvocation, getTreeMaker().Ident(newParamName))),
                            getTreeMaker().Literal(1000000L)));
                }
                originCode.addNewCode(new Context.MethodConfig.NewCode(originCode.getOffset() + 1, // Next line.
                        methodConfig.getLogContent()
                                .getNewCodeStatement(Tree.Kind.VARIABLE, jcVariableDecl,
                                        "", newArgs, getTreeMaker(), getNames())));
            }
        }
    }
}
