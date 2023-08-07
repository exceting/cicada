package io.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#DO_WHILE_LOOP}.
 * eg:
 * <pre>
 *     do
 *         statement
 *     while ( expression );
 * </pre>
 */
public class DoWhileLoopProcessor extends TreeProcessor {
    public DoWhileLoopProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCDoWhileLoop)) {
            return;
        }
        JCTree.JCDoWhileLoop doWhileLoop = (JCTree.JCDoWhileLoop) jcTree;
        if (doWhileLoop.getStatement() != null) {
            getFactory().get(doWhileLoop.getStatement().getKind()).process(doWhileLoop.getStatement());
        }
        if (doWhileLoop.getCondition() != null) {
            getFactory().get(doWhileLoop.getCondition().getKind()).process(doWhileLoop.getCondition());
        }
    }
}
