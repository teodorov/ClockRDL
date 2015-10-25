package ClockRDL.interpreter.tests;

import ClockRDL.compiler.ClockRDLBuilderAST;
import ClockRDL.compiler.GlobalScope;
import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;
import ClockRDL.interpreter.*;
import ClockRDL.interpreter.values.IntegerValue;
import ClockRDL.interpreter.values.NulValue;
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
            "\t [x<10]{a b}[x +=1]\n" +
            "\t\t[x>=10] {a b} [ x := 0]\n" +
            "\t}\n" +
            "}";
    String simpleInstance = "i:simple.counter10";

    @Test
    public void simpleRelationExecutionOnce() {
        Frame env = initialize(simpleInstance, simpleLib);
        RelationInstanceDecl instance = env.getMapping().keySet().toArray(new RelationInstanceDecl[1])[0];
        Set<FireableTransition> fireable = evaluator.fireableTransitions(instance, env);

        for (FireableTransition transition : fireable) {
            evaluator.evaluate(transition);
        }

        Frame instanceFrame = (Frame) env.lookup("i");

        IntegerValue valueX = (IntegerValue) instanceFrame.lookup("x");
        assertEquals(2, valueX.data);
    }

    @Test
    public void simpleRelationExecution10() {
        Frame env = initialize(simpleInstance, simpleLib);
        RelationInstanceDecl instance = env.getMapping().keySet().toArray(new RelationInstanceDecl[1])[0];

        Frame instanceFrame = (Frame) env.lookup("i");
        for (int i = 1; i < 11; i++) {

            IntegerValue valueX = (IntegerValue) instanceFrame.lookup("x");
            assertEquals(i, valueX.data);

            Set<FireableTransition> fireable = evaluator.fireableTransitions(instance, env);
            for (FireableTransition transition : fireable) {
                evaluator.evaluate(transition);
            }
        }
    }

    @Test
    public void simpleRelationExecution30() {
        Frame env = initialize(simpleInstance, simpleLib);
        RelationInstanceDecl instance = env.getMapping().keySet().toArray(new RelationInstanceDecl[1])[0];

        Frame instanceFrame = (Frame) env.lookup("i");
        for (int i = 1; i < 30; i++) {

            IntegerValue valueX = (IntegerValue) instanceFrame.lookup("x");
            assertEquals(i % 11, valueX.data);

            Set<FireableTransition> fireable = evaluator.fireableTransitions(instance, env);
            for (FireableTransition transition : fireable) {
                evaluator.evaluate(transition);
            }
        }
    }

    @Test
    public void simpleRelationFireables() {
        Frame env = initialize(simpleInstance, simpleLib);
        RelationInstanceDecl instance = env.getMapping().keySet().toArray(new RelationInstanceDecl[1])[0];
        Set<FireableTransition> fireable = evaluator.fireableTransitions(instance, env);

        assertEquals(1, fireable.size());
    }

    @Test
    public void simpleRelationInitialization() {
        Frame env = initialize(simpleInstance, simpleLib);
        Frame instanceFrame = (Frame) env.lookup("i");
        assertNotNull(instanceFrame);

        IntegerValue initialX = (IntegerValue) instanceFrame.lookup("x");
        assertEquals(1, initialX.data);

        Value initialA = instanceFrame.lookup("a");
        assertEquals(NulValue.uniqueInstance, initialA);

        Value initialB = instanceFrame.lookup("b");
        assertEquals(NulValue.uniqueInstance, initialB);

    }

    public Frame initialize(String blockCode, String libraryString) {
        RelationInstanceDecl instance = compile(blockCode, libraryString);
        GlobalFrame env = new GlobalFrame();

        evaluator.initialize(instance, env);
        return env;
    }

    public RelationInstanceDecl compile(String instanceString, String libraryString) {

        GlobalScope scope = new GlobalScope();

        //parse the library
        ParseTree tree = parse(libraryString, "libraryDecl");
        ParseTreeWalker walker = new ParseTreeWalker();
        ClockRDLBuilderAST builder = new ClockRDLBuilderAST(scope);

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

        //TODO define a clear error handling strategy for Parsing

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
