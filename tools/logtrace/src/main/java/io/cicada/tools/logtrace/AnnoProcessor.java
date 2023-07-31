package io.cicada.tools.logtrace;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position;
import io.cicada.tools.logtrace.annos.Ban;
import io.cicada.tools.logtrace.annos.Slf4jCheck;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AnnoProcessor extends AbstractProcessor {
    private ProcessorFactory factory;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.factory = new ProcessorFactory(JavacTrees.instance(processingEnv), TreeMaker.instance(context), Names.instance(context));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        set.add(Slf4jCheck.class.getName());
        return set;
    }

    /**
     * Attach trace log code.
     *
     * @param annotations the annotation types requested to be processed.
     * @param roundEnv    environment for information about the current and prior round.
     * @return whether the set of annotation types are claimed by this processor.
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement t : annotations) {
            for (Element e : roundEnv.getElementsAnnotatedWith(t)) {
                currentElement.set(e);
                factory.get(ProcessorFactory.Kind.ROOT).process();
                currentElement.remove();
            }
        }
        return true;
    }

    /**
     * The code line map.
     * Refresh in {@link io.cicada.tools.logtrace.processors.RootProcessor}
     */
    public static final ThreadLocal<Position.LineMap> lineMap = new ThreadLocal<>();

    /**
     * The log obj ident.
     * Refresh in {@link io.cicada.tools.logtrace.processors.RootProcessor}
     */
    public static final ThreadLocal<String> currentLogIdentName = new ThreadLocal<>();

    /**
     * The method annotation config.
     * Refresh in {@link io.cicada.tools.logtrace.processors.ClassProcessor}
     */
    public static final ThreadLocal<MethodConfig> currentMethodConfig = new ThreadLocal<>();

    public static final ThreadLocal<Element> currentElement = new ThreadLocal<>();

    public static class MethodConfig {
        private final String methodName;
        private final LogContent logContent;
        private final boolean exceptionLog;
        private final boolean traceLoop;

        /**
         * Slf4j log level.
         * See: {@link org.slf4j.event.Level}
         */
        private final String traceLevel;

        private final Stack<OldCode> blockStack = new Stack<>(); // Method block stack.

        public MethodConfig(String methodName, Map<String, JCTree.JCExpression> argMap,
                            boolean exceptionLog, boolean traceLoop, String traceLevel) {
            this.methodName = methodName;
            this.logContent = new LogContent(methodName, argMap);
            this.exceptionLog = exceptionLog;
            this.traceLoop = traceLoop;
            this.traceLevel = traceLevel;
        }

        public boolean isExceptionLog() {
            return exceptionLog;
        }

        public String getTraceLevel() {
            return traceLevel;
        }

        public boolean isTraceLoop() {
            return traceLoop;
        }

        public String getMethodName() {
            return methodName;
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
        private final StringBuilder paramContent = new StringBuilder();
        private final LinkedList<JCTree.JCExpression> params = new LinkedList<>();

        private LogContent(String methodName, Map<String, JCTree.JCExpression> argMap) {
            this.head = String.format("LOG_TRACE >>>>>> OUTPUT: [METHOD: %s]", methodName);
            if (argMap != null && argMap.size() > 0) {
                processParams(paramContent, params, argMap);
            }
        }

        public LinkedList<JCTree.JCExpression> getLogParams(Tree.Kind kind,
                                                            int line,
                                                            String content,
                                                            Map<String, JCTree.JCExpression> newParams,
                                                            TreeMaker treeMaker) {
            LinkedList<JCTree.JCExpression> result = new LinkedList<>();

            StringBuilder sb = new StringBuilder();
            sb.append(this.head).append("[").append(kind).append("]").append("[LINE: ")
                    .append(line).append("] ").append(content);

            StringBuilder currentParamContent = new StringBuilder(paramContent);
            LinkedList<JCTree.JCExpression> currentParams = new LinkedList<>(params);

            if (newParams != null && newParams.size() > 0) {
                currentParamContent.append(", ");
                processParams(currentParamContent, currentParams, newParams);
            }

            if (currentParams.size() > 0) {
                result.add(treeMaker.Literal(sb.append(" Params: ").append(currentParamContent).toString()));
                result.addAll(currentParams);
            }
            return result;
        }

        private void processParams(StringBuilder paramContent, LinkedList<JCTree.JCExpression> params,
                                   Map<String, JCTree.JCExpression> paramMap) {
            AtomicInteger i = new AtomicInteger();
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
        }
    }
}
