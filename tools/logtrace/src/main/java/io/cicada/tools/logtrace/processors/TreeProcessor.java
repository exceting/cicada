package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Name;
import org.slf4j.event.Level;

import javax.lang.model.element.Element;
import java.util.*;

public abstract class TreeProcessor {
    static final String PREFIX = "LOG_TRACE >>>>>> OUTPUT: [METHOD: %s][%s] ";
    ProcessorFactory factory;
    JavacTrees javacTrees;
    TreeMaker treeMaker;
    Names names;

    public void process(JCTree jcTree) {
        // do nothing
    }

    public void process(Element e) {
        // do nothing
    }

    public void process(Element e, JCTree... jcTrees) {
        // do nothing
    }

    TreeProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        this.factory = factory;
        this.javacTrees = javacTrees;
        this.treeMaker = treeMaker;
        this.names = names;
    }

    Map<String, JCTree.JCExpression> argMap(List<JCTree.JCVariableDecl> originalArgs) {
        if (originalArgs == null || originalArgs.size() == 0) {
            return null;
        }
        Map<String, JCTree.JCExpression> result = new LinkedHashMap<>();
        originalArgs.forEach(oa -> {
            String argName = oa.getName().toString();
            result.put(argName, getExpByClassType(argName, getClassType(oa.getType()
                    .type.tsym.getQualifiedName().toString())));
        });
        return result;
    }

    Name getSlf4jMethod(Level level) {
        switch (level) {
            case ERROR:
                return names.fromString("error");
            case WARN:
                return names.fromString("warn");
            case INFO:
                return names.fromString("info");
            case DEBUG:
                return names.fromString("debug");
            case TRACE:
                return names.fromString("trace");
            default:
                return null;
        }
    }

    JCTree.JCExpression getExpByClassType(String argName, ClassType classType) {
        switch (classType) {
            case PRIMITIVE:
            case STRING:
            case UNRECOGNIZED:
            case CLASS_NOT_FOUND:
                return treeMaker.Ident(names.fromString(argName));
            case ARRAY:
                return getConExp(argName, "length", true);
            case COLLECTION:
            case MAP:
                return getConExp(argName, "size", false);
            default:
                return null;
        }
    }

    //

    /**
     * @param argName  obj name, eg: a
     * @param attrName field or method name, eg: .length, .size()
     * @param field    is field? true: field, false: method
     * @return conditional exp, eg: a == null ? null : a.size()
     */
    private JCTree.JCExpression getConExp(String argName, String attrName, boolean field) {
        JCTree.JCIdent argIdent = treeMaker.Ident(names.fromString(argName));
        JCTree.JCBinary binary = treeMaker.Binary(JCTree.Tag.EQ, argIdent, treeMaker.Literal(TypeTag.BOT, null));

        return treeMaker.Conditional(binary, treeMaker.Literal(TypeTag.BOT, null),
                field ? getFieldAccessExp(argIdent, attrName) : getMethodInvokeExp(argIdent, attrName));
    }

    private JCTree.JCExpression getFieldAccessExp(JCTree.JCIdent argIdent, String fieldName) {
        return treeMaker.Select(argIdent, names.fromString(fieldName));
    }

    private JCTree.JCExpression getMethodInvokeExp(JCTree.JCIdent argIdent, String sizeMethodName) {
        return treeMaker.Apply(com.sun.tools.javac.util.List.nil(),
                treeMaker.Select(argIdent, names.fromString(sizeMethodName)),
                com.sun.tools.javac.util.List.nil());
    }

    /**
     * Attach new code for method
     *
     * @param stats    original code statements
     * @param attached new code statement
     * @param offset   position of new code
     * @return The new method statements which contains new code.
     */
    com.sun.tools.javac.util.List<JCTree.JCStatement> attachCode(com.sun.tools.javac.util.List<JCTree.JCStatement> stats,
                                                                 JCTree.JCStatement attached,
                                                                 int offset) {
        return attachCode(stats, com.sun.tools.javac.util.List.of(attached), offset);
    }

    com.sun.tools.javac.util.List<JCTree.JCStatement> attachCode(com.sun.tools.javac.util.List<JCTree.JCStatement> stats,
                                                                 com.sun.tools.javac.util.List<JCTree.JCStatement> attached,
                                                                 int offset) {
        List<JCTree.JCStatement> statements = new ArrayList<>();
        // before
        for (int i = 0; i < offset; i++) {
            statements.add(stats.get(i));
        }
        // attach code
        statements.addAll(attached);
        // after
        for (int i = offset; i < stats.size(); i++) {
            statements.add(stats.get(i));
        }
        return com.sun.tools.javac.util.List.from(statements);
    }

    static ClassType getClassType(String className) { // FIXME Can't process inner class

        // PRIMITIVE TYPE, eg: boolean, char, byte, short, int, long, float, double
        if (Boolean.TYPE.getName().equals(className)
                || Character.TYPE.getName().equals(className)
                || Byte.TYPE.getName().equals(className)
                || Short.TYPE.getName().equals(className)
                || Integer.TYPE.getName().equals(className)
                || Long.TYPE.getName().equals(className)
                || Float.TYPE.getName().equals(className)
                || Double.TYPE.getName().equals(className)) {
            return ClassType.PRIMITIVE;
        }
        // ARRAY TYPE, eg: xx[]
        if ("Array".equals(className)) {
            return ClassType.ARRAY;
        }
        try {
            Class<?> clazz = Class.forName(className);
            // Wrapper Classes, eg: Boolean, Character, Byte, Short, Integer, Long, Float, Double
            if (clazz == Boolean.class
                    || clazz == Character.class
                    || clazz == Byte.class
                    || clazz == Short.class
                    || clazz == Integer.class
                    || clazz == Long.class
                    || clazz == Float.class
                    || clazz == Double.class) {
                return ClassType.PRIMITIVE;
            } else if (clazz == String.class) {
                return ClassType.STRING;
            } else if (Collection.class.isAssignableFrom(clazz)) {
                return ClassType.COLLECTION;
            } else if (Map.class.isAssignableFrom(clazz)) {
                return ClassType.MAP;
            } else {
                return ClassType.UNRECOGNIZED;
            }
        } catch (ClassNotFoundException e) {
            return ClassType.CLASS_NOT_FOUND;
        }
    }

    public enum ClassType {
        PRIMITIVE,
        ARRAY,
        STRING,
        COLLECTION,
        MAP,
        UNRECOGNIZED,
        CLASS_NOT_FOUND,
        DO_NOTHING
    }
}
