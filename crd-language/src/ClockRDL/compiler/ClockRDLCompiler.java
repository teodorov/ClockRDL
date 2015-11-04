package ClockRDL.compiler;

import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;
import ClockRDL.model.declarations.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ciprian on 14/10/15.
 */
public class ClockRDLCompiler {

    private static ClockRDLCompiler instance = new ClockRDLCompiler();

    public static RepositoryDecl compile(File file, List<java.net.URI> libraryPaths) throws IOException {
        ANTLRFileStream fs = new ANTLRFileStream(file.getAbsolutePath());

        return instance.compile(fs, libraryPaths);
    }

    public static RepositoryDecl compile(String program, List<java.net.URI> libraryPaths) {
        ANTLRInputStream is = new ANTLRInputStream(program);

        return instance.compile(is, libraryPaths);
    }

    public static URI generateModelXMI(RepositoryDecl lib, String fileName) throws IOException {
        return instance.saveXMI(lib, fileName);
    }

    public RepositoryDecl compile(ANTLRInputStream is, List<java.net.URI> libraryPaths) {
        ClockRDLLexer lexer = new ClockRDLLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ClockRDLParser parser = new ClockRDLParser(tokens);
        ParseTree tree = parser.systemDecl();
        ParseTreeWalker walker = new ParseTreeWalker();

        List<java.net.URI> libPaths = libraryPaths;
        if (libPaths == null) {
            libPaths = new ArrayList<>();
        }
        libPaths.add((new File(System.getProperty("user.dir"))).toURI());

        ClockRDLBuilderAST builder = new ClockRDLBuilderAST(new GlobalScope(), libPaths);

        //TODO define a clear error handling strategy for Parsing
        parser.removeErrorListeners();
        parser.addErrorListener(new ErrorHandler());

        try {
            walker.walk(builder, tree);
            return builder.getValue(tree, RepositoryDecl.class);

        } catch (Error e) {
            java.lang.System.err.println(e.getMessage());
            return null;
        }
    }

    public URI saveXMI(RepositoryDecl lib, String fileName) throws IOException {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        Resource r = resourceSet.createResource(URI.createFileURI(fileName));
        r.getContents().add(lib);
        r.save(Collections.emptyMap());
        return r.getURI();
    }

    public static class ErrorHandler extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> rec, Object offendingSymbol, int line, int column, String msg, RecognitionException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

}
