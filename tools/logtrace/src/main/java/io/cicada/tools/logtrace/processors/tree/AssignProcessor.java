package io.cicada.tools.logtrace.processors.tree;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.context.Context;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

public class AssignProcessor extends TreeProcessor {

    public AssignProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCAssign)) {
            return;
        }
        JCTree.JCAssign assign = (JCTree.JCAssign) jcTree;
        if (assign.getVariable() != null) {

            Context.MethodConfig methodConfig = Context.currentMethodConfig.get();
            Context.MethodConfig.OriginCode originCode = methodConfig.getBlockStack().peek();
            String varName = assign.getVariable().toString();
            if (originCode.getVars().containsKey(varName)) {
                System.out.println("========  " + assign.getVariable().getKind() + "    " + assign.getVariable());
                VariableProcessor.attachVarLog(varName, originCode.getVars().get(varName).isDur(), methodConfig,
                        originCode, assign.getVariable(), getTreeMaker(), getNames());
            }
        }
        if (assign.getExpression() != null) {
            getFactory().get(assign.getExpression().getKind()).process(assign.getExpression());
        }
    }
}
