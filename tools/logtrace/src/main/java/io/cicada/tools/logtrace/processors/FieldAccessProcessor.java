package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#MEMBER_SELECT}.
 * eg:
 * a.b.c;
 */
public class FieldAccessProcessor extends TreeProcessor {
    FieldAccessProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCFieldAccess)) {
            return;
        }
        JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) jcTree;
        System.out.println("******* "+fieldAccess);
        JCTree.JCExpression selected = fieldAccess.getExpression();
        if (selected != null) {
            factory.get(selected.getKind()).process(selected);
        }
    }
}
