package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#CONDITIONAL_EXPRESSION}.
 */
public class ConditionalProcessor extends TreeProcessor {

    ConditionalProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    // TODO IF-STATEMENT conditional process.
    @Override
    public void process(JCTree jcTree) {
        /*if (jcTree instanceof JCTree.JCParens) {
            JCTree.JCParens parens = (JCTree.JCParens) jcTree;
            process(parens.getExpression());
        } else if (jcTree instanceof JCTree.JCBinary) {
            JCTree.JCBinary jcBinary = (JCTree.JCBinary) jcTree;
            process(jcBinary.rhs);
            process(jcBinary.lhs);
        } else if (jcTree instanceof JCTree.JCLiteral) {
            JCTree.JCLiteral jcLiteral = (JCTree.JCLiteral) jcTree;
        } else if (jcTree instanceof JCTree.JCIdent) {
            JCTree.JCIdent jcIdent = (JCTree.JCIdent) jcTree;
        } else if (jcTree instanceof JCTree.JCMethodInvocation) {
            JCTree.JCMethodInvocation methodInvocation = (JCTree.JCMethodInvocation) jcTree;
        } else if (jcTree instanceof JCTree.JCInstanceOf) {
            JCTree.JCInstanceOf instanceOf = (JCTree.JCInstanceOf) jcTree;
        } else {
            // do nothing.
        }*/
    }
}
