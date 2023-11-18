package io.github.exceting.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

public class SynchronizedProcessor extends TreeProcessor {

    public SynchronizedProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCSynchronized)) {
            return;
        }
        JCTree.JCSynchronized jcSynchronized = (JCTree.JCSynchronized) jcTree;
        if (jcSynchronized.getExpression() != null) {
            getFactory().get(jcSynchronized.getExpression().getKind()).process(jcSynchronized.getExpression());
        }
        if (jcSynchronized.getBlock() != null) {
            getFactory().get(jcSynchronized.getBlock().getKind()).process(jcSynchronized.getBlock());
        }
    }
}
