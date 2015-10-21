package ClockRDL.interpreter.tests;

import ClockRDL.compiler.ClockRDLBuilderAST;
import ClockRDL.compiler.Scope;
import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;

import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.values.ArrayValue;
import ClockRDL.interpreter.values.BooleanValue;
import ClockRDL.interpreter.values.IntegerValue;


import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.NamedDeclaration;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;


import org.junit.Test;
import static junit.framework.Assert.*;

/**
 * Created by ciprian on 20/10/15.
 */
public class InterpreterTests {
    Interpreter evaluator = new Interpreter();

    @Test
    public void testArithmetic2Plus3() {
        Expression exp = compileExp("2+3");
        Value result = evaluator.eval(exp, null);

        assertEquals(true, result.isIntegerValue());
        assertEquals(5, ((IntegerValue) result).data);
    }

    @Test
    public void testArithmetic2Minus3() {
        Expression exp = compileExp("2-3");
        Value result = evaluator.eval(exp, null);

        assertEquals(true, result.isIntegerValue());
        assertEquals(-1, ((IntegerValue) result).data);
    }

    @Test
    public void testArithmetic2Mult3() {
        Expression exp = compileExp("2*3");
        Value result = evaluator.eval(exp, null);

        assertEquals(true, result.isIntegerValue());
        assertEquals(6, ((IntegerValue) result).data);
    }

    @Test
    public void testArithmetic2Mod3() {
        Expression exp = compileExp("2%3");
        Value result = evaluator.eval(exp, null);

        assertEquals(true, result.isIntegerValue());
        assertEquals(2, ((IntegerValue) result).data);
    }

    @Test
    public void testArithmetic2Div3() {
        Expression exp = compileExp("2/3");
        Value result = evaluator.eval(exp, null);

        assertEquals(true, result.isIntegerValue());
        assertEquals(0, ((IntegerValue) result).data);
    }

    @Test
    public void testRelational() {
        Value result = evaluator.eval(compileExp("2<3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.eval(compileExp("2<=3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.eval(compileExp("3<=3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.eval(compileExp("3=3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.eval(compileExp("2!=3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(true, ((BooleanValue) result).data);

        result = evaluator.eval(compileExp("3>2"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.eval(compileExp("3>=2"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.eval(compileExp("3>=3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.eval(compileExp("2<3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.eval(compileExp("2<=3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.eval(compileExp("3<=3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);

        result = evaluator.eval(compileExp("3!=3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.eval(compileExp("2=3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.eval(compileExp("3<2"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.eval(compileExp("3<=2"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.eval(compileExp("3<=3"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);
    }

    @Test
    public void testArrayLiteralIndex() {
        Value result = evaluator.eval(compileExp("[1 2 3][0]"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(1, ((IntegerValue)result).data);

        result = evaluator.eval(compileExp("[1 2 3][1]"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(2, ((IntegerValue)result).data);

        result = evaluator.eval(compileExp("[1 2 3][2]"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(3, ((IntegerValue)result).data);
    }

    @Test
    public void testArrayLiteralSize() {
        Value result = evaluator.eval(compileExp("[1 2 3].size()"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(3, ((IntegerValue)result).data);
    }

    @Test
    public void testArrayLiteralSizeAdd() {
        Value result = evaluator.eval(compileExp("[1 2 3].size() + [1 2].size()"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(5, ((IntegerValue)result).data);
    }

    @Test
    public void testQueueLiteralSize() {
        Value result = evaluator.eval(compileExp("{|1 2 3|}.size()"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(3, ((IntegerValue)result).data);
    }

    @Test
    public void testQueueLiteralAdd() {
        Value result = evaluator.eval(compileExp("{|1 2 3|}.add(5)"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);
    }

    @Test
    public void testQueueLiteralAddFirst() {
        Value result = evaluator.eval(compileExp("{|1 2 3|}.addFirst(5)"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);
    }

    @Test
    public void testQueueLiteralAddLast() {
        Value result = evaluator.eval(compileExp("{|1 2 3|}.addLast(5)"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);
    }


    @Test
    public void testQueueLiteralRemove() {
        Value result = evaluator.eval(compileExp("{|1 2 3|}.remove()"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(1, ((IntegerValue)result).data);

        result = evaluator.eval(compileExp("{| [1 2] 3|}.remove().size()"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(2, ((IntegerValue)result).data);
    }

    @Test
    public void testQueueLiteralRemoveFirst() {
        Value result = evaluator.eval(compileExp("{|1 2 3|}.removeFirst()"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(1, ((IntegerValue)result).data);

        result = evaluator.eval(compileExp("{| [1 2] 3|}.removeFirst().size()"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(2, ((IntegerValue)result).data);
    }

    @Test
    public void testQueueLiteralRemoveLast() {
        Value result = evaluator.eval(compileExp("{|1 2 3|}.removeLast()"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(3, ((IntegerValue)result).data);

        result = evaluator.eval(compileExp("{| 3 [1 2] |}.removeLast().size()"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(2, ((IntegerValue)result).data);
    }

    @Test
    public void testQueueLiteralFirst() {
        Value result = evaluator.eval(compileExp("{|55 2 3|}.first()"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(55, ((IntegerValue)result).data);

        result = evaluator.eval(compileExp("{| [1 2] 3|}.first()"), null);
        assertEquals(true, result.isArrayValue());
        assertEquals(2, ((ArrayValue)result).data.length);
        assertEquals(1, ((IntegerValue)((ArrayValue)result).data[0]).data);
        assertEquals(2, ((IntegerValue)((ArrayValue)result).data[1]).data);
    }

    @Test
    public void testQueueLiteralLast() {
        Value result = evaluator.eval(compileExp("{|55 2 3|}.last()"), null);
        assertEquals(true, result.isIntegerValue());
        assertEquals(3, ((IntegerValue)result).data);

        result = evaluator.eval(compileExp("{| 3 [1 2] |}.last()"), null);
        assertEquals(true, result.isArrayValue());
        assertEquals(2, ((ArrayValue)result).data.length);
        assertEquals(1, ((IntegerValue)((ArrayValue)result).data[0]).data);
        assertEquals(2, ((IntegerValue)((ArrayValue)result).data[1]).data);
    }

    @Test
    public void testQueueLiteralIsEmpty() {
        Value result = evaluator.eval(compileExp("{|1 2 3|}.isEmpty()"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.FALSE, result);

        result = evaluator.eval(compileExp("{||}.isEmpty()"), null);
        assertEquals(true, result.isBooleanValue());
        assertEquals(BooleanValue.TRUE, result);
    }

    public Expression compileExp(String expressionString) {
        ANTLRInputStream is = new ANTLRInputStream(expressionString);
        ClockRDLLexer lexer = new ClockRDLLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ClockRDLParser parser = new ClockRDLParser(tokens);
        ParseTree tree = parser.expression();
        ParseTreeWalker walker = new ParseTreeWalker();
        ClockRDLBuilderAST builder = new ClockRDLBuilderAST(new Scope<NamedDeclaration>("global"));

        try {
            walker.walk(builder, tree);
            return builder.getValue(tree, Expression.class);

        } catch (Error e) {
            java.lang.System.err.println(e.getMessage());
            return null;
        }
    }
}
