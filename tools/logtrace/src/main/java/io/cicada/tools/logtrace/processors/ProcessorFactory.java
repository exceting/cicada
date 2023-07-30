package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Element;
import java.util.HashMap;
import java.util.Map;

/**
 * I have many processors :)
 */
public class ProcessorFactory {

    private final Map<Kind, TreeProcessor> processorMap;

    public ProcessorFactory(JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        processorMap = new HashMap<>();
        processorMap.put(Kind.ROOT, new RootProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.IMPORT, new ImportProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.BLOCK, new BlockProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.IF_STATEMENT, new IfProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.IF_COND, new ConditionalProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.CLASS_DECL, new ClassProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.METHOD_DECL, new MethodProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.TRY, new TryProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.FOR_LOOP, new ForLoopProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.ENHANCED_FOR_LOOP, new EnhancedForLoopProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.WHILE_LOOP, new WhileLoopProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.DO_WHILE_LOOP, new DoWhileLoopProcessor(this, javacTrees, treeMaker, names));
        processorMap.put(Kind.VARIABLE, new VariableProcessor(this, javacTrees, treeMaker, names));
    }

    public TreeProcessor get(Kind kind) {
        return processorMap.get(kind);
    }

    public enum Kind {
        ROOT(),
        IMPORT(),
        BLOCK(),
        IF_STATEMENT(),
        IF_COND(),
        CLASS_DECL(),
        METHOD_DECL(),
        TRY(),
        FOR_LOOP(),
        ENHANCED_FOR_LOOP(),
        WHILE_LOOP(),
        DO_WHILE_LOOP(),
        VARIABLE()
    }
}
