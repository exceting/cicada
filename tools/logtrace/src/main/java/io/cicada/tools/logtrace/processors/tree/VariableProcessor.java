package io.cicada.tools.logtrace.processors.tree;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.context.Context;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#VARIABLE}.
 * eg:
 * <pre>
 *     modifiers type name initializer ;
 *     modifiers type qualified-name.this
 * </pre>
 */
public class VariableProcessor extends TreeProcessor {

    public VariableProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    /**
     * Only process local variable, and the init value form conditional or method invoke.
     * eg:
     * <pre>
     *     int a = conditional ? value1 : value2;
     *     int b = getB();
     *     int c = 1; // Not process!
     * </pre>
     */
    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCVariableDecl)) {
            return;
        }
        JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) jcTree;
        if (jcVariableDecl.init != null) {
            getFactory().get(jcVariableDecl.init.getKind()).process(jcVariableDecl.init);
        }

        if (null instanceof JCTree.JCConditional
                || jcVariableDecl.init instanceof JCTree.JCMethodInvocation) {
            // Get current block.
            Context.MethodConfig.OriginCode originCode = Context.currentMethodConfig.get().getBlockStack().peek();
            if (originCode != null) {
                Context.MethodConfig methodConfig = Context.currentMethodConfig.get();
                Map<String, JCTree.JCExpression> newArgs = new HashMap<>();
                newArgs.put(jcVariableDecl.getName().toString(), getTreeMaker().Ident(jcVariableDecl.getName()));
                originCode.addNewCode(new Context.MethodConfig.NewCode(originCode.getOffset() + 1, // Next line.
                        methodConfig.getLogContent()
                                .getNewCodeStatement(Tree.Kind.VARIABLE, jcVariableDecl,
                                        "", newArgs, getTreeMaker(), getNames())));
            }
        }
    }
}
