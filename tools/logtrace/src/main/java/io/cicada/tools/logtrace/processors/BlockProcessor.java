package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.AnnoProcessor;

/**
 * A processor for handling BLOCK statement.
 * eg: {}
 */
public class BlockProcessor extends TreeProcessor {

    BlockProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCBlock)) {
            return;
        }
        AnnoProcessor.MethodConfig.OldCode oldCode = new AnnoProcessor.MethodConfig.OldCode((JCTree.JCBlock) jcTree);
        AnnoProcessor.currentMethodConfig.get().getBlockStack().push(oldCode);
        try {
            if (oldCode.getBlock().getStatements() == null || oldCode.getBlock().getStatements().size() == 0) {
                return;
            }
            for (JCTree.JCStatement statement : oldCode.getBlock().getStatements()) {
                factory.get(statement.getKind()).process(statement);
                oldCode.incrOffset();
            }
            oldCode.getBlock().stats = attachCode(oldCode.getBlock().stats, oldCode.getNewCodes());
        } finally {
            // Pop block.
            AnnoProcessor.currentMethodConfig.get().getBlockStack().pop();
        }
    }
}
