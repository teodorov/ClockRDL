package ClockRDL.compiler;

import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;
import ClockRDL.model.declarations.DeclarationsFactory;
import ClockRDL.model.declarations.LibraryDecl;
import ClockRDL.model.declarations.Repository;
import ClockRDL.model.kernel.Declaration;
import ClockRDL.model.kernel.NamedDeclaration;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import java.io.File;
import java.io.IOException;
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
        ClockRDLBuilderAST builder = new ClockRDLBuilderAST(new Scope<NamedDeclaration>("global"));
        Repository topLib = DeclarationsFactory.eINSTANCE.createRepository();
        topLib.setName("root");

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

}
