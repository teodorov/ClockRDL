package ClockRDL.interpreter.tests;

import ClockRDL.compiler.ClockRDLBuilderAST;
import ClockRDL.compiler.ClockRDLCompiler;
import ClockRDL.compiler.GlobalScope;
import ClockRDL.compiler.Scope;
import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;
import ClockRDL.grammar.tests.ClockRDLGrammarTest;
import ClockRDL.interpreter.GlobalFrame;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.NamedDeclaration;
import ClockRDL.model.statements.BlockStmt;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import javax.swing.undo.UndoableEdit;

/**
 * Created by ciprian on 22/10/15.
 */
public class StatementEvaluatorTest {

    Interpreter evaluator = new Interpreter();

    @Test
    public void testAssignInBlock() {
        evaluate("{var x;}");
        evaluate("{var x := 1; assert(1 x) }");
        evaluate("{var x := 1; x := 2 assert(2 x)}");
        evaluate("{var x := 1; x := 2+x assert(3 x) x += 2 assert(5 x)}");
    }
    @Test
    public void enclosingScopeAccess() {
        evaluate("{var x := 1; {var y := 2 z ; assert(1 x) z := x + y assert(3 z) x := 2 assert(2 x) } }");
    }

    @Test
    public void conditionalStatementIsTrue() {
        evaluate("{var x := 1; if x = 1 { x := 3 } else { x := 5 } assert(3 x)} ");
    }
    @Test
    public void conditionalStatementIsFalse() {
        evaluate("{var x := 1; if x != 1 { x := 3 } else { x := 5 } assert(5 x)} ");
    }

    @Test
    public void loopStatement1To10() {
        evaluate("{var x := 1; while x < 10 { x += 1 } assert(10 x) }");
    }

    @Test
    public void loopStatement1To10Add() {
        evaluate("{var x := 1; while x < 10 { x := x + 1 } assert(10 x) }");
    }

    @Test(expected=RuntimeException.class)
    public void testInvalidAssertCall() {
        evaluate("{var x := 1; assert(x=2 2+3) }");
        evaluate("{var x := 1; assert(x)}");
    }

    @Test(expected=RuntimeException.class)
    public void testAssertFalse() {
        evaluate("{var x := 1; assert(true false) }");
    }

    @Test
    public void testAssertArrayTrue() {
        evaluate("{ assert([1 2] [1 2]) }");
    }

    @Test(expected=RuntimeException.class)
    public void testAssertArrayFalse() {
        evaluate("{ assert([1 2] [2 1]) }");
    }

    @Test(expected=RuntimeException.class)
    public void testUndefinedFunction() {
        compile("{var x := 1; undefined(x=1)}");
    }

    public void evaluate(String blockCode) {
        evaluator.evaluate(compile(blockCode), new GlobalFrame());
    }

    public BlockStmt compile(String expressionString) {
        ANTLRInputStream is = new ANTLRInputStream(expressionString);
        ClockRDLLexer lexer = new ClockRDLGrammarTest.ErrorThrowingLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ClockRDLParser parser = new ClockRDLParser(tokens);
        ParseTree tree = parser.blockStmt();
        ParseTreeWalker walker = new ParseTreeWalker();
        ClockRDLBuilderAST builder = new ClockRDLBuilderAST(new GlobalScope());

        //TODO define a clear error handling strategy for Parsing
        //parser.addErrorListener(new ClockRDLGrammarTest.NoErrorsForTest());

        parser.setErrorHandler(new ClockRDLGrammarTest.ThrowErrorStrategy());

        walker.walk(builder, tree);
        return builder.getValue(tree, BlockStmt.class);
    }
}
