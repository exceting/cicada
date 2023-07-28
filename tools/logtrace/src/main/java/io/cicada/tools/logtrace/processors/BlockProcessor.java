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
        JCTree.JCBlock jcBlock = (JCTree.JCBlock) jcTree;
        AnnoProcessor.currentMethodConfig.get().getBlockStack().push(jcBlock);
        JCTree.JCStatement newCode = AnnoProcessor.currentMethodConfig.get().getAttachStack().isEmpty()
                ? null : AnnoProcessor.currentMethodConfig.get().getAttachStack().pop();
        if (jcBlock.getStatements() == null || jcBlock.getStatements().size() == 0) {
            return;
        }
        for (JCTree.JCStatement statement : jcBlock.getStatements()) {
            switch (statement.getKind()) {
                case IF:
                    factory.get(ProcessorFactory.Kind.IF_STATEMENT).process(statement);
                    break;
                case TRY:
                    factory.get(ProcessorFactory.Kind.TRY).process(statement);
                case FOR_LOOP:
                    factory.get(ProcessorFactory.Kind.FOR_LOOP).process(statement);
                case WHILE_LOOP:
                    factory.get(ProcessorFactory.Kind.WHILE_LOOP).process(statement);
                case DO_WHILE_LOOP:
                    factory.get(ProcessorFactory.Kind.DO_WHILE_LOOP).process(statement);
                case ENHANCED_FOR_LOOP:
                    factory.get(ProcessorFactory.Kind.ENHANCED_FOR_LOOP).process(statement);
                default:
                    // do nothing
            }
        }
        if (newCode != null) {
            // Add new code into the 1st line of current block.
            jcBlock.stats = attachCode(jcBlock.stats, newCode, 0);
        }
        // Pop block.
        AnnoProcessor.currentMethodConfig.get().getBlockStack().pop();
    }
}
