package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

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
        JCTree.JCExpression selected = fieldAccess.getExpression();
        if (selected != null) {
            factory.get(selected.getKind()).process(selected);
        }
    }
}
