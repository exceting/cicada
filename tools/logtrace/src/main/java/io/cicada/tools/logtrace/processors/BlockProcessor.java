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
        JCTree.JCStatement newCode = AnnoProcessor.currentMethodConfig.get().getAttachStack().isEmpty()
                ? null : (JCTree.JCStatement) AnnoProcessor.currentMethodConfig.get().getAttachStack().pop();
        JCTree.JCBlock jcBlock = (JCTree.JCBlock) jcTree;
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
                default:
                    // do nothing
            }
        }
        if (newCode != null) {
            jcBlock.stats = attachCode(jcBlock.stats, newCode, 0); // Add new code to the head.
        }
    }
}
