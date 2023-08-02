package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Name;
import io.cicada.tools.logtrace.context.Context;

import java.util.HashMap;
import java.util.Map;

public class MethodProcessor extends TreeProcessor {


    MethodProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCMethodDecl)) {
            return;
        }
        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) jcTree;
        JCTree.JCBlock methodBody = methodDecl.body;

        Context.MethodConfig methodConfig = Context.currentMethodConfig.get();

        methodConfig.getBlockStack().push(new Context.MethodConfig.OldCode(methodBody));

        try {
            Context.MethodConfig.NewCode startNewCode = new Context.MethodConfig.NewCode(0,
                    methodConfig.getLogContent().getNewCodeStatement(Tree.Kind.METHOD, methodBody,
                            "Start!", null, treeMaker, names));

            factory.get(Tree.Kind.BLOCK).process(methodBody);

            methodBody.stats = attachCode(methodBody.stats, startNewCode);

            // Add try-catch statement.
            if (methodConfig.isExceptionLog()) {
                Name e = names.fromString("e");
                JCTree.JCIdent eIdent = treeMaker.Ident(e);
                Map<String, JCTree.JCExpression> newArgs = new HashMap<>();
                newArgs.put(null, eIdent);
                JCTree.JCCatch jcCatch = treeMaker.Catch(treeMaker.VarDef(treeMaker.Modifiers(0), e,
                                treeMaker.Ident(names.fromString("Exception")), null),
                        treeMaker.Block(0L, List.of(methodConfig.getLogContent()
                                .getNewCodeStatement(Tree.Kind.TRY, methodBody,
                                        "Error!", newArgs, treeMaker, names), treeMaker.Throw(eIdent))));

                methodBody.stats = List.of(treeMaker.Try(treeMaker.Block(methodBody.flags, methodBody.stats),
                        List.of(jcCatch), null));
            }
        } finally {
            methodConfig.getBlockStack().pop();
        }
    }
}
