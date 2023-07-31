package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.context.Context;

public class EnhancedForLoopProcessor extends TreeProcessor {
    EnhancedForLoopProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (Context.currentMethodConfig.get().isBanLoop()) {
            return;
        }
        if (!(jcTree instanceof JCTree.JCEnhancedForLoop)) {
            return;
        }
        JCTree.JCEnhancedForLoop enhancedForLoop = (JCTree.JCEnhancedForLoop) jcTree;
        // TODO The expr,var may should be processed.
        // enhancedForLoop.expr, enhancedForLoop.var
        factory.get(Tree.Kind.BLOCK).process(enhancedForLoop.body);
    }
}
