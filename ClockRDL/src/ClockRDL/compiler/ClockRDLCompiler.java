package ClockRDL.compiler;

import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;
import ClockRDL.model.declarations.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

/**
 * Created by ciprian on 14/10/15.
 */
public class ClockRDLCompiler {

    private static ClockRDLCompiler instance = new ClockRDLCompiler();

    public static Repository compile(File file) throws IOException {
        ANTLRFileStream fs = new ANTLRFileStream(file.getAbsolutePath());

        return instance.compile(fs);
    }

    public static Repository compile(String program) {
        ANTLRInputStream is = new ANTLRInputStream(program);

        return instance.compile(is);
    }

    public static URI generateModelXMI(Repository lib, String fileName) throws IOException {
        return instance.saveXMI(lib, fileName);
    }

    public Repository compile(ANTLRInputStream is) {
        ClockRDLLexer lexer = new ClockRDLLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ClockRDLParser parser = new ClockRDLParser(tokens);
        ParseTree tree = parser.libraryDecl();
        ParseTreeWalker walker = new ParseTreeWalker();
        ClockRDLBuilderAST builder = new ClockRDLBuilderAST(new GlobalScope());
        Repository topLib = DeclarationsFactory.eINSTANCE.createRepository();
        topLib.setName("root");


        //TODO define a clear error handling strategy for Parsing
        parser.removeErrorListeners();
        parser.addErrorListener(new ErrorHandler());

        try {
            walker.walk(builder, tree);
            LibraryDecl system = builder.library;
            //top lib
            topLib.getItems().add(system);

        } catch (Error e) {
            java.lang.System.err.println(e.getMessage());
            return null;
        }

        return topLib;
    }

    public URI saveXMI(Repository lib, String fileName) throws IOException {
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
