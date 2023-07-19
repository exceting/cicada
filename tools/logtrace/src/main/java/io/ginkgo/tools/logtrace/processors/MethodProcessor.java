package io.ginkgo.tools.logtrace.processors;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

public class MethodProcessor {

    public static void process(TreeMaker treeMaker, Names names, JCTree.JCMethodDecl methodDecl) {
        JCTree.JCBlock methodBody = methodDecl.body;
        List<JCTree.JCStatement> statements = methodBody.stats;

        JCTree.JCExpressionStatement exec = treeMaker.Exec(
                treeMaker.Assign(treeMaker.Ident(names.fromString("add")),
                        treeMaker.Binary(JCTree.Tag.PLUS,
                                treeMaker.Ident(names.fromString("a")),
                                treeMaker.Ident(names.fromString("b")))));

        // Method invocation
        JCTree.JCFieldAccess accessOut = treeMaker.Select(treeMaker.Ident(names.fromString("System")),
                names.fromString("out")); // Access System.out
        JCTree.JCFieldAccess accessPrintln = treeMaker.Select(accessOut, names.fromString("println")); // Access System.out.println

        JCTree.JCExpressionStatement invocation = treeMaker.Exec(treeMaker.Apply(null,
                accessPrintln, List.of(treeMaker.Ident(names.fromString("add"))))); // Apply System.out.println(add);

        methodBody.stats = statements.append(exec).append(invocation);

        methodBody.stats.forEach(s -> {
            System.out.println("######     " + s.pos + "     " + s);
        });
        methodBody.stats.forEach(s -> {
            switch (s.getKind()) {
                case IF:
                    IfProcessor.process(treeMaker, names, s);
                    break;
                default:
            }
        });
    }

}
