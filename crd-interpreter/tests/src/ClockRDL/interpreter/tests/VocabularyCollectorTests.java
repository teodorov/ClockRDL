package ClockRDL.interpreter.tests;

import ClockRDL.compiler.ClockRDLCompiler;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.model.declarations.SystemDecl;
import ClockRDL.model.expressions.literals.ClockLiteral;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * Created by ciprian on 13/11/15.
 */
public class VocabularyCollectorTests {

    Interpreter interpreter = new Interpreter();

    @After
    public void teardown() {
        interpreter.reset();
    }

    @Test
    public void oneConstrainedClock() {
        String sys = "library l { relation r clock a; {  } } l.r(a: clock[b]) }";
        compile(sys);

        Set<ClockLiteral> clocks = interpreter.getAllClocks();
        Assert.assertEquals(1, clocks.size());

        Set<ClockLiteral> free = interpreter.getFreeClocks();
        Assert.assertEquals(0, free.size());

        Set<ClockLiteral>[] vocabularies = interpreter.getVocabularies();
        Assert.assertEquals(1, vocabularies.length);
        Assert.assertEquals(1, vocabularies[0].size());
    }

    public void compile(String input) {
        SystemDecl system = (SystemDecl)ClockRDLCompiler.compile(input, null);
        interpreter.initialize(system.getRoot());
        interpreter.collectVocabularies(system.getRoot());
    }
}
