package io.cicada.tools.logtrace.processors.custom;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Name;
import io.cicada.tools.logtrace.annos.Ban;
import io.cicada.tools.logtrace.annos.MethodLog;
import io.cicada.tools.logtrace.context.Context;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

import java.util.*;
import java.util.stream.Collectors;

public class MethodProcessor extends TreeProcessor {

    static final String METHOD_LOG = MethodLog.class.getName();

    static final String BAN = Ban.class.getName();

    static final String METHOD_LOG_EXCEPTION = "exceptionLog";

    static final String METHOD_LOG_DUR = "dur";

    static final String METHOD_LOG_ONLY_VAR = "onlyVar";

    static final String METHOD_LOG_LEVEL = "traceLevel";

    static final String METHOD_LOG_IS_OPEN = "isOpen";

    public MethodProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCMethodDecl)) {
            return;
        }
        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) jcTree;

        if (methodDecl.getBody() == null
                || methodDecl.getBody().getStatements() == null
                || methodDecl.getBody().getStatements().size() == 0) {
            return;
        }

        JCTree.JCAnnotation traceAnno = methodDecl.getModifiers().getAnnotations().stream()
                .filter(a -> METHOD_LOG.equals(a.getAnnotationType().type.toString()))
                .collect(Collectors.toList())
                .get(0);

        boolean exceptionLog = false;
        boolean dur = false;
        boolean onlyVar = false;
        String level = "Level.DEBUG";
        String isOpen = null;
        if (traceAnno.getArguments() != null && traceAnno.getArguments().size() > 0) {
            for (JCTree.JCExpression arg : traceAnno.getArguments()) {
                if (!(arg instanceof JCTree.JCAssign)) {
                    continue;
                }
                JCTree.JCAssign assign = (JCTree.JCAssign) arg;
                if (METHOD_LOG_EXCEPTION.equals(assign.lhs.toString())) {
                    exceptionLog = "true".equals(assign.rhs.toString());
                }
                if (METHOD_LOG_DUR.equals(assign.lhs.toString())) {
                    dur = "true".equals(assign.rhs.toString());
                }
                if (METHOD_LOG_ONLY_VAR.equals(assign.lhs.toString())) {
                    onlyVar = "true".equals(assign.rhs.toString());
                }
                if (METHOD_LOG_LEVEL.equals(assign.lhs.toString())) {
                    level = assign.rhs.toString();
                }
                if (METHOD_LOG_IS_OPEN.equals(assign.lhs.toString())) {
                    isOpen = assign.rhs.toString();
                }
            }
        }

        Set<String> allVarsInMethod = methodDecl.getBody().getStatements().stream()
                .filter(s -> s instanceof JCTree.JCVariableDecl)
                .map(s -> ((JCTree.JCVariableDecl) s).getName().toString()).collect(Collectors.toSet());

        Map<String, JCTree.JCExpression> finalParamsMap = new LinkedHashMap<>();
        java.util.List<JCTree.JCVariableDecl> finalVars = new ArrayList<>();
        // Generate new final params.
        if (methodDecl.getParameters() != null && methodDecl.getParameters().size() > 0) {
            List<JCTree.JCVariableDecl> originParams = List.from(methodDecl.getParameters().stream().filter(p -> {
                if (p.getModifiers() == null
                        || p.getModifiers().getAnnotations() == null
                        || p.getModifiers().getAnnotations().size() == 0) {
                    return true;
                }
                java.util.List<JCTree.JCAnnotation> banAnnos = p.getModifiers().getAnnotations()
                        .stream().filter(oaa -> BAN.equals(oaa.type.toString())).collect(Collectors.toList());
                return banAnnos.size() == 0;
            }).collect(Collectors.toList()));

            if (originParams.size() > 0) {
                originParams.forEach(op -> {
                    String newParamName = String.format("final_%s", op.getName().toString());
                    int num = 0;
                    while (allVarsInMethod.contains(newParamName)) { // Preventing naming conflicts.
                        newParamName = String.format("final_%s_%d", op.getName().toString(), num);
                        num++;
                    }
                    JCTree.JCExpression typeIdent;
                    String paramType = op.getType().toString();
                    if (paramType.contains("<")) {
                        typeIdent = getTreeMaker().Ident(getNames().fromString(paramType.substring(0, paramType.indexOf("<"))));
                    } else if (paramType.contains("[")) {
                        typeIdent = getTreeMaker().TypeArray(getTreeMaker().Ident(getNames().fromString(paramType.substring(0, paramType.indexOf("[")))));
                    } else {
                        typeIdent = getTreeMaker().Ident(getNames().fromString(paramType));
                    }

                    JCTree.JCVariableDecl finalParam = getTreeMaker().VarDef(
                            getTreeMaker().Modifiers(Flags.FINAL, com.sun.tools.javac.util.List.nil()),
                            getNames().fromString(newParamName),
                            typeIdent,
                            getTreeMaker().Ident(getNames().fromString(op.getName().toString()))
                    );
                    finalVars.add(finalParam);
                    finalParamsMap.put(op.getName().toString(), getTreeMaker().Ident(getNames().fromString(newParamName)));
                });
            }
        }

        Context.MethodConfig methodConfig = new Context.MethodConfig(
                methodDecl.getName().toString(),
                finalParamsMap, level, onlyVar, isOpen);
        methodConfig.getBlockStack().push(new Context.MethodConfig.OriginCode(methodDecl.getBody()));
        Context.currentMethodConfig.set(methodConfig);

        try {
            getFactory().get(methodDecl.getBody().getKind()).process(methodDecl.getBody());

            // Do we need method start log?
            /*methodDecl.getBody().accept(new JCTree.Visitor() {
                @Override
                public void visitBlock(JCTree.JCBlock that) {
                    that.stats = generateCode(that.getStatements(), new Context.MethodConfig.NewCode(startLogOffset,
                            methodConfig.getLogContent().getNewCodeStatement(Tree.Kind.METHOD, that,
                                    "Start!", null, getTreeMaker(), getNames())));
                }
            });*/

            // Generate try-block statement.
            JCTree.JCCatch jcCatch = null;
            if (exceptionLog) {

                Name e = getNames().fromString("e");
                JCTree.JCIdent eIdent = getTreeMaker().Ident(e);
                Map<String, JCTree.JCExpression> newArgs = new HashMap<>();
                newArgs.put(null, eIdent);
                jcCatch = getTreeMaker().Catch(getTreeMaker().VarDef(getTreeMaker().Modifiers(0), e,
                                getTreeMaker().Ident(getNames().fromString("Exception")), null),
                        getTreeMaker().Block(0L, List.of(methodConfig.getLogContent()
                                .getNewCodeStatement(Tree.Kind.TRY, methodDecl.getBody(),
                                        "Error!", newArgs, getTreeMaker(), getNames()), getTreeMaker().Throw(eIdent))));
            }

            JCTree.JCBlock jcFinally = null;
            JCTree.JCVariableDecl jcStartTime = null;
            if (dur) {
                Name newParamName = getNames().fromString(getNewVarName("start_"));

                // Code: System.nanoTime()
                JCTree.JCMethodInvocation nanoTimeInvocation = getTreeMaker().Apply(null, getTreeMaker().Select(
                        getTreeMaker().Ident(getNames().fromString("System")),
                        getNames().fromString("nanoTime")), List.nil());

                // Code: final long log_trace_start = System.nanoTime()
                jcStartTime = getTreeMaker().VarDef(getTreeMaker().Modifiers(Flags.FINAL, com.sun.tools.javac.util.List.nil()),
                        newParamName,
                        getTreeMaker().TypeIdent(TypeTag.LONG),
                        nanoTimeInvocation);

                // Code: (System.nanoTime() - log_trace_start) / 1000000L
                Map<String, JCTree.JCExpression> newParams = new LinkedHashMap<>();
                newParams.put("duration", getTreeMaker().Binary(JCTree.Tag.DIV,
                        getTreeMaker().Parens(getTreeMaker().Binary(JCTree.Tag.MINUS,
                                nanoTimeInvocation, getTreeMaker().Ident(newParamName))),
                        getTreeMaker().Literal(1000000L)));

                // Code: finally { trace_logger.debug("xxxx Finished! duration = 25") }
                jcFinally = getTreeMaker().Block(0L, List.of(methodConfig.getLogContent().getNewCodeStatement(
                        Tree.Kind.TRY, methodDecl.getBody(), "Finished!",
                        newParams, getTreeMaker(), getNames())));
            }

            final JCTree.JCCatch finalJcCatch = jcCatch;
            final JCTree.JCBlock finalJcFinally = jcFinally;
            final JCTree.JCVariableDecl finalStartTime = jcStartTime;
            methodDecl.getBody().accept(new JCTree.Visitor() {
                @Override
                public void visitBlock(JCTree.JCBlock that) {
                    that.stats = List.of(getTreeMaker().Try(getTreeMaker().Block(that.flags, that.stats),
                            finalJcCatch == null ? List.nil() : List.of(finalJcCatch), finalJcFinally));
                }
            });

            // Generate top variables.
            java.util.List<JCTree.JCVariableDecl> topVars = new ArrayList<>(finalVars);
            if (finalStartTime != null) {
                topVars.add(finalStartTime);
            }
            topVars.forEach(tv -> methodDecl.getBody().accept(new JCTree.Visitor() {
                @Override
                public void visitBlock(JCTree.JCBlock that) {
                    that.stats = generateCode(that.getStatements(), new Context.MethodConfig.NewCode(0, tv));
                }
            }));
        } finally {
            methodConfig.getBlockStack().pop();
        }
    }


}
