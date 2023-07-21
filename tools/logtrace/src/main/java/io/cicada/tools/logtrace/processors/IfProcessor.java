package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A processor for handling IF statement.
 */
public class IfProcessor extends TreeProcessor {

    IfProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCIf)) {
            return;
        }
        JCTree.JCIf jcIf = (JCTree.JCIf) jcTree;
        System.out.printf("cond = %s\n\nthen = %s", jcIf.cond, jcIf.thenpart);
        System.out.println("============================");

        factory.get(ProcessorFactory.Kind.IF_COND).process(jcIf.cond);
        process(jcIf.elsepart);
    }
}
