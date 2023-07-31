package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position;
import io.cicada.tools.logtrace.context.Context;

/**
 * A processor for handling IF statement.
 * eg:
 * if(cond) {
 * // ...
 * } else if(cond2) {
 * // ...
 * } else{
 * // ...
 * }
 */
public class IfProcessor extends TreeProcessor {

    IfProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCIf) && !(jcTree instanceof JCTree.JCBlock)) {
            return;
        }
        Context.MethodConfig methodConfig = Context.currentMethodConfig.get();
        Position.LineMap lineMap = Context.lineMap.get();
        if (jcTree instanceof JCTree.JCIf) {
            JCTree.JCIf jcIf = (JCTree.JCIf) jcTree;
            // TODO The cond may should be processed.
            // factory.get(ProcessorFactory.Kind.IF_COND).process(jcIf.cond);
            Context.MethodConfig.NewCode newCode = new Context.MethodConfig.NewCode(0,
                    treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(
                            treeMaker.Ident(names.fromString(Context.currentLogIdentName.get())),
                            getSlf4jMethod(methodConfig.getTraceLevel())), List.from(
                            methodConfig.getLogContent().getLogParams(Tree.Kind.IF,
                                    lineMap.getLineNumber(jcIf.getStartPosition()),
                                    String.format("The condition: %s is TRUE!", jcIf.cond),
                                    null, treeMaker)))));
            factory.get(Tree.Kind.BLOCK).process(jcIf.thenpart);
            if (jcIf.thenpart instanceof JCTree.JCBlock) {
                JCTree.JCBlock then = (JCTree.JCBlock) jcIf.thenpart;
                then.stats = attachCode(then.stats, newCode);
            }
            process(jcIf.elsepart);
        } else {
            Context.MethodConfig.NewCode newCode = new Context.MethodConfig.NewCode(0,
                    treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(
                            treeMaker.Ident(names.fromString(Context.currentLogIdentName.get())),
                            getSlf4jMethod(methodConfig.getTraceLevel())), List.from(
                            methodConfig.getLogContent().getLogParams(Tree.Kind.IF,
                                    lineMap.getLineNumber(jcTree.getStartPosition()),
                                    "The condition: ELSE is TRUE!",
                                    null, treeMaker)))));
            factory.get(Tree.Kind.BLOCK).process(jcTree);
            JCTree.JCBlock elsePart = (JCTree.JCBlock) jcTree;
            elsePart.stats = attachCode(elsePart.stats, newCode);
        }
    }
}
