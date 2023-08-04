package io.cicada.tools.logtrace.processors.tree;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.context.Context;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#IF}.
 * eg:
 * <pre>
 *     if ( condition )
 *        thenStatement
 *
 *     if ( condition )
 *         thenStatement
 *     else
 *         elseStatement
 * </pre>
 */
public class IfProcessor extends TreeProcessor {

    public IfProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
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
            if (jcIf.getCondition() != null) {
                getFactory().get(jcIf.getCondition().getKind()).process(jcIf.getCondition());
            }

            Context.MethodConfig.NewCode newCode = new Context.MethodConfig.NewCode(0,
                    methodConfig.getLogContent().getNewCodeStatement(Tree.Kind.IF, jcIf,
                            String.format("The condition: %s is true!", jcIf.getCondition()),
                            null, getTreeMaker(), getNames()));
            if (jcIf.getThenStatement() != null) {
                getFactory().get(jcIf.getThenStatement().getKind()).process(jcIf.getThenStatement());
                if (jcIf.getThenStatement() instanceof JCTree.JCBlock) {
                    JCTree.JCBlock then = (JCTree.JCBlock) jcIf.getThenStatement();
                    then.stats = attachCode(then.stats, newCode);
                }
            }
            if (jcIf.getElseStatement() != null) {
                getFactory().get(jcIf.getElseStatement().getKind()).process(jcIf.getElseStatement());
            }
        } else {
            Context.MethodConfig.NewCode newCode = new Context.MethodConfig.NewCode(0,
                    methodConfig.getLogContent().getNewCodeStatement(Tree.Kind.IF, jcTree,
                            "The condition: else is true!",
                            null, getTreeMaker(), getNames()));
            getFactory().get(jcTree.getKind()).process(jcTree);
            JCTree.JCBlock elsePart = (JCTree.JCBlock) jcTree;
            elsePart.stats = attachCode(elsePart.stats, newCode);
        }
    }
}
