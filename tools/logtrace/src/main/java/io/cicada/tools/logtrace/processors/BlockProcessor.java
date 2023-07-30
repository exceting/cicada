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
                    break;
                case FOR_LOOP:
                    factory.get(ProcessorFactory.Kind.FOR_LOOP).process(statement);
                    break;
                case WHILE_LOOP:
                    factory.get(ProcessorFactory.Kind.WHILE_LOOP).process(statement);
                    break;
                case DO_WHILE_LOOP:
                    factory.get(ProcessorFactory.Kind.DO_WHILE_LOOP).process(statement);
                    break;
                case ENHANCED_FOR_LOOP:
                    factory.get(ProcessorFactory.Kind.ENHANCED_FOR_LOOP).process(statement);
                    break;
                case VARIABLE:
                    factory.get(ProcessorFactory.Kind.VARIABLE).process(statement);
                    break;
                default:
                    // do nothing
            }
        }
        // Pop block.
        AnnoProcessor.currentMethodConfig.get().getBlockStack().pop();
    }
}
