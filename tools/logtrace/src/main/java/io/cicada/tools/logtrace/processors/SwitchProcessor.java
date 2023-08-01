package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position;
import io.cicada.tools.logtrace.context.Context;

public class SwitchProcessor extends TreeProcessor {

    SwitchProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCSwitch)) {
            return;
        }
        JCTree.JCSwitch jcSwitch = (JCTree.JCSwitch) jcTree;
        // System.out.println("------"+jcSwitch.cases+"       "+jcSwitch.selector);
        if (jcSwitch.cases != null && jcSwitch.cases.size() > 0) {
            Context.MethodConfig methodConfig = Context.currentMethodConfig.get();
            Position.LineMap lineMap = Context.lineMap.get();
            for (JCTree.JCCase jcCase : jcSwitch.cases) {
                jcCase.stats = attachCode(jcCase.stats, new Context.MethodConfig.NewCode(0,
                        treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(
                                        treeMaker.Ident(names.fromString(Context.currentLogIdentName.get())),
                                        getSlf4jMethod(methodConfig.getTraceLevel())),
                                List.from(methodConfig.getLogContent().getLogParams(Tree.Kind.SWITCH,
                                        lineMap.getLineNumber(jcCase.getStartPosition()),
                                        String.format("Switch%s case %s is true!",
                                                jcSwitch.selector,
                                                jcCase.getExpression() == null ? "default" : jcCase.getExpression()),
                                        null, treeMaker, names))))));
            }
        }

    }
}
