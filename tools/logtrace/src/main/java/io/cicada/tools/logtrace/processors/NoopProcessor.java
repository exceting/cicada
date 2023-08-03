package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * Default processor, do nothing.
 */
public class NoopProcessor extends TreeProcessor {

    NoopProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process() {
        super.process();
    }

    @Override
    public void process(JCTree jcTree) {
        super.process(jcTree);
    }

    @Override
    public void process(JCTree... jcTrees) {
        super.process(jcTrees);
    }
}
