package io.ginkgo.tools.logtrace.processors;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A processor for handling IF statement.
 */
public class IfProcessor {
    Logger logger = LoggerFactory.getLogger(IfProcessor.class);
    public static void process(TreeMaker treeMaker, Names names, JCTree.JCStatement statement) {
        if (!(statement instanceof JCTree.JCIf)) {
            return;
        }
        JCTree.JCIf jcIf = (JCTree.JCIf) statement;
        System.out.printf("cond = %s\n\nthen = %s", jcIf.cond, jcIf.thenpart);
        System.out.println("============================");
        ExpressionProcessor.process(jcIf.cond);
        process(treeMaker, names, jcIf.elsepart);
    }
}
