package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

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
        if (jcLambda.getBody() != null) {
            factory.get(jcLambda.getBody().getKind()).process(jcLambda.getBody());
        }
    }
}
