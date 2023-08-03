package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

public class MethodInvocationProcessor extends TreeProcessor {

    MethodInvocationProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCMethodInvocation)) {
            return;
        }
        JCTree.JCMethodInvocation methodInvocation = (JCTree.JCMethodInvocation) jcTree;
        if (methodInvocation.args != null && methodInvocation.args.size() > 0) {
            methodInvocation.args.forEach(arg -> factory.get(arg.getKind()).process(arg));
        }
        System.out.println("))))))))------   "+methodInvocation.getMethodSelect().getClass());
        if (methodInvocation.getMethodSelect() != null) {
            factory.get(methodInvocation.getMethodSelect().getKind()).process(methodInvocation.getMethodSelect());
        }
    }
}
