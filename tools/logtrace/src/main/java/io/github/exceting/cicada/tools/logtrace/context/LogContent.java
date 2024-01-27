package io.github.exceting.cicada.tools.logtrace.context;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is responsible for generating log output statements.
 * eg: log.debug("xxxxx")
 */
public class LogContent {
    private final String head;
    /**
     * Slf4j log level.
     * See: {@link org.slf4j.event.Level}
     */
    private final String traceLevel;
    /**
     * See {@link io.github.exceting.cicada.tools.logtrace.annos.MethodLog#isOpen()}.
     */
    private final String mIsOpen;
    private final StringBuilder paramContent = new StringBuilder();
    private final LinkedList<JCTree.JCExpression> params = new LinkedList<>();

    LogContent(String methodName, String traceLevel, String mIsOpen, Map<String, JCTree.JCExpression> argMap) {
        this.head = String.format("LOG_TRACE >>>>>> OUTPUT: [METHOD: %s]", methodName);
        this.traceLevel = traceLevel;
        this.mIsOpen = mIsOpen;
        if (argMap != null && !argMap.isEmpty()) {
            processParams(paramContent, params, argMap);
        }
    }

    public JCTree.JCStatement getNewCodeStatement(Tree.Kind kind,
                                                  JCTree jcTree,
                                                  String content,
                                                  Map<String, JCTree.JCExpression> newParams,
                                                  TreeMaker treeMaker, Names names) {
        return getNewCodeStatement(kind, jcTree, content, newParams, treeMaker, names, null);
    }

    public JCTree.JCStatement getNewCodeStatement(Tree.Kind kind,
                                                  JCTree jcTree,
                                                  String content,
                                                  Map<String, JCTree.JCExpression> newParams,
                                                  TreeMaker treeMaker, Names names, String traceLevel) {
        Position.LineMap lineMap = LogTraceContext.lineMap.get();
        JCTree.JCStatement statement = treeMaker.Exec(treeMaker.Apply(com.sun.tools.javac.util.List.nil(), treeMaker.Select(
                        treeMaker.Ident(names.fromString(LogTraceContext.currentLogIdentName.get())),
                        getSlf4jMethod(traceLevel == null ? this.traceLevel : traceLevel, names)),
                com.sun.tools.javac.util.List.from(getLogParams(kind, lineMap.getLineNumber(jcTree.getStartPosition()),
                        content, newParams, treeMaker, names))));

        Map<String, String> isOpens = LogTraceContext.allIsOpenMap.get();
        if (isOpens == null) {
            return statement;
        }
        String isOpen = this.mIsOpen == null ? null : isOpens.get(this.mIsOpen);
        if (isOpen == null || isOpen.isEmpty()) {
            String cIsOpen = LogTraceContext.classIsOpenFieldName.get();
            if (cIsOpen != null) {
                isOpen = isOpens.get(cIsOpen);
            }
        }
        if (isOpen != null && !isOpen.isEmpty()) {
            statement = treeMaker.If(treeMaker.Apply(com.sun.tools.javac.util.List.nil(),
                            treeMaker.Select(treeMaker.Ident(names.fromString(isOpen)),
                                    names.fromString("get")), com.sun.tools.javac.util.List.nil()),
                    statement, null);
        }
        return statement;
    }

    public LinkedList<JCTree.JCExpression> getLogParams(Tree.Kind kind,
                                                        int line,
                                                        String content,
                                                        Map<String, JCTree.JCExpression> newParams,
                                                        TreeMaker treeMaker, Names names) {

        StringBuilder sb = new StringBuilder();
        sb.append(this.head).append("[").append(kind).append("]").append("[LINE: ")
                .append(line).append("] ").append(content);

        StringBuilder currentParamContent = new StringBuilder(paramContent);
        LinkedList<JCTree.JCExpression> currentParams = new LinkedList<>(params);

        if (newParams != null && !newParams.isEmpty()) {
            if (!currentParams.isEmpty()) {
                currentParamContent.append(", ");
            }
            processParams(currentParamContent, currentParams, newParams);
        }

        LinkedList<JCTree.JCExpression> result = new LinkedList<>();
        if (!currentParams.isEmpty()) {
            sb.append(" Data: ").append(currentParamContent);
            result.add(treeMaker.NewArray(treeMaker.Ident(names.fromString("Object")),
                    com.sun.tools.javac.util.List.nil(),
                    com.sun.tools.javac.util.List.from(currentParams)));
        }
        result.addFirst(treeMaker.Literal(sb.toString()));
        return result;
    }

    private void processParams(StringBuilder paramContent, LinkedList<JCTree.JCExpression> params,
                               Map<String, JCTree.JCExpression> paramMap) {
        AtomicInteger i = new AtomicInteger();
        JCTree.JCExpression exp = paramMap.remove(null);
        paramMap.forEach((k, v) -> {
            i.incrementAndGet();
            paramContent.append(k).append(" = ");
            if (i.get() == paramMap.size()) {
                paramContent.append("{}");
            } else {
                paramContent.append("{}, ");
            }
            params.add(v);
        });
        if (exp != null) {
            params.add(exp);
        }
    }

    private Name getSlf4jMethod(String level, Names names) {
        switch (level) {
            case "Level.ERROR":
                return names.fromString("error");
            case "Level.WARN":
                return names.fromString("warn");
            case "Level.INFO":
                return names.fromString("info");
            case "Level.DEBUG":
                return names.fromString("debug");
            case "Level.TRACE":
                return names.fromString("trace");
            default:
                return null;
        }
    }
}
