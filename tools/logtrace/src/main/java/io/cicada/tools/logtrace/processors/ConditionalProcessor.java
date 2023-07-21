package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A processor for handling Expression.
 */
public class ConditionalProcessor extends TreeProcessor {

    ConditionalProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (jcTree instanceof JCTree.JCParens) {
            JCTree.JCParens parens = (JCTree.JCParens) jcTree;
            process(parens.getExpression());
        } else if (jcTree instanceof JCTree.JCBinary) {
            JCTree.JCBinary jcBinary = (JCTree.JCBinary) jcTree;
            System.out.println(jcBinary.lhs + "    *********     " + jcBinary.rhs);
            process(jcBinary.rhs);
            process(jcBinary.lhs);
        } else if (jcTree instanceof JCTree.JCLiteral) {
            JCTree.JCLiteral jcLiteral = (JCTree.JCLiteral) jcTree;
            System.out.println(jcLiteral.typetag + "    AAAAAAAAA     " + jcLiteral.value);
        } else if (jcTree instanceof JCTree.JCIdent) {
            JCTree.JCIdent jcIdent = (JCTree.JCIdent) jcTree;
            System.out.println(jcIdent.sym + "    BBBBBBBBBB     " + jcIdent.name);
        } else if (jcTree instanceof JCTree.JCMethodInvocation) {
            JCTree.JCMethodInvocation methodInvocation = (JCTree.JCMethodInvocation) jcTree;
            System.out.println("CCCCCCCCCCC      " + methodInvocation.getMethodSelect());
        } else if (jcTree instanceof JCTree.JCInstanceOf) {
            JCTree.JCInstanceOf instanceOf = (JCTree.JCInstanceOf) jcTree;
            System.out.println("DDDDDDDDDDDD     " + instanceOf);
        } else {
            System.out.println(jcTree.getClass());
        }
        // System.out.printf("ExpressionProcessor cond = %s         truePart = %s     falsePart = %s", conditional.cond, conditional.truepart, conditional.falsepart);
    }
}
