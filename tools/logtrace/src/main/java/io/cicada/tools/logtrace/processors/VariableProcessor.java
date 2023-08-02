package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.context.Context;

import java.util.HashMap;
import java.util.Map;

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
            // Get current block.
            Context.MethodConfig.OriginCode originCode = Context.currentMethodConfig.get().getBlockStack().peek();
            if (originCode != null) {
                Context.MethodConfig methodConfig = Context.currentMethodConfig.get();
                Map<String, JCTree.JCExpression> newArgs = new HashMap<>();
                newArgs.put(jcVariableDecl.getName().toString(), treeMaker.Ident(jcVariableDecl.getName()));
                originCode.addNewCode(new Context.MethodConfig.NewCode(originCode.getOffset() + 1,
                        methodConfig.getLogContent()
                                .getNewCodeStatement(Tree.Kind.VARIABLE, jcVariableDecl,
                                        "", newArgs, treeMaker, names)));
            }
        }
    }
}
