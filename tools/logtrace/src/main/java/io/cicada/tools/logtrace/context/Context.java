package io.cicada.tools.logtrace.context;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Position;
import io.cicada.tools.logtrace.processors.RootProcessor;

import javax.lang.model.element.Element;
import java.util.*;

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
        private final boolean arrayToSize;

        private final Stack<OriginCode> blockStack = new Stack<>(); // Method stack.

        public MethodConfig(String methodName, Map<String, JCTree.JCExpression> argMap, boolean exceptionLog,
                            boolean banLoop, String traceLevel, boolean arrayToSize) {
            this.logContent = new LogContent(methodName, traceLevel, argMap);
            this.exceptionLog = exceptionLog;
            this.banLoop = banLoop;
            this.arrayToSize = arrayToSize;
        }

        public boolean isExceptionLog() {
            return exceptionLog;
        }

        public boolean isBanLoop() {
            return banLoop;
        }

        public Stack<OriginCode> getBlockStack() {
            return blockStack;
        }

        public LogContent getLogContent() {
            return logContent;
        }

        public boolean isArrayToSize() {
            return arrayToSize;
        }

        public static class OriginCode {
            private int offset = 0;
            // Need add to current block when pop stack.
            private final List<NewCode> newCodes = new ArrayList<>();
            private final JCTree.JCBlock block;

            public OriginCode(JCTree.JCBlock block) {
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

}