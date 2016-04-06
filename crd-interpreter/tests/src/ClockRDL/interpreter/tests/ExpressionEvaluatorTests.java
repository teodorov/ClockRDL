package ClockRDL.interpreter.tests;

import ClockRDL.compiler.ClockRDLBuilderAST;
import ClockRDL.compiler.ClockRDLParserConstants;
import ClockRDL.compiler.GlobalScope;
import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.values.ArrayValue;
import ClockRDL.interpreter.values.BooleanValue;
import ClockRDL.interpreter.values.IntegerValue;
import ClockRDL.model.kernel.Expression;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.After;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by ciprian on 20/10/15.
 */
public class ExpressionEvaluatorTests {
    Interpreter evaluator = new Interpreter();

    @After
    public void teardown() {
        evaluator.reset();
    }

    @Test
    public void testArithmetic2Plus3() {
        Expression exp = compileExp("2+3");
        Value result = evaluator.evaluate(exp);

        assertEquals(true, result.isIntegerValue());
        assertEquals(5, ((IntegerValue) result).getData());
    }

    @Test
    public void testArithmetic2Minus3() {
        Expression exp = compileExp("2-3");
        Value result = evaluator.evaluate(exp);

        assertEquals(true, result.isIntegerValue());
        assertEquals(-1, ((IntegerValue) result).getData());
    }

    @Test
    public void testArithmetic2Mult3() {
        Expression exp = compileExp("2*3");
        Value result = evaluator.evaluate(exp);

        assertEquals(true, result.isIntegerValue());
        assertEquals(6, ((IntegerValue) result).getData());
    }

    @Test
    public void testArithmetic2Mod3() {
        Expression exp = compileExp("2%3");
        Value result = evaluator.evaluate(exp);

        assertEquals(true, result.isIntegerValue());
        assertEquals(2, ((IntegerValue) result).getData());
    }

    @Test
    public void testArithmetic2Div3() {
        Expression exp = compileExp("2/3");
        Value result = evaluator.evaluate(exp);

        assertEquals(true, result.isIntegerValue());
        assertEquals(0, ((IntegerValue) result).getData());
    }

    @Test
    public void testRelational() {
        Value result = evaluator.evaluate(compileExp("2<3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("2<=3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("3<=3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("3=3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("2!=3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(true, ((BooleanValue) result).getData());

        result = evaluator.evaluate(compileExp("3>2"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("3>=2"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("3>=3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("2<3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("2<=3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("3<=3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("3!=3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.evaluate(compileExp("2=3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.evaluate(compileExp("3<2"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.evaluate(compileExp("3<=2"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.evaluate(compileExp("3<=3"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);
    }

    @Test
    public void testArrayLiteralIndex() {
        Value result = evaluator.evaluate(compileExp("[1 2 3][0]"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(1, ((IntegerValue)result).getData());

        result = evaluator.evaluate(compileExp("[1 2 3][1]"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(2, ((IntegerValue)result).getData());

        result = evaluator.evaluate(compileExp("[1 2 3][2]"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(3, ((IntegerValue)result).getData());
    }

    @Test
    public void testArrayLiteralSize() {
        Value result = evaluator.evaluate(compileExp("[1 2 3].size()"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(3, ((IntegerValue)result).getData());
    }

    @Test
    public void testArrayLiteralSizeAdd() {
        Value result = evaluator.evaluate(compileExp("[1 2 3].size() + [1 2].size()"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(5, ((IntegerValue)result).getData());
    }

    @Test
    public void testQueueLiteralSize() {
        Value result = evaluator.evaluate(compileExp("{|1 2 3|}.size()"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(3, ((IntegerValue)result).getData());
    }

    @Test
    public void testQueueLiteralAdd() {
        Value result = evaluator.evaluate(compileExp("{|1 2 3|}.add(5)"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);
    }

    @Test
    public void testQueueLiteralAddFirst() {
        Value result = evaluator.evaluate(compileExp("{|1 2 3|}.addFirst(5)"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);
    }

    @Test
    public void testQueueLiteralAddLast() {
        Value result = evaluator.evaluate(compileExp("{|1 2 3|}.addLast(5)"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);
    }


    @Test
    public void testQueueLiteralRemove() {
        Value result = evaluator.evaluate(compileExp("{|1 2 3|}.remove()"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(1, ((IntegerValue)result).getData());

        result = evaluator.evaluate(compileExp("{| [1 2] 3|}.remove().size()"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(2, ((IntegerValue)result).getData());
    }

    @Test
    public void testQueueLiteralRemoveFirst() {
        Value result = evaluator.evaluate(compileExp("{|1 2 3|}.removeFirst()"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(1, ((IntegerValue)result).getData());

        result = evaluator.evaluate(compileExp("{| [1 2] 3|}.removeFirst().size()"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(2, ((IntegerValue)result).getData());
    }

    @Test
    public void testQueueLiteralRemoveLast() {
        Value result = evaluator.evaluate(compileExp("{|1 2 3|}.removeLast()"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(3, ((IntegerValue)result).getData());

        result = evaluator.evaluate(compileExp("{| 3 [1 2] |}.removeLast().size()"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(2, ((IntegerValue)result).getData());
    }

    @Test
    public void testQueueLiteralFirst() {
        Value result = evaluator.evaluate(compileExp("{|55 2 3|}.first()"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(55, ((IntegerValue)result).getData());

        result = evaluator.evaluate(compileExp("{| [1 2] 3|}.first()"));
        assertEquals(true, result.isArrayValue());
        assertEquals(2, ((ArrayValue)result).data.length);
        assertEquals(1, ((IntegerValue)((ArrayValue)result).data[0]).getData());
        assertEquals(2, ((IntegerValue)((ArrayValue)result).data[1]).getData());
    }

    @Test
    public void testQueueLiteralLast() {
        Value result = evaluator.evaluate(compileExp("{|55 2 3|}.last()"));
        assertEquals(true, result.isIntegerValue());
        assertEquals(3, ((IntegerValue)result).getData());

        result = evaluator.evaluate(compileExp("{| 3 [1 2] |}.last()"));
        assertEquals(true, result.isArrayValue());
        assertEquals(2, ((ArrayValue)result).data.length);
        assertEquals(1, ((IntegerValue)((ArrayValue)result).data[0]).getData());
        assertEquals(2, ((IntegerValue)((ArrayValue)result).data[1]).getData());
    }

    @Test
    public void testBooleanShortCircuit() {
        Value result = evaluator.evaluate(compileExp("false & {||}.first()"));
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.evaluate(compileExp("true | {||}.first()"));
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("true nor {||}.first()"));
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.evaluate(compileExp("false nand {||}.first()"));
        assertEquals(BooleanValue.TRUE, result);
    }

    @Test
    public void testQueueLiteralIsEmpty() {
        Value result = evaluator.evaluate(compileExp("{|1 2 3|}.isEmpty()"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.evaluate(compileExp("{||}.isEmpty()"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);
    }

    @Test
    public void testQueueLiteralIsNotEmpty() {
        Value result = evaluator.evaluate(compileExp("{|1 2 3|}.isNotEmpty()"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.evaluate(compileExp("{||}.isNotEmpty()"));
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.FALSE, result);
    }

    //TODO test expressions using variables defined in the environment

    public Expression compileExp(String expressionString) {
        ANTLRInputStream is = new ANTLRInputStream(expressionString);
        ClockRDLLexer lexer = new ClockRDLLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ClockRDLParser parser = new ClockRDLParser(tokens);
        ParseTree tree = parser.expression();
        ParseTreeWalker walker = new ParseTreeWalker();
        ClockRDLBuilderAST builder = new ClockRDLBuilderAST(new GlobalScope(), null);
        parser.addErrorListener(new ClockRDLParserConstants.NoErrorsForTest());

        walker.walk(builder, tree);
        return builder.getValue(tree, Expression.class);
    }
}
