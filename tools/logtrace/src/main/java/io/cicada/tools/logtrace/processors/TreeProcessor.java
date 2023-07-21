package io.cicada.tools.logtrace.processors;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Element;

public abstract class TreeProcessor {
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
}
