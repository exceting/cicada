package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

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
        factory.get(ProcessorFactory.Kind.BLOCK).process(jcTry.body);
    }
}
