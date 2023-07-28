package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position;
import io.cicada.tools.logtrace.AnnoProcessor;
import com.sun.tools.javac.util.Name;

import java.util.LinkedList;
import java.util.Map;

public class MethodProcessor extends TreeProcessor {


    MethodProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCMethodDecl)) {
            return;
        }
        System.out.println("@#@#@##@######");
        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) jcTree;
        JCTree.JCBlock methodBody = methodDecl.body;

        AnnoProcessor.MethodConfig methodConfig = AnnoProcessor.currentMethodConfig.get();
        Position.LineMap lineMap = AnnoProcessor.lineMap.get();

        StringBuilder logParams = new StringBuilder();

        // Get args.
        List<JCTree.JCVariableDecl> params = methodDecl.getParameters();
        LinkedList<JCTree.JCExpression> logArgs = new LinkedList<>();
        if (params != null && params.size() > 0) {
            Map<String, JCTree.JCExpression> argMap = argMap(params);
            if (argMap != null && argMap.size() > 0) {
                logParams.append(" Params: ");
                argMap.forEach((k, v) -> {
                    logParams.append(k).append(" = ").append("{}, ");
                    logArgs.add(v);
                });
            }
        }

        String methodStart = String.format(PREFIX, methodConfig.getMethodName(), "METHOD_START",
                lineMap.getLineNumber(methodBody.getStartPosition())) + "Start!";
        logArgs.addFirst(treeMaker.Literal(methodStart + logParams));
        methodConfig.getBlockStack().push(methodBody);
        // Push the new code into stack.
        Name logObjName = names.fromString(AnnoProcessor.currentLogIdentName.get());
        Name slf4jMethodName = getSlf4jMethod(methodConfig.getTraceLevel());
        methodConfig.getAttachStack().push(treeMaker.Exec(treeMaker.Apply(List.nil(),
                treeMaker.Select(treeMaker.Ident(logObjName), slf4jMethodName), List.from(logArgs))));

        factory.get(ProcessorFactory.Kind.BLOCK).process(methodBody);

        // Add try-catch statement.
        if (methodConfig.isExceptionLog()) {

            String methodError = String.format(PREFIX, methodConfig.getMethodName(), "METHOD_ERROR",
                    lineMap.getLineNumber(methodBody.getStartPosition())) + "Error!";
            Name p = names.fromString("e");
            logArgs.removeFirst();
            logArgs.addFirst(treeMaker.Literal(methodError + logParams));
            logArgs.add(treeMaker.Ident(p));
            JCTree.JCCatch jcCatch = treeMaker.Catch(treeMaker.VarDef(treeMaker.Modifiers(0), p,
                            treeMaker.Ident(names.fromString("Exception")), null),
                    treeMaker.Block(0L, List.of(treeMaker.Exec(treeMaker.Apply(List.nil(),
                                    treeMaker.Select(treeMaker.Ident(logObjName), slf4jMethodName),
                                    List.from(logArgs))),
                            treeMaker.Throw(treeMaker.Ident(p)))));

            methodBody.stats = List.of(treeMaker.Try(treeMaker.Block(methodBody.flags, methodBody.stats),
                    List.of(jcCatch), null));
        }
    }
}
