package io.cicada.tools.logtrace.processors.custom;

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
import io.cicada.tools.logtrace.processors.ProcessorFactory;
import io.cicada.tools.logtrace.processors.TreeProcessor;
import org.slf4j.Logger;

import javax.lang.model.element.Element;
import java.util.*;
import java.util.stream.Collectors;

public class ClassProcessor extends TreeProcessor {

    static final String SLF4J_CHECK = Slf4jCheck.class.getName();

    static final String LOMBOK_PACK = "lombok.extern.slf4j.Slf4j";

    static final String LOMBOK_PACK_ROOT = "lombok.extern.slf4j.*";

    static final String SLF4J_PACK = Logger.class.getName();

    static final String SLF4J_PACK_ROOT = "org.slf4j.*";

    public ClassProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process() {
        Context.currentIsOpenIdentName.remove();
        Element e = Context.currentElement.get();
        JCTree jcTree = getJavacTrees().getTree(e);
        if (!(jcTree instanceof JCTree.JCClassDecl)) {
            return;
        }
        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) jcTree;
        final TreePath treePath = getJavacTrees().getPath(e);
        final JCTree.JCCompilationUnit unitTree = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();
        Context.lineMap.set(unitTree.getLineMap());
        final List<JCTree.JCImport> imports = unitTree.getImports();

        getTreeMaker().pos = classDecl.pos; // Reset pos.
        final List<JCTree.JCAnnotation> annos = classDecl.getModifiers().getAnnotations(); // All annotations of this Class

        final List<String> importClasses = imports.stream()
                .map(imp -> imp.getQualifiedIdentifier().toString())
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
                    getFactory().get(ProcessorFactory.Kind.IMPORT).process(getTreeMaker().Import(getTreeMaker().Select(
                                    getTreeMaker().Ident(getNames().fromString("java.util.concurrent.atomic")),
                                    getNames().fromString("AtomicBoolean")), false),
                            getTreeMaker().Import(getTreeMaker().Select(getTreeMaker().Ident(getNames().fromString("java.lang.reflect")),
                                    getNames().fromString("Field")), false));

                    // Var named is_open.
                    String isOpenIdentName = "is_open";
                    int num = 0;
                    while (allExistFieldNames.contains(isOpenIdentName)) { // Preventing naming conflicts.
                        isOpenIdentName = String.format("is_open_%d", num);
                        num++;
                    }

                    Context.currentIsOpenIdentName.set(isOpenIdentName);

                    JCTree.JCIdent atomicBooleanIdent = getTreeMaker().Ident(getNames().fromString("AtomicBoolean"));

                    // Generate code: static final AtomicBoolean is_open = null;
                    classDecl.defs = classDecl.defs.append(getTreeMaker().VarDef(
                            getTreeMaker().Modifiers(Flags.STATIC, com.sun.tools.javac.util.List.nil()),
                            getNames().fromString(isOpenIdentName), atomicBooleanIdent,
                            getTreeMaker().Literal(TypeTag.BOT, null)));

                    // Generate static block to init is_open.
                    classDecl.defs = classDecl.defs.append(getInitIsOpenStatement(assign.rhs.toString(), isOpenIdentName));
                }
            }
        }

        String logIdentName;
        if ((importClasses.contains(LOMBOK_PACK) || importClasses.contains(LOMBOK_PACK_ROOT))
                && annoMap.containsKey(LOMBOK_PACK)) { // Support lombok.
            logIdentName = "log";
        } else if ((importClasses.contains(SLF4J_PACK) || importClasses.contains(SLF4J_PACK_ROOT))
                && slf4jObjs.size() > 0) { // Local log obj.
            logIdentName = slf4jObjs.get(0); // Default to use the 1st one.
        } else { // Else, new slf4j logger object.
            logIdentName = "trace_logger";
            int num = 0;
            while (allExistFieldNames.contains(logIdentName)) { // Preventing naming conflicts.
                logIdentName = String.format("trace_logger_%d", num);
                num++;
            }

            // Import Logger.class, LoggerFactory.class, TRACE_ON_OFF.class.
            getFactory().get(ProcessorFactory.Kind.IMPORT).process(
                    getTreeMaker().Import(getTreeMaker().Select(getTreeMaker().Ident(getNames().fromString("org.slf4j")),
                            getNames().fromString("Logger")), false),
                    getTreeMaker().Import(getTreeMaker().Select(getTreeMaker().Ident(getNames().fromString("org.slf4j")),
                            getNames().fromString("LoggerFactory")), false));

            classDecl.defs = classDecl.defs.append(getTreeMaker().VarDef(
                    getTreeMaker().Modifiers(Flags.STATIC | Flags.FINAL, com.sun.tools.javac.util.List.nil()),
                    getNames().fromString(logIdentName), getTreeMaker().Ident(getNames().fromString("Logger")),
                    getTreeMaker().Apply(null, getTreeMaker().Select(
                                    getTreeMaker().Ident(getNames().fromString("LoggerFactory")),
                                    getNames().fromString("getLogger")),
                            com.sun.tools.javac.util.List.of(getTreeMaker().Select(
                                    getTreeMaker().Ident(getNames().fromString(classDecl.getSimpleName().toString())),
                                    getNames().fromString("class"))))));
        }
        // Init config
        Context.currentLogIdentName.set(logIdentName);


        // Filter out methods with @LogTrace annotation and process them
        classDecl.defs.stream().filter(Objects::nonNull).filter(def -> def instanceof JCTree.JCMethodDecl)
                .map(def -> (JCTree.JCMethodDecl) def)
                .filter(def -> def.getModifiers().annotations != null && def.getModifiers().annotations.size() > 0)
                .forEach(def -> {
                    getFactory().get(ProcessorFactory.Kind.METHOD).process(def);
                });
    }

    private JCTree.JCStatement getInitIsOpenStatement(String isOpenAnnoValue, String isOpenObjName) {
        String[] vs = isOpenAnnoValue.replace("\"", "").split("#");
        JCTree.JCIdent classIdent = getTreeMaker().Ident(getNames().fromString("Class"));
        Name clazzName = getNames().fromString("clazz");
        // Class<?> clazz = Class.forName("xx.xx.x");
        JCTree.JCVariableDecl clazzVar = getTreeMaker().VarDef(getTreeMaker().Modifiers(0), clazzName,
                classIdent, getTreeMaker().Apply(com.sun.tools.javac.util.List.nil(),
                        getTreeMaker().Select(getTreeMaker().Ident(getNames().fromString("Class")), getNames().fromString("forName")),
                        com.sun.tools.javac.util.List.of(getTreeMaker().Literal(vs[0]))));

        JCTree.JCIdent fieldIdent = getTreeMaker().Ident(getNames().fromString("Field"));
        Name fieldName = getNames().fromString("field");
        // Field field = clazz.getField("isOpen");
        JCTree.JCVariableDecl fieldVar = getTreeMaker().VarDef(getTreeMaker().Modifiers(0), fieldName,
                fieldIdent, getTreeMaker().Apply(com.sun.tools.javac.util.List.nil(),
                        getTreeMaker().Select(getTreeMaker().Ident(clazzName), getNames().fromString("getField")),
                        com.sun.tools.javac.util.List.of(getTreeMaker().Literal(vs[1]))));

        // field.setAccessible(true);
        JCTree.JCMethodInvocation methodInvocation = getTreeMaker().Apply(com.sun.tools.javac.util.List.nil(),
                getTreeMaker().Select(getTreeMaker().Ident(fieldName), getNames().fromString("setAccessible")),
                com.sun.tools.javac.util.List.of(getTreeMaker().Literal(true)));

        // isOpen = (AtomicBoolean) field.get(null)
        JCTree.JCAssign jcAssign = getTreeMaker().Assign(getTreeMaker().Ident(getNames().fromString(isOpenObjName)),
                getTreeMaker().TypeCast(getTreeMaker().Ident(getNames().fromString("AtomicBoolean")),
                        getTreeMaker().Apply(com.sun.tools.javac.util.List.nil(), getTreeMaker().Select(
                                        getTreeMaker().Ident(fieldName), getNames().fromString("get")),
                                com.sun.tools.javac.util.List.of(getTreeMaker().Literal(TypeTag.BOT, null)))));

        // Exception e
        JCTree.JCVariableDecl exceptionVar = getTreeMaker().VarDef(getTreeMaker().Modifiers(0), getNames().fromString("e"),
                getTreeMaker().Ident(getNames().fromString("Exception")), null);

        // throw new RuntimeException(e)
        JCTree.JCThrow throwStatement = getTreeMaker().Throw(getTreeMaker().NewClass(null, com.sun.tools.javac.util.List.nil(),
                getTreeMaker().Ident(getNames().fromString("RuntimeException")), com.sun.tools.javac.util.List.of(
                        getTreeMaker().Ident(getNames().fromString("e"))), null));

        // try{...}catch(...){...}
        JCTree.JCTry jcTry = getTreeMaker().Try(getTreeMaker().Block(0,
                        com.sun.tools.javac.util.List.of(clazzVar, fieldVar, getTreeMaker().Exec(methodInvocation), getTreeMaker().Exec(jcAssign))),
                com.sun.tools.javac.util.List.of(getTreeMaker().Catch(exceptionVar, getTreeMaker().Block(0,
                        com.sun.tools.javac.util.List.of(throwStatement)))), null);

        return getTreeMaker().Block(Flags.STATIC, com.sun.tools.javac.util.List.of(jcTry));
    }
}
