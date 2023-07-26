package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.AnnoProcessor;

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
        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) jcTree;
        JCTree.JCBlock methodBody = methodDecl.body;

        AnnoProcessor.MethodConfig methodConfig = AnnoProcessor.currentMethodConfig.get();

        StringBuilder logMsg = new StringBuilder(String.format(PREFIX, methodConfig.getMethodName(), "METHOD_START"));
        logMsg.append("invoked!");
        // Get args.
        List<JCTree.JCVariableDecl> params = methodDecl.getParameters();
        LinkedList<JCTree.JCExpression> logArgs = new LinkedList<>();
        if (params != null && params.size() > 0) {
            Map<String, JCTree.JCExpression> argMap = argMap(params);
            if (argMap != null && argMap.size() > 0) {
                logMsg.append(" Params: ");
                argMap.forEach((k, v) -> {
                    logMsg.append(k).append(" = ").append("{}, ");
                    logArgs.add(v);
                });
            }
        }

        logArgs.addFirst(treeMaker.Literal(logMsg.toString()));
        // Push the new code into stack.
        methodConfig.getAttachStack().push(treeMaker.Exec(treeMaker.Apply(List.nil(),
                treeMaker.Select(treeMaker.Ident(names.fromString(AnnoProcessor.currentLogIdentName.get())),
                        getSlf4jMethod(methodConfig.getTraceLevel())), List.from(logArgs))));

        //methodBody.stats = attachCode(methodBody.stats, logTrace, offset);

        factory.get(ProcessorFactory.Kind.BLOCK).process(methodBody);
    }
}
