package io.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#TRY}.
 * eg:
 * <pre>
 *     try
 *         block
 *     catches
 *     finally
 *         finallyBlock
 * </pre>
 */
public class TryProcessor extends TreeProcessor {

    public TryProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCTry)) {
            return;
        }

        JCTree.JCTry jcTry = (JCTree.JCTry) jcTree;
        // Since Java7: try-with-resources statements
        if (jcTry.getResources() != null && jcTry.getResources().size() > 0) {
            jcTry.getResources().forEach(r -> getFactory().get(r.getKind()).process(r));
        }

        // Try body.
        if (jcTry.getBlock() != null) {
            getFactory().get(jcTry.getBlock().getKind()).process(jcTry.getBlock());
        }

        // Catches.
        if (jcTry.getCatches() != null && jcTry.getCatches().size() > 0) {
            jcTry.getCatches().forEach(c -> getFactory().get(c.getKind()).process(c));
        }

        // Finally.
        if (jcTry.getFinallyBlock() != null) {
            getFactory().get(jcTry.getFinallyBlock().getKind()).process(jcTry.getFinallyBlock());
        }
    }
}
