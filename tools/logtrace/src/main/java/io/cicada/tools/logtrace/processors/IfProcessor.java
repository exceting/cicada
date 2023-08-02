package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.context.Context;

/**
 * A processor for handling IF statement.
 * eg:
 * if(cond) {
 * // ...
 * } else if(cond2) {
 * // ...
 * } else{
 * // ...
 * }
 */
public class IfProcessor extends TreeProcessor {

    IfProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCIf) && !(jcTree instanceof JCTree.JCBlock)) {
            return;
        }
        Context.MethodConfig methodConfig = Context.currentMethodConfig.get();
        if (jcTree instanceof JCTree.JCIf) {
            JCTree.JCIf jcIf = (JCTree.JCIf) jcTree;
            // TODO The cond may should be processed.
            // factory.get(ProcessorFactory.Kind.IF_COND).process(jcIf.cond);
            Context.MethodConfig.NewCode newCode = new Context.MethodConfig.NewCode(0,
                    methodConfig.getLogContent().getNewCodeStatement(Tree.Kind.IF, jcIf,
                            String.format("The condition: %s is true!", jcIf.cond),
                            null, treeMaker, names));
            factory.get(Tree.Kind.BLOCK).process(jcIf.thenpart);
            if (jcIf.thenpart instanceof JCTree.JCBlock) {
                JCTree.JCBlock then = (JCTree.JCBlock) jcIf.thenpart;
                then.stats = attachCode(then.stats, newCode);
            }
            process(jcIf.elsepart);
        } else {
            Context.MethodConfig.NewCode newCode = new Context.MethodConfig.NewCode(0,
                    methodConfig.getLogContent().getNewCodeStatement(Tree.Kind.IF, jcTree,
                            "The condition: else is true!",
                            null, treeMaker, names));
            factory.get(Tree.Kind.BLOCK).process(jcTree);
            JCTree.JCBlock elsePart = (JCTree.JCBlock) jcTree;
            elsePart.stats = attachCode(elsePart.stats, newCode);
        }
    }
}
