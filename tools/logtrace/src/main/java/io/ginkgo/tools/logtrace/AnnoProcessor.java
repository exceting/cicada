package io.ginkgo.tools.logtrace;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Names;
import io.ginkgo.tools.logtrace.annos.LogTrace;
import io.ginkgo.tools.logtrace.annos.Slf4jCheck;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.*;
import java.util.stream.Collectors;

public class AnnoProcessor extends AbstractProcessor {

    static final String LOMBOK_PACK = "lombok.extern.slf4j.Slf4j";

    static final String SLF4J_PACK = "org.slf4j.Logger";

    static final String LOG_TRACE = LogTrace.class.getName();

    private JavacTrees javacTrees;
    private Trees trees;
    private TreeMaker treeMaker;
    private Names names;
    private ProcessingEnvironment processingEnv;

    /**
     * 初始化处理器
     *
     * @param processingEnv 提供了一系列的实用工具
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
        this.javacTrees = JavacTrees.instance(processingEnv);
        this.trees = Trees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
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

                final TreePath treePath = trees.getPath(e);
                final CompilationUnitTree unitTree = treePath.getCompilationUnit();
                final List<JCTree.JCImport> imports = (List<JCTree.JCImport>) unitTree.getImports();
                final JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) javacTrees.getTree(e);
                final List<JCTree> defs = classDecl.defs; // Methods and fields
                final List<JCTree.JCAnnotation> annos = classDecl.mods.annotations; // All annotations of this Class

                final List<String> importClasses = imports.stream()
                        .map(imp -> imp.qualid.type.toString())
                        .collect(Collectors.toList());

                final List<String> annoClasses = annos.stream()
                        .map(imp -> imp.annotationType.type.toString())
                        .collect(Collectors.toList());

                List<String> slf4jObjs = defs.stream()
                        .filter(def -> def instanceof JCTree.JCVariableDecl)
                        .map(def -> (JCTree.JCVariableDecl) def)
                        .filter(def -> SLF4J_PACK.equals(def.getType().type.toString()))
                        .map(def -> def.getName().toString())
                        .collect(Collectors.toList());

                String logObj;
                // Support lombok
                if (importClasses.contains(LOMBOK_PACK) && annoClasses.contains(LOMBOK_PACK)) {
                    logObj = "log";
                } else if (importClasses.contains(SLF4J_PACK) && slf4jObjs.size() > 0) {
                    logObj = slf4jObjs.get(0); // Default to use the first one.
                } else { // Else, new slf4j logger object.

                }


                // Filter out methods with @LogTrace annotations and process them
                defs.stream().filter(def -> def instanceof JCTree.JCMethodDecl)
                        .map(def -> (JCTree.JCMethodDecl) def)
                        .filter(def -> def.getModifiers().annotations != null && def.getModifiers().annotations.size() > 0)
                        .filter(def -> {
                            for (JCTree.JCAnnotation anno : def.getModifiers().annotations) {
                                System.out.println("++++++++++++++++  " + anno.getAnnotationType().type.toString());
                                if (LOG_TRACE.equals(anno.getAnnotationType().type.toString())) {
                                    return true;
                                }
                            }
                            return false;
                        }).collect(Collectors.toList()).forEach(methodDecl -> {
                            System.out.println("============ 方法 = " + methodDecl.getName().toString());
                        });
            }
        }
        return true;
    }
}
