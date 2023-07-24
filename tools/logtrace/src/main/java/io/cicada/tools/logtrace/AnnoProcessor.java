package io.cicada.tools.logtrace;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.annos.Slf4jCheck;
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import org.slf4j.event.Level;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnnoProcessor extends AbstractProcessor {
    private ProcessorFactory factory;

    /**
     * 初始化处理器
     *
     * @param processingEnv 提供了一系列的实用工具
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.factory = new ProcessorFactory(JavacTrees.instance(processingEnv),
                TreeMaker.instance(context),
                Names.instance(context));
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
                factory.get(ProcessorFactory.Kind.ROOT).process(e);
            }
        }
        return true;
    }

    public static final ThreadLocal<GlobalConfig> config = new ThreadLocal<>();

    public static class GlobalConfig {
        private final String logIdentName;

        private final Map<JCTree.JCMethodDecl, MethodConfig> methodConfigMap = new ConcurrentHashMap<>();

        public GlobalConfig(String logIdentName) {
            this.logIdentName = logIdentName;
        }

        public String getLogIdentName() {
            return logIdentName;
        }

        public Map<JCTree.JCMethodDecl, MethodConfig> getMethodConfigMap() {
            return methodConfigMap;
        }

        public static class MethodConfig {
            private final boolean exceptionLog;
            private final boolean traceLoop;
            private final Level traceLevel;

            public MethodConfig(boolean exceptionLog, boolean traceLoop, Level traceLevel) {
                this.exceptionLog = exceptionLog;
                this.traceLoop = traceLoop;
                this.traceLevel = traceLevel;
            }

            public boolean isExceptionLog() {
                return exceptionLog;
            }

            public Level getTraceLevel() {
                return traceLevel;
            }
        }
    }
}
