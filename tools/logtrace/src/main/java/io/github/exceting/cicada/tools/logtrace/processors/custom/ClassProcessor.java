package io.github.exceting.cicada.tools.logtrace.processors.custom;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.annos.MethodLog;
import io.github.exceting.cicada.tools.logtrace.annos.Slf4jCheck;
import io.github.exceting.cicada.tools.logtrace.context.LogTraceContext;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;
import org.slf4j.Logger;

import javax.lang.model.element.Element;
import java.util.*;
import java.util.stream.Collectors;

public class ClassProcessor extends TreeProcessor {

    static final String SLF4J_CHECK = Slf4jCheck.class.getName();

    static final String METHOD_LOG = MethodLog.class.getName();

    static final String SLF4J_IS_OPEN = "isOpen";

    static final String LOMBOK_PACK = "lombok.extern.slf4j.Slf4j";

    static final String LOMBOK_PACK_ROOT = "lombok.extern.slf4j.*";

    static final String SLF4J_PACK = Logger.class.getName();

    static final String SLF4J_PACK_ROOT = "org.slf4j.*";

    public ClassProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process() {
        Element e = LogTraceContext.currentElement.get();
        JCTree jcTree = getJavacTrees().getTree(e);
        if (!(jcTree instanceof JCTree.JCClassDecl)) {
            return;
        }
        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) jcTree;
        final TreePath treePath = getJavacTrees().getPath(e);
        final JCTree.JCCompilationUnit unitTree = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();
        LogTraceContext.lineMap.set(unitTree.getLineMap());
        final List<JCTree.JCImport> imports = unitTree.getImports();

        getTreeMaker().pos = classDecl.pos; // Reset pos.
        final List<JCTree.JCAnnotation> annos = classDecl.getModifiers().getAnnotations(); // All annotations of this Class

        final List<String> importClasses = imports.stream()
                .map(imp -> imp.getQualifiedIdentifier().toString())
                .collect(Collectors.toList());

        final Map<String, JCTree.JCAnnotation> annoMap = annos.stream()
                .collect(Collectors.toMap(a -> a.annotationType.type.toString(), a -> a));

        final Set<String> allExistFieldNames = new HashSet<>();

        List<String> slf4jObjs = classDecl.getMembers().stream()
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

            // Import Logger.class, LoggerFactory.class.
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
        // Init config.
        LogTraceContext.currentLogIdentName.set(logIdentName);

        // Filter out methods with @MethodLog annotation and process them.
        List<JCTree.JCMethodDecl> allNeedProcessMethods = classDecl.getMembers().stream().filter(Objects::nonNull).filter(def -> def instanceof JCTree.JCMethodDecl)
                .map(def -> (JCTree.JCMethodDecl) def)
                .filter(def -> {
                    if (def.getModifiers().getAnnotations() != null && def.getModifiers().getAnnotations().size() > 0) {
                        for (JCTree.JCAnnotation anno : def.getModifiers().getAnnotations()) {
                            if (METHOD_LOG.equals(anno.getAnnotationType().type.toString())) {
                                return true;
                            }
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
        if (allNeedProcessMethods.size() > 0) {
            Map<String, String> isOpens = getIsOpens(annoMap, allNeedProcessMethods);
            if (isOpens != null && isOpens.size() > 0) {
                List<JCTree.JCStatement> statements = getAllIsOpenStatements(isOpens);
                if (statements != null && statements.size() > 0) {
                    classDecl.accept(new JCTree.Visitor() {
                        @Override
                        public void visitClassDef(JCTree.JCClassDecl that) {
                            that.defs = that.getMembers().appendList(com.sun.tools.javac.util.List.from(statements));
                        }
                    });
                }
            }
            LogTraceContext.allIsOpenMap.set(isOpens);
            allNeedProcessMethods.forEach(def -> getFactory().get(ProcessorFactory.Kind.METHOD).process(def));
        }
    }

    /**
     * Get all isOpen field, key = original field name, value = UUID var name.
     */
    private Map<String, String> getIsOpens(Map<String, JCTree.JCAnnotation> annoMap, List<JCTree.JCMethodDecl> methodDecls) {
        Set<String> isOpenNames = new HashSet<>();
        JCTree.JCAnnotation slf4jCheck = annoMap.get(SLF4J_CHECK);
        String cIsOpen = getAnnoAttrValue(slf4jCheck, SLF4J_IS_OPEN);
        if (cIsOpen != null) {
            isOpenNames.add(cIsOpen);
            LogTraceContext.classIsOpenFieldName.set(cIsOpen);
        }

        methodDecls.forEach(m -> {
            JCTree.JCAnnotation methodLog = m.getModifiers().getAnnotations().stream()
                    .filter(a -> METHOD_LOG.equals(a.getAnnotationType().type.toString()))
                    .collect(Collectors.toList())
                    .get(0);
            String mIsOpen = getAnnoAttrValue(methodLog, MethodProcessor.METHOD_LOG_IS_OPEN);
            if (mIsOpen != null) {
                isOpenNames.add(mIsOpen);
            }
        });

        if (isOpenNames.size() > 0) {
            getFactory().get(ProcessorFactory.Kind.IMPORT).process(getTreeMaker().Import(getTreeMaker().Select(
                            getTreeMaker().Ident(getNames().fromString("java.util.concurrent.atomic")),
                            getNames().fromString("AtomicBoolean")), false),
                    getTreeMaker().Import(getTreeMaker().Select(
                            getTreeMaker().Ident(getNames().fromString("java.lang.reflect")),
                            getNames().fromString("Field")), false));

            Map<String, String> isOpens = new LinkedHashMap<>();

            isOpenNames.forEach(isOpen -> isOpens.put(isOpen, getNewVarName("is_open_")));

            return isOpens;
        }
        return null;
    }

    private List<JCTree.JCStatement> getAllIsOpenStatements(Map<String, String> isOpenNameMap) {
        if (isOpenNameMap == null) {
            return null;
        }
        final List<JCTree.JCVariableDecl> isOpenVars = new ArrayList<>();
        final List<JCTree.JCStatement> isOpenStatements = new ArrayList<>();

        isOpenNameMap.forEach((className, varName) -> {

            JCTree.JCIdent atomicBooleanIdent = getTreeMaker().Ident(getNames().fromString("AtomicBoolean"));
            // Generate code: static final AtomicBoolean is_open = null;
            isOpenVars.add(getTreeMaker().VarDef(
                    getTreeMaker().Modifiers(Flags.STATIC, com.sun.tools.javac.util.List.nil()),
                    getNames().fromString(varName), atomicBooleanIdent,
                    getTreeMaker().Literal(TypeTag.BOT, null)));

            String[] vs = className.replace("\"", "").split("#");
            JCTree.JCIdent classIdent = getTreeMaker().Ident(getNames().fromString("Class"));
            Name clazzName = getNames().fromString(getNewVarName("clazz_"));
            // Class<?> clazz = Class.forName("xx.xx.x");
            isOpenStatements.add(getTreeMaker().VarDef(getTreeMaker().Modifiers(0), clazzName,
                    classIdent, getTreeMaker().Apply(com.sun.tools.javac.util.List.nil(),
                            getTreeMaker().Select(getTreeMaker().Ident(getNames().fromString("Class")), getNames().fromString("forName")),
                            com.sun.tools.javac.util.List.of(getTreeMaker().Literal(vs[0])))));

            JCTree.JCIdent fieldIdent = getTreeMaker().Ident(getNames().fromString("Field"));
            Name fieldName = getNames().fromString(getNewVarName("field_"));
            Name isOpenName = getNames().fromString(varName);

            // Field field = clazz.getField("isOpen");
            isOpenStatements.add(getTreeMaker().VarDef(getTreeMaker().Modifiers(0), fieldName,
                    fieldIdent, getTreeMaker().Apply(com.sun.tools.javac.util.List.nil(),
                            getTreeMaker().Select(getTreeMaker().Ident(clazzName), getNames().fromString("getField")),
                            com.sun.tools.javac.util.List.of(getTreeMaker().Literal(vs[1])))));

            // field.setAccessible(true);
            isOpenStatements.add(getTreeMaker().Exec(getTreeMaker().Apply(com.sun.tools.javac.util.List.nil(),
                    getTreeMaker().Select(getTreeMaker().Ident(fieldName), getNames().fromString("setAccessible")),
                    com.sun.tools.javac.util.List.of(getTreeMaker().Literal(true)))));

            // isOpen = (AtomicBoolean) field.get(null)
            isOpenStatements.add(getTreeMaker().Exec(getTreeMaker().Assign(getTreeMaker().Ident(isOpenName),
                    getTreeMaker().TypeCast(getTreeMaker().Ident(getNames().fromString("AtomicBoolean")),
                            getTreeMaker().Apply(com.sun.tools.javac.util.List.nil(), getTreeMaker().Select(
                                            getTreeMaker().Ident(fieldName), getNames().fromString("get")),
                                    com.sun.tools.javac.util.List.of(getTreeMaker().Literal(TypeTag.BOT, null)))))));

            // isOpen.get()     to check if null.
            isOpenStatements.add(getTreeMaker().Exec(getTreeMaker().Apply(com.sun.tools.javac.util.List.nil(),
                    getTreeMaker().Select(getTreeMaker().Ident(isOpenName), getNames().fromString("get")),
                    com.sun.tools.javac.util.List.nil())));
        });


        // Exception e
        JCTree.JCVariableDecl exceptionVar = getTreeMaker().VarDef(getTreeMaker().Modifiers(0), getNames().fromString("e"),
                getTreeMaker().Ident(getNames().fromString("Exception")), null);

        // throw new RuntimeException(e)
        JCTree.JCThrow throwStatement = getTreeMaker().Throw(getTreeMaker().NewClass(null, com.sun.tools.javac.util.List.nil(),
                getTreeMaker().Ident(getNames().fromString("RuntimeException")), com.sun.tools.javac.util.List.of(
                        getTreeMaker().Ident(getNames().fromString("e"))), null));

        // try{...}catch(...){...}
        JCTree.JCTry jcTry = getTreeMaker().Try(getTreeMaker().Block(0,
                        com.sun.tools.javac.util.List.from(isOpenStatements)),
                com.sun.tools.javac.util.List.of(getTreeMaker().Catch(exceptionVar, getTreeMaker().Block(0,
                        com.sun.tools.javac.util.List.of(throwStatement)))), null);

        List<JCTree.JCStatement> result = new ArrayList<>(isOpenVars);
        result.add(getTreeMaker().Block(Flags.STATIC, com.sun.tools.javac.util.List.of(jcTry)));

        return result;
    }
}
