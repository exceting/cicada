package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.annos.LogTrace;
import io.cicada.tools.logtrace.context.Context;

import java.util.Objects;

public class ClassProcessor extends TreeProcessor {

    static final String LOG_TRACE = LogTrace.class.getName();

    static final String LOG_TRACE_EXCEPTION_LOG = "exceptionLog";
    static final String LOG_TRACE_BAN_LOOP = "banLoop";
    static final String LOG_TRACE_LEVEL = "traceLevel";

    static final String LOG_TRACE_ARRAY_TO_SIZE = "arrayToSize";

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
                .forEach(def -> {
                    for (JCTree.JCAnnotation anno : def.getModifiers().annotations) {
                        if (!LOG_TRACE.equals(anno.getAnnotationType().type.toString())) {
                            continue;
                        }
                        boolean exceptionLog = false;
                        boolean banLoop = false;
                        String level = "Level.TRACE";
                        boolean arrayToSize = false;
                        if (anno.getArguments() != null && anno.getArguments().size() > 0) {
                            for (JCTree.JCExpression arg : anno.getArguments()) {
                                if (!(arg instanceof JCTree.JCAssign)) {
                                    continue;
                                }
                                JCTree.JCAssign assign = (JCTree.JCAssign) arg;
                                if (LOG_TRACE_EXCEPTION_LOG.equals(assign.lhs.toString())) {
                                    exceptionLog = "true".equals(assign.rhs.toString());
                                }
                                if (LOG_TRACE_BAN_LOOP.equals(assign.lhs.toString())) {
                                    banLoop = "true".equals(assign.rhs.toString());
                                }
                                if (LOG_TRACE_LEVEL.equals(assign.lhs.toString())) {
                                    level = assign.rhs.toString();
                                }
                                if (LOG_TRACE_ARRAY_TO_SIZE.equals(assign.lhs.toString())) {
                                    arrayToSize = "true".equals(assign.rhs.toString());
                                }
                            }
                        }
                        Context.currentMethodConfig.set(new Context.MethodConfig(
                                def.getName().toString(),
                                argMap(def.getParameters(), arrayToSize),
                                exceptionLog,
                                banLoop,
                                level,
                                arrayToSize));
                        factory.get(ProcessorFactory.Kind.METHOD_DECL).process(def);
                    }
                });
    }
}
