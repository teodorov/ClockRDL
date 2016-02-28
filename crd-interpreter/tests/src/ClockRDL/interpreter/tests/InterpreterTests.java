package ClockRDL.interpreter.tests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.After;
import org.junit.Test;

import ClockRDL.compiler.ClockRDLBuilderAST;
import ClockRDL.compiler.GlobalScope;
import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;
import ClockRDL.interpreter.Environment;
import ClockRDL.interpreter.FireableTransition;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.interpreter.values.IntegerValue;
import ClockRDL.model.declarations.RelationInstanceDecl;

/**
 * Created by ciprian on 23/10/15.
 */
public class InterpreterTests {

    Interpreter interpreter = new Interpreter();

    String simpleLib = "library simple {\n" +
            "\trelation counter10\n" +
            "\t clock a := clock[x] b := clock[y];\n" +
            "\t\tvar x:=1;\n" +
            "\t{\n" +
            "\t [x<10]{a b}[x +=1];\n" +
            "\t\t[x>=10] {a b} [ x := 0];\n" +
            "\t}\n" +
            "}";
    String simpleInstance = "i:simple.counter10";
    String simpleInstanceWithClocks = "i:simple.counter10(a: clock[x] b:clock[y])";

    String libWithFct="library rel {\n"+
            " relation r \n"+
            " var x := 2;\n"+
            " def fct( a ) {\n"+
            "    return a + x\n"+
            " }\n"+
            " {\n"+
            " {} [{ assert(2 x) x := fct(1)+5 assert(8 x)}];\n"+
            " }\n"+
            "}";
    String fctInstance = "j:rel.r";


    @Test
    public void libWithFctExecutionOnce() {
        initialize(fctInstance, libWithFct);
        Environment env = interpreter.getEnvironment();
        RelationInstanceDecl instance = (RelationInstanceDecl)env.find("j");
        Set<FireableTransition> fireable = interpreter.fireableTransitions(instance);

        for (FireableTransition transition : fireable) {
            interpreter.evaluate(transition);
        }

        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("j");

        IntegerValue valueX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
        assertEquals(8, valueX.getData());
    }

    @Test
    public void simpleRelationExecutionOnce() {
        initialize(simpleInstance, simpleLib);
        Environment env = interpreter.getEnvironment();
        RelationInstanceDecl instance = (RelationInstanceDecl)env.find("i");
        Set<FireableTransition> fireable = interpreter.fireableTransitions(instance);

        for (FireableTransition transition : fireable) {
            interpreter.evaluate(transition);
        }

        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("i");

        IntegerValue valueX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
        assertEquals(2, valueX.getData());
    }

    @Test
    public void simpleRelationExecution10() {
        initialize(simpleInstance, simpleLib);
        Environment env = interpreter.getEnvironment();
        RelationInstanceDecl instance = (RelationInstanceDecl)env.find("i");

        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("i");
        for (int i = 1; i < 11; i++) {

            IntegerValue valueX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
            assertEquals(i, valueX.getData());

            Set<FireableTransition> fireable = interpreter.fireableTransitions(instance);
            for (FireableTransition transition : fireable) {
                interpreter.evaluate(transition);
            }
        }
    }

    @Test
    public void simpleRelationExecution30() {
        initialize(simpleInstance, simpleLib);
        Environment env = interpreter.getEnvironment();
        RelationInstanceDecl instance = (RelationInstanceDecl)env.find("i");

        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("i");
        for (int i = 1; i < 30; i++) {

            IntegerValue valueX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
            assertEquals(i % 11, valueX.getData());

            Set<FireableTransition> fireable = interpreter.fireableTransitions(instance);
            for (FireableTransition transition : fireable) {
                interpreter.evaluate(transition);
            }
        }
    }

    @Test
    public void simpleRelationFireables() {
        initialize(simpleInstance, simpleLib);
        RelationInstanceDecl instance = (RelationInstanceDecl) interpreter.getEnvironment().find("i");
        Set<FireableTransition> fireable = interpreter.fireableTransitions(instance);

        assertEquals(1, fireable.size());
    }

    @Test
    public void simpleRelationInitialization() {
        initialize(simpleInstance, simpleLib);
        Environment env = interpreter.getEnvironment();
        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("i");
        assertNotNull(instanceFrame);

        IntegerValue initialX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
        assertEquals(1, initialX.getData());

        Value initialA = instanceFrame.lookup("a", env.getMemory());
        assertEquals("x", (initialA).toString());

        Value initialB = instanceFrame.lookup("b", env.getMemory());
        assertEquals("y", (initialB).toString());

    }

    @Test
    public void simpleRelationWithClocksInitialization() {
        initialize(simpleInstanceWithClocks, simpleLib);
        Environment env = interpreter.getEnvironment();
        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("i");
        assertNotNull(instanceFrame);

        IntegerValue initialX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
        assertEquals(1, initialX.getData());

        Value initialA = instanceFrame.lookup("a", env.getMemory());
        assertEquals("x", (initialA).toString());

        Value initialB = instanceFrame.lookup("b", env.getMemory());
        assertEquals("y", (initialB).toString());

    }

    public void initialize(String blockCode, String libraryString) {
        RelationInstanceDecl instance = compile(blockCode, libraryString);

        interpreter.initialize(instance);
    }

    @After
    public void teardown() {
        interpreter.reset();
    }

    public RelationInstanceDecl compile(String instanceString, String libraryString) {

        GlobalScope scope = new GlobalScope();

        //parse the library
        ParseTree tree = parse(libraryString, "libraryDecl");
        ParseTreeWalker walker = new ParseTreeWalker();
        ClockRDLBuilderAST builder = new ClockRDLBuilderAST(scope, null);

        walker.walk(builder, tree);

        //parse the instanceString using the same global scope
        tree = parse(instanceString, "instanceDecl");
        walker.walk(builder, tree);

        return builder.getValue(tree, RelationInstanceDecl.class);
    }

    public ParseTree parse(String input, String rule) {
        ANTLRInputStream is = new ANTLRInputStream(input);
        ClockRDLLexer lexer = new ClockRDLLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Parser parser = new ClockRDLParser(tokens);

        try {
            Method mtd = parser.getClass().getMethod(rule);
            ParseTree pt = (ParseTree) mtd.invoke(parser);
            return pt;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            System.err.println("no matching method for rule: " + rule);
            return null;
        }
    }
}
