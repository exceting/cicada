package io.github.exceting.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#PARENTHESIZED}.
 * eg:
 * <pre>
 *     ( expression )
 * </pre>
 */
public class ParensProcessor extends TreeProcessor {
    public ParensProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCParens)) {
            return;
        }
        JCTree.JCParens jcParens = (JCTree.JCParens) jcTree;
        JCTree.JCExpression jcExpression = jcParens.getExpression();
        if (jcExpression != null) {
            getFactory().get(jcExpression.getKind()).process(jcExpression);
        }
    }
}
