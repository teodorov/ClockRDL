package ClockRDL.grammar.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

public class ClockRDLGrammarTest {
	private String currentRule;


    @Test
    public void importDecl() {
        setCurrentRule("importDecl");
        assertParse("import \"mocos.crd\"");
    }
	@Test	
	public void booleanLiteral() {
		setCurrentRule("booleanLiteral");
		assertParse("true");
		assertParse("false");
	}

	@Test
	public void integerLiteral() {
		setCurrentRule("integerLiteral");
		assertParse("123");
		assertParse("758");
		assertParse("32");
		assertParse("3");
	}

    @Test
    public void arrayLiteral() {
        setCurrentRule("arrayLiteral");
        assertParse("[]");
        assertParse("[1]");
        assertParse("[true]");
        assertParse("[1 2 3 4]");
        assertParse("[true false true]");
        assertParse("[[true false] [false true]]");
        assertParse("[2+3 5%4]");
    }

    @Test
    public void queueLiteral() {
        setCurrentRule("queueLiteral");
        assertParse("{||}");
        assertParse("{|1|}");
        assertParse("{|true|}");
        assertParse("{|1 2 3 4|}");
        assertParse("{|true false true|}");
        assertParse("{|{|true false|} {|false true|}|}");
        assertParse("{|[1 2] [44 22] [3 4]|}");
        assertParse("{|1 2 3+2 4|}");
    }

    @Test
    public void recordLiteral() {
        setCurrentRule("recordLiteral");
        assertParse("{a=2}");
        assertParse("{a=2 b=3}");
        assertParse("{a=2 b=[1 2]}");
        assertParse("{a={|2|} b=[1 2]}");
        assertParse("{a=true b=[[1] [2]]}");
        assertParse("{a=2/2 b=3*5}");
    }

    @Test
    public void literal() {
        setCurrentRule("literal");
        assertParse("true");
        assertParse("2340");
        assertParse("[1 2 3]");
        assertParse("{x=2 y = 4}");
        assertParse("{|[12 34]|}");
    }

    @Test
    public void expression() {
        setCurrentRule("expression");
        assertParse("true");
        assertParse("123");
        assertParse("toto");
        assertParse("toto.x");
        assertParse("toto[3]");
        assertParse("toto[3][2]");
        assertParse("toto.x[3].y.w");
        assertParse("23 + 32");
        assertParse("23 - 32");
        assertParse("23 * 32");
        assertParse("23 / 32");
        assertParse("23 % 32");
        assertParse("! b");
        assertParse("+ b");
        assertParse("- b");
        assertParse("a < b");
        assertParse("a <= b");
        assertParse("a > b");
        assertParse("a >= b");
        assertParse("a = b");
        assertParse("a != b");
        assertParse("true | false");
        assertParse("a nor b");
        assertParse("a xor b");
        assertParse("a nand b");
        assertParse("a & b");
        assertParse("(a nand b) | (toto[2] & toto[3])");
        assertParse("{|a|}.first()");
        assertParse("{|a|}.removeFirst()");
        assertParse("{|a|}.isEmpty()");
        assertParse("{|a|}.includes(b c)");
        assertParse("function(2 x [a b])");
    }

    @Test
    public void conditionalExpression() {
        setCurrentRule("expression");
        assertParse("a < b ? 23 : 45");
    }

    @Test
    public void functionCallStmt(){
        setCurrentRule("statement");
        assertParse("f(x)");
        assertParse("f(x x*2)");
        assertParse("a.f(x x*2)");
        assertParse("a[4].f(x x*2)");
        assertParse("f(x=2)");
    }

    @Test
    public void functionCallInBlock() {
        setCurrentRule("statement");
        assertParse("{assert(x=1)}");
    }

    @Test
    public void functionCallInBlockWithVar() {
        setCurrentRule("statement");
        assertParse("{var x := 1; assert(1)}");
        assertParse("{var x := 1; assert(x=1) }");
    }

    @Test
    public void assignmentStmt() {
        setCurrentRule("assignmentStmt");
        assertParse("a := b");
        assertParse("a += b");
        assertParse("a -= b");
        assertParse("a *= b");
        assertParse("a /= b");
        assertParse("a %= b");
        assertParse("a |= b");
        assertParse("a &= b");
        assertParse("a -= 2");
        assertParse("a -= 2+3*(2-a)");
    }

    @Test
    public void conditionalStmt() {
        setCurrentRule("conditionalStmt");
        assertParse("if a < b { c := 3 }");
        assertParse("if a < b { c := 3 } else { f(x) }");
    }

    @Test
    public void loopStmt() {
        setCurrentRule("loopStmt");
        assertParse("while a<b {d := 5}");
    }

    @Test
    public void returnStmt() {
        setCurrentRule("returnStmt");
        assertParse("return x+3");
    }

    @Test
    public void blockStmt() {
        setCurrentRule("blockStmt");
        assertParse("{}");
        assertParse("{a:=2}");
        assertParse("{var x := 2;}");
        assertParse("{const y := 3;}");
        assertParse("{var z;}");
        assertParse("{var x; x:=z+1}");
        assertParse("{var x y := 5; const T; x:=z+1*y/T}");
    }

    @Test
    public void variableDecl() {
        setCurrentRule("variableDecl");
        assertParse("var a;");
        assertParse("var b := 3;");
        assertParse("var a b:=3 c:=true d:=[1 2];");
    }

    @Test
    public void constantDecl() {
        setCurrentRule("constantDecl");
        assertParse("const a;");
        assertParse("const b := 3;");
        assertParse("const a b:=3 c:=true d:=[1 2];");
    }

    @Test
    public void clockDecl() {
        setCurrentRule("clockDecl");
        assertParse("clock a;");
        assertParse("clock a b;");
    }

    @Test
    public void functionDecl() {
        setCurrentRule("functionDecl");
        assertParse("def x {return 2}");
        assertParse("def x {a := 3 return 2}");
        assertParse("def x(a) {a := 3 return 2}");
        assertParse("def x(a b c) {a := 3 if d < 3 { return 2 } else { return a } }");
    }

    @Test
    public void emptyPrimitiveRelation() {
        setCurrentRule("relationDecl");
        assertParse("relation r {}");
    }


    @Test
	public void testExamples() {
		File testDir = new File("../examples/");
		System.out.println(new File(".").getAbsolutePath());
		
		list(testDir);
	}
	public void list(File file) {
	    File[] children = file.listFiles();
	    for (File child : children) {
	    	if (child.isDirectory()) {
	    		list(child);
	    	}
	    	else {
	    		if (child.getName().endsWith(".crd")) {
	    			System.out.println(child.getName());
	    			assertFile(child.getPath());
	    		}
	    	}
	    }
	}
	
	public ParseTree assertFile(String filename) {
		ParseTree result = parseFile(filename);
		assertTrue("testing "+ filename, result != null);
		return result;
	}
	
	public ParseTree parseFile(String filename) {
		ANTLRFileStream fs;
		try {
			fs = new ANTLRFileStream(filename);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return parse(fs, "systemDecl");
	}
	
	public ParseTree parse(CharStream cs, String rule) {
		ClockRDLLexer 		lexer 	= new ErrorThrowingLexer(cs);
		CommonTokenStream 	tokens 	= new CommonTokenStream(lexer);
		Parser 				parser 	= new ClockRDLParser(tokens);
		NoErrorsForTest  	errorL  = new NoErrorsForTest();

		parser.removeErrorListeners();
		parser.addErrorListener(errorL);

        parser.setErrorHandler(new ThrowErrorStrategy());
		try {
			Method mtd = parser.getClass().getMethod(rule);
			ParseTree pt = (ParseTree)mtd.invoke(parser);
			if (errorL.hasErrors()) return null;
			return pt; 
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Compilation error (" + e.getCause().getLocalizedMessage() +" "+ e.getMessage() + ")");
		}
	}

    public static class ErrorThrowingLexer extends ClockRDLLexer {
        public ErrorThrowingLexer(CharStream input) { super(input); }
        public void recover(LexerNoViableAltException e) {
            throw new RuntimeException(e);
        }
    }

    public static class ThrowErrorStrategy extends DefaultErrorStrategy {
        //see The Definitive ANTLR4 Reference for more details on this class
        @Override
        public void recover(Parser recognizer, RecognitionException e) {
            throw new RuntimeException(e);
        }

        @Override
        public Token recoverInline(Parser recognizer) throws RecognitionException {
            throw new RuntimeException(new InputMismatchException(recognizer));
        }

        @Override
        public void sync(Parser recognizer) throws RecognitionException {}
    }

	public static class NoErrorsForTest extends BaseErrorListener {
		private Boolean hasErrors = false;
		@Override
		public void syntaxError(Recognizer<?, ?> rec, Object offendingSymbol, int line, int column, String msg, RecognitionException e) {
			hasErrors = true;
            throw new RuntimeException(e.getLocalizedMessage());
		}
		public Boolean hasErrors() {
			return hasErrors;
		}
	}
	class ClockRDLFileFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return name.endsWith(".crd");
		}
	}
	public void setCurrentRule(String currentRule) {
		this.currentRule = currentRule;
	}
	public ParseTree parse(String input, String rule) {
		ANTLRInputStream 	is		= new ANTLRInputStream(input);
		return parse(is, rule);
	}
	public ParseTree assertParse(String input) {
		ParseTree result = parse(input, currentRule);
		assertTrue(result != null);
		return result;
	}
}
