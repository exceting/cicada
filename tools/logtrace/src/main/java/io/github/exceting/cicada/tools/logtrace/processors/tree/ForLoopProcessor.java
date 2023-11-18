package io.github.exceting.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#FOR_LOOP}.
 * eg:
 * <pre>
 *     for ( initializer ; condition ; update )
 *         statement
 * </pre>
 */
public class ForLoopProcessor extends TreeProcessor {
    public ForLoopProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCForLoop)) {
            return;
        }

        JCTree.JCForLoop forLoop = (JCTree.JCForLoop) jcTree;
        if (forLoop.getInitializer() != null && forLoop.getInitializer().size() > 0) {
            forLoop.getInitializer().forEach(i -> getFactory().get(i.getKind()).process(i));
        }

        if (forLoop.getCondition() != null) {
            getFactory().get(forLoop.getCondition().getKind()).process(forLoop.getCondition());
        }

        if (forLoop.getUpdate() != null && forLoop.getUpdate().size() > 0) {
            forLoop.getUpdate().forEach(u -> getFactory().get(u.getKind()).process(u));
        }

        if (forLoop.getStatement() != null) {
            getFactory().get(forLoop.getStatement().getKind()).process(forLoop.getStatement());
        }
    }
}
