package io.github.exceting.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#NEW_CLASS}.
 * eg:
 * <pre>
 *     new identifier ( )
 *
 *     new identifier ( arguments )
 *
 *     new typeArguments identifier ( arguments )
 *         classBody
 *
 *     enclosingExpression.new identifier ( arguments )
 * </pre>
 */
public class NewClassProcessor extends TreeProcessor {
    public NewClassProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCNewClass)) {
            return;
        }
        JCTree.JCNewClass jcNewClass = (JCTree.JCNewClass) jcTree;
        if (jcNewClass.getArguments() != null && jcNewClass.getArguments().size() > 0) {
            jcNewClass.getArguments().forEach(arg -> getFactory().get(arg.getKind()).process(arg));
        }
        // Only anonymous inner class has the class body.
        if (jcNewClass.getClassBody() != null) {
            getFactory().get(jcNewClass.getClassBody().getKind()).process(jcNewClass.getClassBody());
        }
    }
}
