package io.cicada.tools.logtrace;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position;
import io.cicada.tools.logtrace.annos.Slf4jCheck;
import io.cicada.tools.logtrace.processors.ProcessorFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.*;

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
        private final boolean exceptionLog;
        private final boolean traceLoop;

        /**
         * Slf4j log level.
         * See: {@link org.slf4j.event.Level}
         */
        private final String traceLevel;

        private final Stack<JCTree.JCBlock> blockStack = new Stack<>(); // Method block stack.

        public MethodConfig(String methodName, boolean exceptionLog, boolean traceLoop, String traceLevel) {
            this.methodName = methodName;
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

        public Stack<JCTree.JCBlock> getBlockStack() {
            return blockStack;
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
