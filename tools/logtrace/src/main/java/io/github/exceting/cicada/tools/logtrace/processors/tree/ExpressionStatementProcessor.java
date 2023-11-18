package io.github.exceting.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#EXPRESSION_STATEMENT}.
 * <pre>
 *     expression ;
 * </pre>
 */
public class ExpressionStatementProcessor extends TreeProcessor {
    public ExpressionStatementProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCExpressionStatement)) {
            return;
        }
        JCTree.JCExpressionStatement expressionStatement = (JCTree.JCExpressionStatement) jcTree;
        JCTree.JCExpression expression = expressionStatement.getExpression();
        if (expression != null) {
            getFactory().get(expression.getKind()).process(expression);
        }
    }
}
