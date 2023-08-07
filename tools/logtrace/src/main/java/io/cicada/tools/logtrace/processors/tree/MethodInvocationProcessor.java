package io.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#METHOD_INVOCATION}.
 * eg:
 * <pre>
 *     identifier ( arguments )
 *     this . typeArguments identifier ( arguments )
 * </pre>
 */
public class MethodInvocationProcessor extends TreeProcessor {

    public MethodInvocationProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCMethodInvocation)) {
            return;
        }

        JCTree.JCMethodInvocation methodInvocation = (JCTree.JCMethodInvocation) jcTree;
        if (methodInvocation.getArguments() != null && methodInvocation.getArguments().size() > 0) {
            methodInvocation.getArguments().forEach(arg -> getFactory().get(arg.getKind()).process(arg));
        }

        if (methodInvocation.getMethodSelect() != null) {
            System.out.println("========   "+methodInvocation.getMethodSelect()+"     "+methodInvocation.getMethodSelect());
            getFactory().get(methodInvocation.getMethodSelect().getKind()).process(methodInvocation.getMethodSelect());
        }
    }
}
