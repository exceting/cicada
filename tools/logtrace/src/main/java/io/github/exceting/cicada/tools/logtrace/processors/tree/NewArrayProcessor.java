package io.github.exceting.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#NEW_ARRAY}.
 * eg:
 * <pre>
 *     new type dimensions initializers
 *     new type dimensions [ ] initializers
 * </pre>
 */
public class NewArrayProcessor extends TreeProcessor {
    public NewArrayProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCNewArray)) {
            return;
        }

        JCTree.JCNewArray jcNewArray = (JCTree.JCNewArray) jcTree;
        if (jcNewArray.getInitializers() != null && jcNewArray.getInitializers().size() > 0) {
            jcNewArray.getInitializers().forEach(i -> getFactory().get(i.getKind()).process(i));
        }

        if (jcNewArray.getDimensions() != null && jcNewArray.getDimensions().size() > 0) {
            jcNewArray.getDimensions().forEach(dim -> getFactory().get(dim.getKind()).process(dim));
        }
    }
}
