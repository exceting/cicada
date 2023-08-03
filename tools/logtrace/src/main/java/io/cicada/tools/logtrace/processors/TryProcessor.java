package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#TRY}.
 * eg:
 * try {
 * //...
 * } catch(Exception e) {
 * //...
 * }finally{
 * //...
 * }
 */
public class TryProcessor extends TreeProcessor {

    TryProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCTry)) {
            return;
        }
        JCTree.JCTry jcTry = (JCTree.JCTry) jcTree;
        factory.get(Tree.Kind.BLOCK).process(jcTry.body);
    }
}
