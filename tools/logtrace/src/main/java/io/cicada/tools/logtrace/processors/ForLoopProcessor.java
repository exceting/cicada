package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.AnnoProcessor;

public class ForLoopProcessor extends TreeProcessor {
    ForLoopProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!AnnoProcessor.currentMethodConfig.get().isTraceLoop()) {
            return;
        }
        if (!(jcTree instanceof JCTree.JCForLoop)) {
            return;
        }
        JCTree.JCForLoop forLoop = (JCTree.JCForLoop) jcTree;
        // TODO The cond,init,step may should be processed.
        // forLoop.cond, forLoop.init, forLoop.step
        factory.get(Tree.Kind.BLOCK).process(forLoop.body);
    }
}
