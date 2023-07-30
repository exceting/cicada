package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position;
import io.cicada.tools.logtrace.AnnoProcessor;

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
        AnnoProcessor.MethodConfig methodConfig = AnnoProcessor.currentMethodConfig.get();
        Position.LineMap lineMap = AnnoProcessor.lineMap.get();
        StringBuilder logMsg = new StringBuilder();
        if (jcTree instanceof JCTree.JCIf) {
            JCTree.JCIf jcIf = (JCTree.JCIf) jcTree;
            // TODO The cond may should be processed.
            // factory.get(ProcessorFactory.Kind.IF_COND).process(jcIf.cond);
            AnnoProcessor.MethodConfig.NewCode newCode = new AnnoProcessor.MethodConfig.NewCode(0, buildNewCode(methodConfig, logMsg
                    .append(String.format(PREFIX, methodConfig.getMethodName(),
                            ProcessorFactory.Kind.IF_STATEMENT,
                            lineMap.getLineNumber(jcIf.getStartPosition())))
                    .append("The condition: ")
                    .append(jcIf.cond)
                    .append(" is TRUE!")
                    .toString()));
            factory.get(ProcessorFactory.Kind.BLOCK).process(jcIf.thenpart);
            if (jcIf.thenpart instanceof JCTree.JCBlock) {
                JCTree.JCBlock then = (JCTree.JCBlock) jcIf.thenpart;
                then.stats = attachCode(then.stats, newCode);
            }
            process(jcIf.elsepart);
        } else {
            AnnoProcessor.MethodConfig.NewCode newCode = new AnnoProcessor.MethodConfig.NewCode(0, buildNewCode(methodConfig, logMsg
                    .append(String.format(PREFIX, methodConfig.getMethodName(),
                            ProcessorFactory.Kind.IF_STATEMENT,
                            lineMap.getLineNumber(jcTree.getStartPosition())))
                    .append("The condition: ")
                    .append("ELSE")
                    .append(" is TRUE!")
                    .toString()));
            factory.get(ProcessorFactory.Kind.BLOCK).process(jcTree);
            JCTree.JCBlock elsePart = (JCTree.JCBlock) jcTree;
            elsePart.stats = attachCode(elsePart.stats, newCode);
        }
    }

    private JCTree.JCStatement buildNewCode(AnnoProcessor.MethodConfig methodConfig, String msg) {
        return treeMaker.Exec(treeMaker.Apply(List.nil(), treeMaker.Select(
                treeMaker.Ident(names.fromString(AnnoProcessor.currentLogIdentName.get())),
                getSlf4jMethod(methodConfig.getTraceLevel())), List.of(treeMaker.Literal(msg))));
    }
}
