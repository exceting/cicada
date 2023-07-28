package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

public class DoWhileLoopProcessor extends TreeProcessor {
    DoWhileLoopProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCDoWhileLoop)) {
            return;
        }
        JCTree.JCDoWhileLoop doWhileLoop = (JCTree.JCDoWhileLoop) jcTree;
        // TODO The cond may should be processed.
        // doWhileLoop.cond
        factory.get(ProcessorFactory.Kind.BLOCK).process(doWhileLoop.body);
    }
}
