package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.AnnoProcessor;
import io.cicada.tools.logtrace.annos.LogTrace;

import java.util.Objects;

public class ClassProcessor extends TreeProcessor {

    static final String LOG_TRACE = LogTrace.class.getName();

    static final String LOG_TRACE_EXCEPTION_LOG = "exceptionLog";
    static final String LOG_TRACE_RETURN_LOG = "returnLog";
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
                .forEach(def -> {
                    for (JCTree.JCAnnotation anno : def.getModifiers().annotations) {
                        if (!LOG_TRACE.equals(anno.getAnnotationType().type.toString())) {
                            continue;
                        }
                        boolean exceptionLog = false;
                        boolean returnLog = true;
                        String level = "Level.TRACE";
                        if (anno.getArguments() != null && anno.getArguments().size() > 0) {
                            for (JCTree.JCExpression arg : anno.getArguments()) {
                                if (!(arg instanceof JCTree.JCAssign)) {
                                    continue;
                                }
                                JCTree.JCAssign assign = (JCTree.JCAssign) arg;
                                if (LOG_TRACE_EXCEPTION_LOG.equals(assign.lhs.toString())) {
                                    exceptionLog = "true".equals(assign.rhs.toString());
                                }
                                if (LOG_TRACE_RETURN_LOG.equals(assign.lhs.toString())) {
                                    returnLog = "true".equals(assign.rhs.toString());
                                }
                                if (LOG_TRACE_LEVEL.equals(assign.lhs.toString())) {
                                    level = assign.rhs.toString();
                                }
                            }
                            AnnoProcessor.currentMethodConfig.set(new AnnoProcessor.MethodConfig(
                                    String.format("%s.%s#%s",
                                            classDecl.sym.packge().getQualifiedName(),
                                            classDecl.getSimpleName(),
                                            def.getName().toString()),
                                    argMap(def.getParameters()),
                                    exceptionLog,
                                    returnLog,
                                    level));
                            factory.get(ProcessorFactory.Kind.METHOD_DECL).process(def);
                        }
                    }
                });
    }
}
