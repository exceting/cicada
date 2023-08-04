package io.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#CONDITIONAL_EXPRESSION}.
 * eg:
 * <pre>
 *     condition ? trueExpression : falseExpression
 * </pre>
 */
public class ConditionalProcessor extends TreeProcessor {

    public ConditionalProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCConditional)) {
            return;
        }

        JCTree.JCConditional jcConditional = (JCTree.JCConditional) jcTree;

        if (jcConditional.getCondition() != null) {
            getFactory().get(jcConditional.getCondition().getKind()).process(jcConditional.getCondition());
        }

        if (jcConditional.getTrueExpression() != null) {
            getFactory().get(jcConditional.getTrueExpression().getKind()).process(jcConditional.getTrueExpression());
        }

        if (jcConditional.getFalseExpression() != null) {
            getFactory().get(jcConditional.getFalseExpression().getKind()).process(jcConditional.getFalseExpression());
        }
    }
}
