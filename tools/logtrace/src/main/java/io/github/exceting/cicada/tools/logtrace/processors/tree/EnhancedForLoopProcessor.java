package io.github.exceting.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#ENHANCED_FOR_LOOP}.
 * eg:
 * <pre>
 *     for ( variable : expression )
 *         statement
 * </pre>
 */
public class EnhancedForLoopProcessor extends TreeProcessor {
    public EnhancedForLoopProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCEnhancedForLoop)) {
            return;
        }

        JCTree.JCEnhancedForLoop enhancedForLoop = (JCTree.JCEnhancedForLoop) jcTree;
        if (enhancedForLoop.getStatement() != null) {
            getFactory().get(enhancedForLoop.getStatement().getKind()).process(enhancedForLoop.getStatement());
        }

        if (enhancedForLoop.getExpression() != null) {
            getFactory().get(enhancedForLoop.getExpression().getKind()).process(enhancedForLoop.getExpression());
        }

        if (enhancedForLoop.getVariable() != null) {
            getFactory().get(enhancedForLoop.getVariable().getKind()).process(enhancedForLoop.getVariable());
        }
    }
}
