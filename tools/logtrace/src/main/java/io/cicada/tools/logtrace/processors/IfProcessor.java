package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.AnnoProcessor;

/**
 * A processor for handling IF statement.
 */
public class IfProcessor extends TreeProcessor {

    IfProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCIf)) {
            return;
        }
        JCTree.JCIf jcIf = (JCTree.JCIf) jcTree;
        // TODO The cond maybe should be processed in detail.
        // factory.get(ProcessorFactory.Kind.IF_COND).process(jcIf.cond);
        AnnoProcessor.MethodConfig methodConfig = AnnoProcessor.currentMethodConfig.get();
        methodConfig.getAttachStack().push(treeMaker.Exec(treeMaker.Apply(List.nil(),
                treeMaker.Select(treeMaker.Ident(names.fromString(AnnoProcessor.currentLogIdentName.get())),
                        getSlf4jMethod(methodConfig.getTraceLevel())), List.of(treeMaker.Literal(
                        String.format(PREFIX, methodConfig.getMethodName(),
                                ProcessorFactory.Kind.IF_STATEMENT)
                                + "The condition: "
                                + jcIf.cond
                                + " is TRUE!")))));
        factory.get(ProcessorFactory.Kind.BLOCK).process(jcIf.thenpart);
        process(jcIf.elsepart);
    }
}
