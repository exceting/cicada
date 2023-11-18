package io.github.exceting.cicada.tools.logtrace.processors.custom;

import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;
import io.github.exceting.cicada.tools.logtrace.context.Context;
import io.github.exceting.cicada.tools.logtrace.processors.ProcessorFactory;
import io.github.exceting.cicada.tools.logtrace.processors.TreeProcessor;

import java.util.stream.Collectors;

import static com.sun.tools.javac.tree.JCTree.Tag.IMPORT;
import static com.sun.tools.javac.tree.JCTree.Tag.PACKAGEDEF;

public class ImportProcessor extends TreeProcessor {

    public ImportProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        process(new JCTree[]{jcTree});
    }

    /**
     * Import new classes for current Class
     *
     * @param jcTrees see {@link JCTree.JCImport}
     */
    @Override
    public void process(JCTree... jcTrees) {
        if (jcTrees != null) {

            final TreePath treePath = getJavacTrees().getPath(Context.currentElement.get());
            final JCTree.JCCompilationUnit unitTree = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();

            ListBuffer<JCTree> newDefs = new ListBuffer<>();

            // Generate package and original import
            unitTree.defs.stream().filter(d -> d.hasTag(PACKAGEDEF) || d.hasTag(IMPORT))
                    .collect(Collectors.toList())
                    .forEach(newDefs::append);

            for (JCTree jcTree : jcTrees) {
                if (!(jcTree instanceof JCTree.JCImport)) {
                    continue;
                }
                newDefs.append(jcTree);
            }

            // Generate original methods and fields
            unitTree.defs.stream().filter(d -> !d.hasTag(PACKAGEDEF) && !d.hasTag(IMPORT))
                    .collect(Collectors.toList())
                    .forEach(newDefs::append);

            unitTree.defs = List.from(newDefs);
        }
    }
}
