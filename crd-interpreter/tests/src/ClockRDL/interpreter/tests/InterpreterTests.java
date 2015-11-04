package ClockRDL.interpreter.tests;

import ClockRDL.compiler.ClockRDLBuilderAST;
import ClockRDL.compiler.GlobalScope;
import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;
import ClockRDL.interpreter.*;
import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.interpreter.values.ClockValue;
import ClockRDL.interpreter.values.IntegerValue;
import ClockRDL.interpreter.values.NullValue;
import ClockRDL.model.declarations.RelationInstanceDecl;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by ciprian on 23/10/15.
 */
public class InterpreterTests {

    Interpreter evaluator = new Interpreter();

    String simpleLib = "library simple {\n" +
            "\trelation counter10\n" +
            "\t clock a b;\n" +
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
        Environment env = initialize(fctInstance, libWithFct);
        RelationInstanceDecl instance = (RelationInstanceDecl)env.find("j");
        Set<FireableTransition> fireable = evaluator.fireableTransitions(instance, env);

        for (FireableTransition transition : fireable) {
            evaluator.evaluate(transition, env);
        }

        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("j");

        IntegerValue valueX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
        assertEquals(8, valueX.data);
    }

    @Test
    public void simpleRelationExecutionOnce() {
        Environment env = initialize(simpleInstance, simpleLib);
        RelationInstanceDecl instance = (RelationInstanceDecl)env.find("i");
        Set<FireableTransition> fireable = evaluator.fireableTransitions(instance, env);

        for (FireableTransition transition : fireable) {
            evaluator.evaluate(transition, env);
        }

        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("i");

        IntegerValue valueX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
        assertEquals(2, valueX.data);
    }

    @Test
    public void simpleRelationExecution10() {
        Environment env = initialize(simpleInstance, simpleLib);
        RelationInstanceDecl instance = (RelationInstanceDecl)env.find("i");

        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("i");
        for (int i = 1; i < 11; i++) {

            IntegerValue valueX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
            assertEquals(i, valueX.data);

            Set<FireableTransition> fireable = evaluator.fireableTransitions(instance, env);
            for (FireableTransition transition : fireable) {
                evaluator.evaluate(transition, env);
            }
        }
    }

    @Test
    public void simpleRelationExecution30() {
        Environment env = initialize(simpleInstance, simpleLib);
        RelationInstanceDecl instance = (RelationInstanceDecl)env.find("i");

        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("i");
        for (int i = 1; i < 30; i++) {

            IntegerValue valueX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
            assertEquals(i % 11, valueX.data);

            Set<FireableTransition> fireable = evaluator.fireableTransitions(instance, env);
            for (FireableTransition transition : fireable) {
                evaluator.evaluate(transition, env);
            }
        }
    }

    @Test
    public void simpleRelationFireables() {
        Environment env = initialize(simpleInstance, simpleLib);
        RelationInstanceDecl instance = (RelationInstanceDecl)env.find("i");
        Set<FireableTransition> fireable = evaluator.fireableTransitions(instance, env);

        assertEquals(1, fireable.size());
    }

    @Test
    public void simpleRelationInitialization() {
        Environment env = initialize(simpleInstance, simpleLib);
        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("i");
        assertNotNull(instanceFrame);

        IntegerValue initialX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
        assertEquals(1, initialX.data);

        Value initialA = instanceFrame.lookup("a", env.getMemory());
        assertEquals(NullValue.uniqueInstance, initialA);

        Value initialB = instanceFrame.lookup("b", env.getMemory());
        assertEquals(NullValue.uniqueInstance, initialB);

    }

    @Test
    public void simpleRelationWithClocksInitialization() {
        Environment env = initialize(simpleInstanceWithClocks, simpleLib);
        AbstractFrame instanceFrame = (AbstractFrame) env.lookup("i");
        assertNotNull(instanceFrame);

        IntegerValue initialX = (IntegerValue) instanceFrame.lookup("x", env.getMemory());
        assertEquals(1, initialX.data);

        Value initialA = instanceFrame.lookup("a", env.getMemory());
        assertEquals("x", ((ClockValue)initialA).toString());

        Value initialB = instanceFrame.lookup("b", env.getMemory());
        assertEquals("y", ((ClockValue)initialB).toString());

    }

    public Environment initialize(String blockCode, String libraryString) {
        RelationInstanceDecl instance = compile(blockCode, libraryString);
        Environment env = new Environment();

        evaluator.initialize(instance, env);
        return env;
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
