package io.github.exceting.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.context.Context;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#BLOCK}.
 * eg: {}
 */
public class BlockProcessor extends TreeProcessor {

    public BlockProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCBlock)) {
            return;
        }
        Context.MethodConfig methodConfig = Context.currentMethodConfig.get();
        Context.MethodConfig.OriginCode originCode = new Context.MethodConfig.OriginCode((JCTree.JCBlock) jcTree);
        if (!methodConfig.getBlockStack().isEmpty() && !methodConfig.isInClassOrLambda()) {
            Context.MethodConfig.OriginCode parentOriginCode = Context.currentMethodConfig.get().getBlockStack().peek();
            originCode.getVars().putAll(parentOriginCode.getVars()); // Inherit the vars of the parent block
        }
        Context.currentMethodConfig.get().getBlockStack().push(originCode);
        try {
            if (originCode.getBlock().getStatements() == null || originCode.getBlock().getStatements().size() == 0) {
                return;
            }
            for (JCTree.JCStatement statement : originCode.getBlock().getStatements()) {
                getFactory().get(statement.getKind()).process(statement);
                originCode.incrOffset();
            }
            originCode.getBlock().accept(new JCTree.Visitor() {
                @Override
                public void visitBlock(JCTree.JCBlock that) {
                    that.stats = generateCode(that.getStatements(), originCode.getNewCodes());
                }
            });
        } finally {
            // Pop block.
            Context.currentMethodConfig.get().getBlockStack().pop();
        }
    }
}
