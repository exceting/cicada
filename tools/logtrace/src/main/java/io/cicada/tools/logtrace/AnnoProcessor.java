package io.cicada.tools.logtrace;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Names;
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
}
