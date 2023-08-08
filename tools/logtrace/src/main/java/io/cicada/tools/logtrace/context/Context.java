package io.cicada.tools.logtrace.context;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Position;
import io.cicada.tools.logtrace.processors.custom.MethodProcessor;
import io.cicada.tools.logtrace.processors.custom.ClassProcessor;

import javax.lang.model.element.Element;
import java.util.*;

public class Context {

    /**
     * The code line map.
     * Refresh in {@link ClassProcessor#process()}
     */
    public static final ThreadLocal<Position.LineMap> lineMap = new ThreadLocal<>();

    /**
     * The log obj ident.
     * Refresh in {@link ClassProcessor#process()}
     */
    public static final ThreadLocal<String> currentLogIdentName = new ThreadLocal<>();

    /**
     * Master switch obj ident.
     * Refresh in {@link ClassProcessor#process()}
     */
    public static final ThreadLocal<String> currentIsOpenIdentName = new ThreadLocal<>();

    /**
     * The method annotation config.
     * Refresh in {@link MethodProcessor#process(JCTree)}
     */
    public static final ThreadLocal<MethodConfig> currentMethodConfig = new ThreadLocal<>();

    public static final ThreadLocal<Element> currentElement = new ThreadLocal<>();

    public static class MethodConfig {
        private final LogContent logContent;

        private final boolean onlyVar;

        private final Stack<OriginCode> blockStack = new Stack<>(); // Method stack.

        public MethodConfig(String methodName, Map<String, JCTree.JCExpression> argMap, String traceLevel, boolean onlyVar) {
            this.logContent = new LogContent(methodName, traceLevel, argMap);
            this.onlyVar = onlyVar;
        }

        public Stack<OriginCode> getBlockStack() {
            return blockStack;
        }

        public LogContent getLogContent() {
            return logContent;
        }

        public boolean isOnlyVar() {
            return onlyVar;
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
