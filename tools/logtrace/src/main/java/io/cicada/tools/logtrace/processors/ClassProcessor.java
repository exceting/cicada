package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.annos.LogTrace;

import java.util.Objects;
import java.util.stream.Collectors;

public class ClassProcessor extends TreeProcessor {

    static final String LOG_TRACE = LogTrace.class.getName();

    ClassProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCClassDecl)) {
            return;
        }
        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) jcTree;
        // Filter out methods with @LogTrace annotation and process them
        classDecl.defs.stream().filter(Objects::nonNull).filter(def -> def instanceof JCTree.JCMethodDecl)
                .map(def -> (JCTree.JCMethodDecl) def)
                .filter(def -> def.getModifiers().annotations != null && def.getModifiers().annotations.size() > 0)
                .filter(def -> {
                    for (JCTree.JCAnnotation anno : def.getModifiers().annotations) {
                        if (LOG_TRACE.equals(anno.getAnnotationType().type.toString())) {
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList())
                .forEach(methodDecl -> factory.get(ProcessorFactory.Kind.METHOD_DECL)
                        .process(methodDecl));
    }
}
