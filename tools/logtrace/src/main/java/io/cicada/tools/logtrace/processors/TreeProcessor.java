package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.context.Context;

import java.util.UUID;

public abstract class TreeProcessor {
    ProcessorFactory factory;
    JavacTrees javacTrees;
    TreeMaker treeMaker;
    Names names;

    public void process() {
        // do nothing
    }

    /**
     * Process single JcTree
     */
    public void process(JCTree jcTree) {
        // do nothing
    }

    /**
     * Process JCTrees.
     *
     * @param jcTrees Tree array
     */
    public void process(JCTree... jcTrees) {
        // do nothing
    }

    public TreeProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        this.factory = factory;
        this.javacTrees = javacTrees;
        this.treeMaker = treeMaker;
        this.names = names;
    }


    /**
     * Generate new code.
     *
     * @param stats     original code
     * @param generated new code
     * @return The new method statements which contains new code.
     */
    public List<JCTree.JCStatement> generateCode(List<JCTree.JCStatement> stats,
                                                 Context.MethodConfig.NewCode generated) {
        return generateCode(stats, List.of(generated));
    }

    public List<JCTree.JCStatement> generateCode(List<JCTree.JCStatement> stats,
                                                 java.util.List<Context.MethodConfig.NewCode> generated) {
        return generateCode(stats, List.from(generated));
    }

    public List<JCTree.JCStatement> generateCode(List<JCTree.JCStatement> stats,
                                                 List<Context.MethodConfig.NewCode> generated) {

        if (generated == null || generated.size() == 0) {
            return stats;
        }

        List<JCTree.JCStatement> result = List.nil();
        int offset = 0;

        for (Context.MethodConfig.NewCode newCode : generated) {
            int newOffset = newCode.getOffset();
            while (offset < newOffset) {
                result = result.append(stats.get(offset));
                offset++;
            }
            result = result.append(newCode.getStatement());
        }

        while (offset < stats.size()) {
            result = result.append(stats.get(offset));
            offset++;
        }

        return result;
    }

    public String getAnnoAttrValue(JCTree.JCAnnotation anno, String attrName) {
        if (anno.getArguments() == null || anno.getArguments().size() == 0) {
            return null;
        }
        for (JCTree.JCExpression arg : anno.getArguments()) {
            if (!(arg instanceof JCTree.JCAssign)) {
                continue;
            }
            JCTree.JCAssign assign = (JCTree.JCAssign) arg;
            if (attrName.equals(assign.lhs.toString())) {
                return assign.rhs.toString();
            }
        }
        return null;
    }

    /**
     * Generate new variable names; Use UUID to avoid conflicts.
     */
    public String getNewVarName(String head) {
        return String.format("%s%s", head, UUID.randomUUID()).replace("-", "_");
    }

    public ProcessorFactory getFactory() {
        return factory;
    }

    public JavacTrees getJavacTrees() {
        return javacTrees;
    }

    public TreeMaker getTreeMaker() {
        return treeMaker;
    }

    public Names getNames() {
        return names;
    }
}
