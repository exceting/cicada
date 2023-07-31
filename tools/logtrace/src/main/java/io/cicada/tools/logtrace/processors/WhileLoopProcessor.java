package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.context.Context;

public class WhileLoopProcessor extends TreeProcessor {
    WhileLoopProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (Context.currentMethodConfig.get().isBanLoop()) {
            return;
        }
        if (!(jcTree instanceof JCTree.JCWhileLoop)) {
            return;
        }
        JCTree.JCWhileLoop whileLoop = (JCTree.JCWhileLoop) jcTree;
        // TODO The cond may should be processed.
        // whileLoop.cond
        factory.get(Tree.Kind.BLOCK).process(whileLoop.body);
    }
}
