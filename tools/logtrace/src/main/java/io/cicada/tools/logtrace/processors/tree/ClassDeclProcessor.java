package io.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.context.Context;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#CLASS}.
 * Only for anonymous inner class!!
 * eg:
 * <pre>
 *     modifiers class simpleName typeParameters
 *         extends extendsClause
 *         implements implementsClause
 *     {
 *         members
 *     }
 * </pre>
 */
public class ClassDeclProcessor extends TreeProcessor {

    public ClassDeclProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    // Only for anonymous inner class!!
    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCClassDecl)) {
            return;
        }
        JCTree.JCClassDecl jcClassDecl = (JCTree.JCClassDecl) jcTree;

        if (jcClassDecl.getMembers() != null) {
            Context.MethodConfig methodConfig = Context.currentMethodConfig.get();
            methodConfig.setInClassOrLambda(true);
            // Only process method.
            jcClassDecl.getMembers().stream()
                    .filter(jm -> jm instanceof JCTree.JCMethodDecl)
                    .forEach(jm -> getFactory().get(jm.getKind()).process(jm));
            methodConfig.setInClassOrLambda(false);
        }
    }
}
