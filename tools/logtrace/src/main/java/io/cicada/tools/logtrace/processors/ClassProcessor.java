package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.AnnoProcessor;
import io.cicada.tools.logtrace.annos.LogTrace;
import org.slf4j.event.Level;

import java.util.Objects;
import java.util.stream.Collectors;

public class ClassProcessor extends TreeProcessor {

    static final String LOG_TRACE = LogTrace.class.getName();

    static final String LOG_TRACE_EXCEPTION_LOG = "exceptionLog";
    static final String LOG_TRACE_LOOP = "traceLoop";
    static final String LOG_TRACE_LEVEL = "traceLevel";

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
                            boolean exceptionLog = false;
                            boolean traceLoop = false;
                            Level level = Level.TRACE;
                            if (anno.getArguments() != null && anno.getArguments().size() > 0) {
                                for (JCTree.JCExpression arg : anno.getArguments()) {
                                    if (!(arg instanceof JCTree.JCAssign)) {
                                        continue;
                                    }
                                    JCTree.JCAssign assign = (JCTree.JCAssign) arg;
                                    if (LOG_TRACE_EXCEPTION_LOG.equals(assign.lhs.toString())) {
                                        exceptionLog = "true".equals(assign.rhs.toString());
                                    }
                                    if (LOG_TRACE_LOOP.equals(assign.lhs.toString())) {
                                        traceLoop = "true".equals(assign.rhs.toString());
                                    }
                                    if (LOG_TRACE_LEVEL.equals(assign.lhs.toString())) {
                                        switch (assign.rhs.toString()) {
                                            case "Level.ERROR":
                                                level = Level.ERROR;
                                                break;
                                            case "Level.WARN":
                                                level = Level.WARN;
                                                break;
                                            case "Level.INFO":
                                                level = Level.INFO;
                                                break;
                                            case "Level.DEBUG":
                                                level = Level.DEBUG;
                                                break;
                                            case "Level.TRACE":
                                                level = Level.TRACE;
                                                break;
                                            default:
                                        }
                                    }
                                }
                            }
                            AnnoProcessor.config.get().getMethodConfigMap().put(def,
                                    new AnnoProcessor.GlobalConfig.MethodConfig(exceptionLog, traceLoop, level));
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList())
                .forEach(methodDecl -> factory.get(ProcessorFactory.Kind.METHOD_DECL)
                        .process(methodDecl));
    }
}
