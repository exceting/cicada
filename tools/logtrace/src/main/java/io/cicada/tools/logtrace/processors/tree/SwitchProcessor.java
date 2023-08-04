package io.cicada.tools.logtrace.processors.tree;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.context.Context;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#SWITCH}.
 * eg:
 * <pre>
 *     switch ( expression ) {
 *       cases
 *     }
 * </pre>
 */
public class SwitchProcessor extends TreeProcessor {

    public SwitchProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCSwitch)) {
            return;
        }
        JCTree.JCSwitch jcSwitch = (JCTree.JCSwitch) jcTree;
        if (jcSwitch.getExpression() != null) {
            getFactory().get(jcSwitch.getExpression().getKind()).process(jcSwitch.getExpression());
        }
        if (jcSwitch.getCases() != null && jcSwitch.getCases().size() > 0) {
            Context.MethodConfig methodConfig = Context.currentMethodConfig.get();
            for (JCTree.JCCase jcCase : jcSwitch.getCases()) {
                jcCase.stats = attachCode(jcCase.stats, new Context.MethodConfig.NewCode(0,
                        methodConfig.getLogContent()
                                .getNewCodeStatement(Tree.Kind.SWITCH, jcCase,
                                        String.format("Switch%s case %s is true!",
                                                jcSwitch.selector,
                                                jcCase.getExpression() == null ? "default" : jcCase.getExpression()),
                                        null, getTreeMaker(), getNames())));
            }
        }

    }
}
