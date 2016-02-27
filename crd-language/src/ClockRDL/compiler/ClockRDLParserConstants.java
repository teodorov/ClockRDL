package ClockRDL.compiler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ClockRDL.grammar.ClockRDLLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class ClockRDLParserConstants {
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
}