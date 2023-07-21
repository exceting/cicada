package io.cicada.tools.logtrace.processors;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Element;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class RootProcessor extends TreeProcessor {

    static final String LOMBOK_PACK = "lombok.extern.slf4j.Slf4j";

    static final String SLF4J_PACK = "org.slf4j.Logger";

    RootProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(Element e) {
        final TreePath treePath = javacTrees.getPath(e);
        final JCTree.JCCompilationUnit unitTree = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();
        final List<JCTree.JCImport> imports = unitTree.getImports();
        final JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) javacTrees.getTree(e);
        final List<JCTree.JCAnnotation> annos = classDecl.mods.annotations; // All annotations of this Class

        final List<String> importClasses = imports.stream()
                .map(imp -> imp.qualid.type.toString())
                .collect(Collectors.toList());

        final List<String> annoClasses = annos.stream()
                .map(imp -> imp.annotationType.type.toString())
                .collect(Collectors.toList());

        final Set<String> allExistFieldNames = new HashSet<>();

        List<String> slf4jObjs = classDecl.defs.stream()
                .filter(def -> def instanceof JCTree.JCVariableDecl)
                .map(def -> {
                    JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) def;
                    allExistFieldNames.add(variableDecl.getName().toString());
                    return variableDecl;
                }).filter(def -> SLF4J_PACK.equals(def.getType().type.toString())
                        && (def.getModifiers().flags >> 3 & 1) == 1) // Filter static slf4j obj.
                .map(def -> def.getName().toString())
                .collect(Collectors.toList());

        String logIdentName;
        if (importClasses.contains(LOMBOK_PACK) && annoClasses.contains(LOMBOK_PACK)) { // Support lombok

            logIdentName = "log";
        } else if (importClasses.contains(SLF4J_PACK) && slf4jObjs.size() > 0) { // Local log obj.

            logIdentName = slf4jObjs.get(0); // Default to use the 1st one.
        } else { // Else, new slf4j logger object.

            logIdentName = "trace_logger";
            int num = 0;
            while (allExistFieldNames.contains(logIdentName)) { // Preventing naming conflicts.
                logIdentName = String.format("trace_logger_%d", num);
                num++;
            }

            // Import Logger and LoggerFactory
            factory.get(ProcessorFactory.Kind.IMPORT).process(e,
                    treeMaker.Import(treeMaker.Select(treeMaker.Ident(names.fromString("org.slf4j")), names.fromString("Logger")), false),
                    treeMaker.Import(treeMaker.Select(treeMaker.Ident(names.fromString("org.slf4j")), names.fromString("LoggerFactory")), false));

            // LoggerFactory.getLogger(MockForLogTrace.class)
            JCTree.JCMethodInvocation getLoggerInvoke = treeMaker.Apply(null, treeMaker.Select(
                            treeMaker.Ident(names.fromString("LoggerFactory")), names.fromString("getLogger")),
                    com.sun.tools.javac.util.List.of(treeMaker.Select(
                            treeMaker.Ident(names.fromString(classDecl.getSimpleName().toString())),
                            names.fromString("class"))));

            JCTree.JCVariableDecl loggerVariableDecl = treeMaker.VarDef(treeMaker.Modifiers(Flags.STATIC | Flags.FINAL, com.sun.tools.javac.util.List.nil()),
                    names.fromString(logIdentName), treeMaker.Ident(names.fromString("Logger")), getLoggerInvoke);

            classDecl.defs = classDecl.defs.append(loggerVariableDecl);
            factory.get(ProcessorFactory.Kind.CLASS_DECL).process(classDecl);
        }
    }
}
