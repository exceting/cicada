package io.cicada.tools.logtrace.processors;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.annos.Slf4jCheck;
import io.cicada.tools.logtrace.context.Context;
import org.slf4j.Logger;

import javax.lang.model.element.Element;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class RootProcessor extends TreeProcessor {

    static final String SLF4J_CHECK = Slf4jCheck.class.getName();

    static final String LOMBOK_PACK = "lombok.extern.slf4j.Slf4j";

    static final String SLF4J_PACK = Logger.class.getName();

    RootProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process() {
        Context.currentIsOpenIdentName.remove();
        Element e = Context.currentElement.get();
        final TreePath treePath = javacTrees.getPath(e);
        final JCTree.JCCompilationUnit unitTree = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();
        Context.lineMap.set(unitTree.getLineMap());
        final List<JCTree.JCImport> imports = unitTree.getImports();
        final JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) javacTrees.getTree(e);
        final List<JCTree.JCAnnotation> annos = classDecl.mods.annotations; // All annotations of this Class

        final List<String> importClasses = imports.stream()
                .map(imp -> imp.qualid.type.toString())
                .collect(Collectors.toList());

        final Map<String, JCTree.JCAnnotation> annoMap = annos.stream()
                .collect(Collectors.toMap(a -> a.annotationType.type.toString(), a -> a));

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

        JCTree.JCAnnotation slf4jCheck = annoMap.get(SLF4J_CHECK);
        if (slf4jCheck.getArguments() != null && slf4jCheck.getArguments().size() > 0) {
            for (JCTree.JCExpression arg : slf4jCheck.getArguments()) {
                if (!(arg instanceof JCTree.JCAssign)) {
                    continue;
                }
                JCTree.JCAssign assign = (JCTree.JCAssign) arg;
                if ("isOpen".equals(assign.lhs.toString())) {
                    factory.get(ProcessorFactory.Kind.IMPORT).process(treeMaker.Import(treeMaker.Select(
                                    treeMaker.Ident(names.fromString("java.util.concurrent.atomic")),
                                    names.fromString("AtomicBoolean")), false),
                            treeMaker.Import(treeMaker.Select(treeMaker.Ident(names.fromString("java.lang.reflect")),
                                    names.fromString("Field")), false));

                    // Var named is_open.
                    String isOpenIdentName = "is_open";
                    int num = 0;
                    while (allExistFieldNames.contains(isOpenIdentName)) { // Preventing naming conflicts.
                        isOpenIdentName = String.format("is_open_%d", num);
                        num++;
                    }

                    Context.currentIsOpenIdentName.set(isOpenIdentName);

                    JCTree.JCIdent atomicBooleanIdent = treeMaker.Ident(names.fromString("AtomicBoolean"));

                    // Add code: static final AtomicBoolean is_open = null;
                    classDecl.defs = classDecl.defs.append(treeMaker.VarDef(
                            treeMaker.Modifiers(Flags.STATIC, com.sun.tools.javac.util.List.nil()),
                            names.fromString(isOpenIdentName), atomicBooleanIdent, treeMaker.Literal(TypeTag.BOT, null)));

                    // Add static block to init is_open.
                    classDecl.defs = classDecl.defs.append(getInitIsOpenStatement(assign.rhs.toString(), isOpenIdentName));
                }
            }
        }

        String logIdentName;
        if (importClasses.contains(LOMBOK_PACK) && annoMap.containsKey(LOMBOK_PACK)) { // Support lombok.
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

            // Import Logger.class, LoggerFactory.class, TRACE_ON_OFF.class.
            factory.get(ProcessorFactory.Kind.IMPORT).process(
                    treeMaker.Import(treeMaker.Select(treeMaker.Ident(names.fromString("org.slf4j")),
                            names.fromString("Logger")), false),
                    treeMaker.Import(treeMaker.Select(treeMaker.Ident(names.fromString("org.slf4j")),
                            names.fromString("LoggerFactory")), false));

            classDecl.defs = classDecl.defs.append(treeMaker.VarDef(
                    treeMaker.Modifiers(Flags.STATIC | Flags.FINAL, com.sun.tools.javac.util.List.nil()),
                    names.fromString(logIdentName), treeMaker.Ident(names.fromString("Logger")),
                    treeMaker.Apply(null, treeMaker.Select(
                                    treeMaker.Ident(names.fromString("LoggerFactory")),
                                    names.fromString("getLogger")),
                            com.sun.tools.javac.util.List.of(treeMaker.Select(
                                    treeMaker.Ident(names.fromString(classDecl.getSimpleName().toString())),
                                    names.fromString("class"))))));
        }
        // Init config
        Context.currentLogIdentName.set(logIdentName);
        factory.get(ProcessorFactory.Kind.CLASS_DECL).process(classDecl);
    }

    private JCTree.JCStatement getInitIsOpenStatement(String isOpenAnnoValue, String isOpenObjName) {
        String[] vs = isOpenAnnoValue.replace("\"", "").split("#");
        JCTree.JCIdent classIdent = treeMaker.Ident(names.fromString("Class"));
        Name clazzName = names.fromString("clazz");
        // Class<?> clazz = Class.forName("xx.xx.x");
        JCTree.JCVariableDecl clazzVar = treeMaker.VarDef(treeMaker.Modifiers(0), clazzName,
                classIdent, treeMaker.Apply(com.sun.tools.javac.util.List.nil(),
                        treeMaker.Select(treeMaker.Ident(names.fromString("Class")), names.fromString("forName")),
                        com.sun.tools.javac.util.List.of(treeMaker.Literal(vs[0]))));

        JCTree.JCIdent fieldIdent = treeMaker.Ident(names.fromString("Field"));
        Name fieldName = names.fromString("field");
        // Field field = clazz.getField("isOpen");
        JCTree.JCVariableDecl fieldVar = treeMaker.VarDef(treeMaker.Modifiers(0), fieldName,
                fieldIdent, treeMaker.Apply(com.sun.tools.javac.util.List.nil(),
                        treeMaker.Select(treeMaker.Ident(clazzName), names.fromString("getField")),
                        com.sun.tools.javac.util.List.of(treeMaker.Literal(vs[1]))));

        // field.setAccessible(true);
        JCTree.JCMethodInvocation methodInvocation = treeMaker.Apply(com.sun.tools.javac.util.List.nil(),
                treeMaker.Select(treeMaker.Ident(fieldName), names.fromString("setAccessible")),
                com.sun.tools.javac.util.List.of(treeMaker.Literal(true)));

        // isOpen = (AtomicBoolean) field.get(null)
        JCTree.JCAssign jcAssign = treeMaker.Assign(treeMaker.Ident(names.fromString(isOpenObjName)),
                treeMaker.TypeCast(treeMaker.Ident(names.fromString("AtomicBoolean")),
                        treeMaker.Apply(com.sun.tools.javac.util.List.nil(), treeMaker.Select(
                                        treeMaker.Ident(fieldName), names.fromString("get")),
                                com.sun.tools.javac.util.List.of(treeMaker.Literal(TypeTag.BOT, null)))));

        // Exception e
        JCTree.JCVariableDecl exceptionVar = treeMaker.VarDef(treeMaker.Modifiers(0), names.fromString("e"),
                treeMaker.Ident(names.fromString("Exception")), null);

        // throw new RuntimeException(e)
        JCTree.JCThrow throwStatement = treeMaker.Throw(treeMaker.NewClass(null, com.sun.tools.javac.util.List.nil(),
                treeMaker.Ident(names.fromString("RuntimeException")), com.sun.tools.javac.util.List.of(
                        treeMaker.Ident(names.fromString("e"))), null));

        // try{...}catch(...){...}
        JCTree.JCTry jcTry = treeMaker.Try(treeMaker.Block(0,
                        com.sun.tools.javac.util.List.of(clazzVar, fieldVar, treeMaker.Exec(methodInvocation), treeMaker.Exec(jcAssign))),
                com.sun.tools.javac.util.List.of(treeMaker.Catch(exceptionVar, treeMaker.Block(0,
                        com.sun.tools.javac.util.List.of(throwStatement)))), null);

        return treeMaker.Block(Flags.STATIC, com.sun.tools.javac.util.List.of(jcTry));
    }
}
