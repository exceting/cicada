package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

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
        TRY()
    }
}
