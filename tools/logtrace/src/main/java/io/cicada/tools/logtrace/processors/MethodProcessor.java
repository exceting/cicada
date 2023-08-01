package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position;
import com.sun.tools.javac.util.Name;
import io.cicada.tools.logtrace.context.Context;

import java.util.LinkedList;

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
        Position.LineMap lineMap = Context.lineMap.get();

        methodConfig.getBlockStack().push(new Context.MethodConfig.OldCode(methodBody));

        try {
            Name logObjName = names.fromString(Context.currentLogIdentName.get());
            Name slf4jMethodName = getSlf4jMethod(methodConfig.getTraceLevel());

            Context.MethodConfig.NewCode startNewCode = new Context.MethodConfig.NewCode(0,
                    treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(
                                    treeMaker.Ident(logObjName), slf4jMethodName),
                            List.from(methodConfig.getLogContent().getLogParams(Tree.Kind.METHOD,
                                    lineMap.getLineNumber(methodBody.getStartPosition()),
                                    "Start!", null, treeMaker, names)))));

            factory.get(Tree.Kind.BLOCK).process(methodBody);

            methodBody.stats = attachCode(methodBody.stats, startNewCode);

            // Add try-catch statement.
            if (methodConfig.isExceptionLog()) {

                LinkedList<JCTree.JCExpression> logParams = methodConfig.getLogContent()
                        .getLogParams(Tree.Kind.TRY,
                                lineMap.getLineNumber(methodBody.getStartPosition()),
                                "Error!", null, treeMaker, names);
                Name e = names.fromString("e");
                JCTree.JCIdent eIdent = treeMaker.Ident(e);
                logParams.add(eIdent);

                JCTree.JCCatch jcCatch = treeMaker.Catch(treeMaker.VarDef(treeMaker.Modifiers(0), e,
                                treeMaker.Ident(names.fromString("Exception")), null),
                        treeMaker.Block(0L, List.of(treeMaker.Exec(treeMaker.Apply(List.nil(),
                                        treeMaker.Select(treeMaker.Ident(logObjName), slf4jMethodName),
                                        List.from(logParams))),
                                treeMaker.Throw(eIdent))));

                methodBody.stats = List.of(treeMaker.Try(treeMaker.Block(methodBody.flags, methodBody.stats),
                        List.of(jcCatch), null));
            }
        } finally {
            methodConfig.getBlockStack().pop();
        }
    }
}
