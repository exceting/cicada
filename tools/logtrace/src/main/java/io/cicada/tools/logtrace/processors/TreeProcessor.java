package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Name;
import org.slf4j.event.Level;

import javax.lang.model.element.Element;

public abstract class TreeProcessor {
    static final String PREFIX = "LOG_TRACE >>>>>> OUTPUT: ";
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
}
