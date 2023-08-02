package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
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
            for (JCTree.JCCase jcCase : jcSwitch.cases) {
                jcCase.stats = attachCode(jcCase.stats, new Context.MethodConfig.NewCode(0,
                        methodConfig.getLogContent()
                                .getNewCodeStatement(Tree.Kind.SWITCH, jcCase,
                                        String.format("Switch%s case %s is true!",
                                                jcSwitch.selector,
                                                jcCase.getExpression() == null ? "default" : jcCase.getExpression()),
                                        null, treeMaker, names)));
            }
        }

    }
}
