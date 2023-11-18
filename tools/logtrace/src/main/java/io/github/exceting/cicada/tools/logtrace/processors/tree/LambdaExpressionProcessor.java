package io.github.exceting.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.context.Context;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#LAMBDA_EXPRESSION}.
 * eg:
 * <pre>
 *      ()->{}
 *      (List<String> ls)->ls.size()
 *      (x,y)-> { return x + y; }
 * </pre>
 */
public class LambdaExpressionProcessor extends TreeProcessor {

    public LambdaExpressionProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCLambda)) {
            return;
        }

        JCTree.JCLambda jcLambda = (JCTree.JCLambda) jcTree;

        // TODO JCTree.JCLambda.getParameters() may should be processed.
        /*if(jcLambda.getParameters() != null && jcLambda.getParameters().size() > 0){
            jcLambda.getParameters().forEach(p-> getFactory().get(p.getKind()).process(p));
        }*/
        if (jcLambda.getBody() != null) {
            Context.MethodConfig methodConfig = Context.currentMethodConfig.get();
            methodConfig.setInClassOrLambda(true);
            getFactory().get(jcLambda.getBody().getKind()).process(jcLambda.getBody());
            methodConfig.setInClassOrLambda(false);
        }
    }
}
