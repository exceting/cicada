package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#LAMBDA_EXPRESSION}.
 * eg:
 * s->{}
 */
public class LambdaExpressionProcessor extends TreeProcessor {

    LambdaExpressionProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCLambda)) {
            return;
        }
        JCTree.JCLambda jcLambda = (JCTree.JCLambda) jcTree;
        //System.out.println("------  "+jcLambda.getBody());
        if (jcLambda.getBody() != null) {
            factory.get(jcLambda.getBody().getKind()).process(jcLambda.getBody());
        }
    }
}
