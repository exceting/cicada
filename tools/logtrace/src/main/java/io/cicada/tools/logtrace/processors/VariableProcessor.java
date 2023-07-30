package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.AnnoProcessor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class VariableProcessor extends TreeProcessor {

    VariableProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    /**
     * Only process conditional and method invoke.
     */
    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCVariableDecl)) {
            return;
        }
        JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) jcTree;
        if (jcVariableDecl.init instanceof JCTree.JCConditional
                || jcVariableDecl.init instanceof JCTree.JCMethodInvocation) {

            String className = "";
            if (jcVariableDecl.getType() instanceof JCTree.JCPrimitiveTypeTree) {
                className = jcVariableDecl.getType().toString();
            }
            if (jcVariableDecl.getType() instanceof JCTree.JCIdent) {
                JCTree.JCIdent jcIdent = (JCTree.JCIdent) jcVariableDecl.getType();
            }

            System.out.println("XXXXXXXX  " + jcVariableDecl + "    " + jcVariableDecl.getType().getClass());
            // Get current block.
            JCTree.JCBlock blockStack = AnnoProcessor.currentMethodConfig.get().getBlockStack().peek();
            if (blockStack != null) {

            }
        }
    }
}
