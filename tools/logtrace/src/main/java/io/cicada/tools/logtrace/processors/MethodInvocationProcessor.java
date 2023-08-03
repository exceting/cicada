package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#METHOD_INVOCATION}.
 * eg:
 * a.b();
 */
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
        //System.out.println("+++++++++++  "+methodInvocation+"   "+methodInvocation.getKind());
        if (methodInvocation.args != null && methodInvocation.args.size() > 0) {
            methodInvocation.args.forEach(arg -> factory.get(arg.getKind()).process(arg));
        }
        if (methodInvocation.getMethodSelect() != null) {
            factory.get(methodInvocation.getMethodSelect().getKind()).process(methodInvocation.getMethodSelect());
        }
    }
}
