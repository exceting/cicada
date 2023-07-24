package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.AnnoProcessor;
import org.graalvm.compiler.debug.Indent;

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
        List<JCTree.JCStatement> statements = methodBody.stats;

        JCTree.JCExpressionStatement exec = treeMaker.Exec(
                treeMaker.Assign(treeMaker.Ident(names.fromString("add")),
                        treeMaker.Binary(JCTree.Tag.PLUS,
                                treeMaker.Ident(names.fromString("a")),
                                treeMaker.Ident(names.fromString("b")))));

        AnnoProcessor.GlobalConfig globalConfig = AnnoProcessor.config.get();
        AnnoProcessor.GlobalConfig.MethodConfig methodConfig = globalConfig.getMethodConfigMap().get(jcTree);
        JCTree.JCIdent logObj = treeMaker.Ident(names.fromString(globalConfig.getLogIdentName()));

        StringBuilder logMsg = new StringBuilder(PREFIX);
        logMsg.append("Method ").append(methodDecl.getName()).append(" invoked!");
        // Get args.
        List<JCTree.JCVariableDecl> params = methodDecl.getParameters();
        if (params != null && params.size() > 0) {
            params.forEach(p -> {
                System.out.println("参数：" + p.getName().toString() + "      参数类型：" + p.sym.owner);
            });
        }

        JCTree.JCExpressionStatement logTrace = treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(logObj, getSlf4jMethod(methodConfig.getTraceLevel())),
                List.of(treeMaker.Literal("xxxxxddfddd"))));

        methodBody.stats = statements.append(exec).append(logTrace);
        methodBody.stats.forEach(s -> {
            switch (s.getKind()) {
                case IF:
                    factory.get(ProcessorFactory.Kind.IF_STATEMENT).process(s);
                    break;
                default:
            }
        });
    }
}
