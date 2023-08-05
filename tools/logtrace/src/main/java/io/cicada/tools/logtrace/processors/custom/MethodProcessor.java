package io.cicada.tools.logtrace.processors.custom;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Name;
import io.cicada.tools.logtrace.context.Context;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

import java.util.HashMap;
import java.util.Map;

public class MethodProcessor extends TreeProcessor {


    public MethodProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
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

        methodConfig.getBlockStack().push(new Context.MethodConfig.OriginCode(methodBody));

        try {
            getFactory().get(methodBody.getKind()).process(methodBody);
            methodBody.accept(new JCTree.Visitor() {
                @Override
                public void visitBlock(JCTree.JCBlock that) {
                    that.stats = generateCode(that.getStatements(), new Context.MethodConfig.NewCode(0,
                            methodConfig.getLogContent().getNewCodeStatement(Tree.Kind.METHOD, methodBody,
                                    "Start!", null, getTreeMaker(), getNames())));
                }
            });

            // Generate try-catch statement.
            if (methodConfig.isExceptionLog()) {
                methodBody.accept(new JCTree.Visitor() {
                    @Override
                    public void visitBlock(JCTree.JCBlock that) {
                        Name e = getNames().fromString("e");
                        JCTree.JCIdent eIdent = getTreeMaker().Ident(e);
                        Map<String, JCTree.JCExpression> newArgs = new HashMap<>();
                        newArgs.put(null, eIdent);
                        JCTree.JCCatch jcCatch = getTreeMaker().Catch(getTreeMaker().VarDef(getTreeMaker().Modifiers(0), e,
                                        getTreeMaker().Ident(getNames().fromString("Exception")), null),
                                getTreeMaker().Block(0L, List.of(methodConfig.getLogContent()
                                        .getNewCodeStatement(Tree.Kind.TRY, methodBody,
                                                "Error!", newArgs, getTreeMaker(), getNames()), getTreeMaker().Throw(eIdent))));
                        that.stats = List.of(getTreeMaker().Try(getTreeMaker().Block(methodBody.flags, methodBody.stats),
                                List.of(jcCatch), null));
                    }
                });
            }
        } finally {
            methodConfig.getBlockStack().pop();
        }
    }
}