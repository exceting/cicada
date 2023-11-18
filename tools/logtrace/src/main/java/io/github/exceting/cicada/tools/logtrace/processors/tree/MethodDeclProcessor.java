package io.github.exceting.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#METHOD}.
 * eg:
 * <pre>
 *      modifiers typeParameters type name
 *        ( parameters )
 *        body
 *
 *     modifiers type name () default defaultValue
 * </pre>
 */
public class MethodDeclProcessor extends TreeProcessor {
    public MethodDeclProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCMethodDecl)) {
            return;
        }
        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) jcTree;
        // Only process body.
        if (methodDecl.getBody() != null) {
            getFactory().get(methodDecl.getBody().getKind()).process(methodDecl.getBody());
        }
    }
}
