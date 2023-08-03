package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#EXPRESSION_STATEMENT}.
 */
public class ExpressionStatementProcessor extends TreeProcessor {
    ExpressionStatementProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
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
            factory.get(expression.getKind()).process(expression);
        }
    }
}
