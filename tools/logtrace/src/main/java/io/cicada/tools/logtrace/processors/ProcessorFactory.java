package io.cicada.tools.logtrace.processors;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import io.cicada.tools.logtrace.processors.custom.*;
import io.cicada.tools.logtrace.processors.tree.*;

import java.util.HashMap;
import java.util.Map;

/**
 * I have many processors :)
 */
public class ProcessorFactory {

    private final TreeProcessor noop;
    private final Map<Kind, TreeProcessor> baseProcessorMap;
    private final Map<Tree.Kind, TreeProcessor> processorMap;

    public ProcessorFactory(JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        baseProcessorMap = new HashMap<>();
        processorMap = new HashMap<>();

        baseProcessorMap.put(Kind.STARTER, new StarterProcessor(this, javacTrees, treeMaker, names));
        baseProcessorMap.put(Kind.IMPORT, new ImportProcessor(this, javacTrees, treeMaker, names));
        baseProcessorMap.put(Kind.CLASS, new ClassProcessor(this, javacTrees, treeMaker, names));
        baseProcessorMap.put(Kind.METHOD, new MethodProcessor(this, javacTrees, treeMaker, names));

        noop = new NoopProcessor(this, javacTrees, treeMaker, names);

        processorMap.put(Tree.Kind.BLOCK, new BlockProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.IF, new IfProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.CONDITIONAL_EXPRESSION, new ConditionalProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.TRY, new TryProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.CATCH, new CatchProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.FOR_LOOP, new ForLoopProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.ENHANCED_FOR_LOOP, new EnhancedForLoopProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.WHILE_LOOP, new WhileLoopProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.DO_WHILE_LOOP, new DoWhileLoopProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.VARIABLE, new VariableProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.SWITCH, new SwitchProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.METHOD_INVOCATION, new MethodInvocationProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.LAMBDA_EXPRESSION, new LambdaExpressionProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.MEMBER_SELECT, new FieldAccessProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.EXPRESSION_STATEMENT, new ExpressionStatementProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.NEW_CLASS, new NewClassProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.CLASS, new ClassDeclProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.NEW_ARRAY, new NewArrayProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.METHOD, new MethodDeclProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.PARENTHESIZED, new ParensProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Tree.Kind.TYPE_CAST, new TypeCastProcessor(this, javacTrees, treeMaker, names));
    }

    public TreeProcessor get(Kind kind) {
        TreeProcessor tp = baseProcessorMap.get(kind);
        return tp == null ? noop : tp;
    }

    public TreeProcessor get(Tree.Kind kind) {
        TreeProcessor tp = processorMap.get(kind);
        return tp == null ? noop : tp;
    }

    public enum Kind {
        STARTER(),
        IMPORT(),
        CLASS(),
        METHOD()
    }
}
