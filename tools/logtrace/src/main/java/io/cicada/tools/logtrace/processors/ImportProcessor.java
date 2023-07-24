package io.cicada.tools.logtrace.processors;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.lang.model.element.Element;
import java.util.stream.Collectors;

import static com.sun.tools.javac.tree.JCTree.Tag.IMPORT;
import static com.sun.tools.javac.tree.JCTree.Tag.PACKAGEDEF;

public class ImportProcessor extends TreeProcessor {

    ImportProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    // Import new classes for current Class
    @Override
    public void process(Element e, JCTree... jcTrees) {
        if (jcTrees != null) {

            final TreePath treePath = javacTrees.getPath(e);
            final JCTree.JCCompilationUnit unitTree = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();

            ListBuffer<JCTree> newDefs = new ListBuffer<>();

            // Add package and original import
            unitTree.defs.stream().filter(d -> d.hasTag(PACKAGEDEF) || d.hasTag(IMPORT))
                    .collect(Collectors.toList())
                    .forEach(newDefs::append);

            for (JCTree jcTree : jcTrees) {
                if (!(jcTree instanceof JCTree.JCImport)) {
                    continue;
                }
                newDefs.append(jcTree);
            }

            // Add original methods and fields
            unitTree.defs.stream().filter(d -> !d.hasTag(PACKAGEDEF) && !d.hasTag(IMPORT))
                    .collect(Collectors.toList())
                    .forEach(newDefs::append);

            unitTree.defs = List.from(newDefs);
        }


    }
}