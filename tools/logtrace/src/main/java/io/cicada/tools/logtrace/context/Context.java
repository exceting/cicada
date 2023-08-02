package io.cicada.tools.logtrace.context;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position;
import io.cicada.tools.logtrace.processors.RootProcessor;

import javax.lang.model.element.Element;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Context {

    /**
     * The code line map.
     * Refresh in {@link RootProcessor#process()}
     */
    public static final ThreadLocal<Position.LineMap> lineMap = new ThreadLocal<>();

    /**
     * The log obj ident.
     * Refresh in {@link RootProcessor#process()}
     */
    public static final ThreadLocal<String> currentLogIdentName = new ThreadLocal<>();

    /**
     * Master switch obj ident.
     * Refresh in {@link RootProcessor#process()}
     */
    public static final ThreadLocal<String> currentIsOpenIdentName = new ThreadLocal<>();

    /**
     * The method annotation config.
     * Refresh in {@link io.cicada.tools.logtrace.processors.ClassProcessor#process(JCTree)}
     */
    public static final ThreadLocal<MethodConfig> currentMethodConfig = new ThreadLocal<>();

    public static final ThreadLocal<Element> currentElement = new ThreadLocal<>();

    public static class MethodConfig {
        private final LogContent logContent;
        private final boolean exceptionLog;
        private final boolean banLoop;

        private final Stack<OldCode> blockStack = new Stack<>(); // Method block stack.

        public MethodConfig(String methodName, Map<String, JCTree.JCExpression> argMap, boolean exceptionLog,
                            boolean banLoop, String traceLevel) {
            this.logContent = new LogContent(methodName, traceLevel, argMap);
            this.exceptionLog = exceptionLog;
            this.banLoop = banLoop;
        }

        public boolean isExceptionLog() {
            return exceptionLog;
        }

        public boolean isBanLoop() {
            return banLoop;
        }

        public Stack<OldCode> getBlockStack() {
            return blockStack;
        }

        public LogContent getLogContent() {
            return logContent;
        }

        public static class OldCode {
            private int offset = 0;
            private final List<NewCode> newCodes = new ArrayList<>();
            private final JCTree.JCBlock block;

            public OldCode(JCTree.JCBlock block) {
                this.block = block;
            }

            public void incrOffset() {
                offset++;
            }

            public void addNewCode(NewCode newCode) {
                newCodes.add(newCode);
            }

            public int getOffset() {
                return offset;
            }

            public JCTree.JCBlock getBlock() {
                return block;
            }

            public List<NewCode> getNewCodes() {
                return newCodes;
            }
        }

        public static class NewCode {
            private final int offset;
            private final JCTree.JCStatement statement;

            public NewCode(int offset, JCTree.JCStatement statement) {
                this.offset = offset;
                this.statement = statement;
            }

            public int getOffset() {
                return offset;
            }

            public JCTree.JCStatement getStatement() {
                return statement;
            }
        }
    }

    public static class LogContent {
        private final String head;
        /**
         * Slf4j log level.
         * See: {@link org.slf4j.event.Level}
         */
        private final String traceLevel;
        private final StringBuilder paramContent = new StringBuilder();
        private final LinkedList<JCTree.JCExpression> params = new LinkedList<>();

        private LogContent(String methodName, String traceLevel, Map<String, JCTree.JCExpression> argMap) {
            this.head = String.format("LOG_TRACE >>>>>> OUTPUT: [METHOD: %s]", methodName);
            this.traceLevel = traceLevel;
            if (argMap != null && argMap.size() > 0) {
                processParams(paramContent, params, argMap);
            }
        }

        public JCTree.JCStatement getNewCodeStatement(Tree.Kind kind,
                                                      JCTree jcTree,
                                                      String content,
                                                      Map<String, JCTree.JCExpression> newParams,
                                                      TreeMaker treeMaker, Names names) {
            Position.LineMap lineMap = Context.lineMap.get();
            JCTree.JCStatement statement = treeMaker.Exec(treeMaker.Apply(com.sun.tools.javac.util.List.nil(), treeMaker.Select(
                    treeMaker.Ident(names.fromString(Context.currentLogIdentName.get())),
                    getSlf4jMethod(traceLevel, names)), com.sun.tools.javac.util.List.from(
                    getLogParams(kind, lineMap.getLineNumber(jcTree.getStartPosition()),
                            content, newParams, treeMaker, names))));

            String isOpen = currentIsOpenIdentName.get();
            if (isOpen != null && !isOpen.equals("")) {
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

            if (newParams != null && newParams.size() > 0) {
                if (currentParams.size() > 0) {
                    currentParamContent.append(", ");
                }
                processParams(currentParamContent, currentParams, newParams);
            }

            LinkedList<JCTree.JCExpression> result = new LinkedList<>();
            if (currentParams.size() > 0) {
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

}
