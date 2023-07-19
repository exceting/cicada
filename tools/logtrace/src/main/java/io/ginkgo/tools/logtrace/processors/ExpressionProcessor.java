package io.ginkgo.tools.logtrace.processors;

import com.sun.tools.javac.tree.JCTree;

/**
 * A processor for handling Expression.
 */
public class ExpressionProcessor {

    public static void process(JCTree.JCExpression jcExpression) {
        if (jcExpression instanceof JCTree.JCParens) {
            JCTree.JCParens parens = (JCTree.JCParens) jcExpression;
            process(parens.getExpression());
        } else if (jcExpression instanceof JCTree.JCBinary) {
            JCTree.JCBinary jcBinary = (JCTree.JCBinary) jcExpression;
            System.out.println(jcBinary.lhs + "    *********     " + jcBinary.rhs);
            process(jcBinary.rhs);
            process(jcBinary.lhs);
        } else if (jcExpression instanceof JCTree.JCLiteral) {
            JCTree.JCLiteral jcLiteral = (JCTree.JCLiteral) jcExpression;
            System.out.println(jcLiteral.typetag + "    AAAAAAAAA     " + jcLiteral.value);
        } else if (jcExpression instanceof JCTree.JCIdent) {
            JCTree.JCIdent jcIdent = (JCTree.JCIdent) jcExpression;
            System.out.println(jcIdent.sym + "    BBBBBBBBBB     " + jcIdent.name);
        } else if (jcExpression instanceof JCTree.JCMethodInvocation) {
            JCTree.JCMethodInvocation methodInvocation = (JCTree.JCMethodInvocation) jcExpression;
            System.out.println("CCCCCCCCCCC      " + methodInvocation.getMethodSelect());
        } else if (jcExpression instanceof JCTree.JCInstanceOf) {
            JCTree.JCInstanceOf instanceOf = (JCTree.JCInstanceOf) jcExpression;
            System.out.println("DDDDDDDDDDDD     " + instanceOf);
        } else {
            System.out.println(jcExpression.getClass());
        }
        // System.out.printf("ExpressionProcessor cond = %s         truePart = %s     falsePart = %s", conditional.cond, conditional.truepart, conditional.falsepart);
    }
}
