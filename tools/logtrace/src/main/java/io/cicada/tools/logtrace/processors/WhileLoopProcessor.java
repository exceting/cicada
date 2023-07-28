package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

public class WhileLoopProcessor extends TreeProcessor {
    WhileLoopProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCWhileLoop)) {
            return;
        }
        JCTree.JCWhileLoop whileLoop = (JCTree.JCWhileLoop) jcTree;
        // TODO The cond may should be processed.
        // whileLoop.cond
        factory.get(ProcessorFactory.Kind.BLOCK).process(whileLoop.body);
    }
}
