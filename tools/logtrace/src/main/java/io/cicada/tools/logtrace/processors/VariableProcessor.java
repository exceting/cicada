package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.AnnoProcessor;

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
            AnnoProcessor.MethodConfig.OldCode oldCode = AnnoProcessor.currentMethodConfig.get().getBlockStack().peek();
            if (oldCode != null) {
                AnnoProcessor.MethodConfig methodConfig = AnnoProcessor.currentMethodConfig.get();
                Map<String, JCTree.JCExpression> newArgs = new HashMap<>();
                newArgs.put(jcVariableDecl.getName().toString(), treeMaker.Ident(jcVariableDecl.getName()));

                oldCode.addNewCode(new AnnoProcessor.MethodConfig.NewCode(oldCode.getOffset() + 1,
                        treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(
                                treeMaker.Ident(names.fromString(AnnoProcessor.currentLogIdentName.get())),
                                getSlf4jMethod(methodConfig.getTraceLevel())), List.from(
                                methodConfig.getLogContent().getLogParams(Tree.Kind.VARIABLE,
                                        AnnoProcessor.lineMap.get().getLineNumber(jcVariableDecl.getStartPosition()),
                                        "", newArgs, treeMaker))))));
            }
        }
    }
}
