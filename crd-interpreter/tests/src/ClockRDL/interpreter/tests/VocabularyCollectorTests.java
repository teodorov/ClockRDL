package ClockRDL.interpreter.tests;

import ClockRDL.compiler.ClockRDLCompiler;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.values.ClockValue;
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
    Set<ClockValue> clocks;
    Set<ClockValue> free;
    Set<ClockValue>[] vocabularies;

    @After
    public void teardown() {
        interpreter.reset();
        clocks = null;
        free = null;
        vocabularies = null;
    }

    @Test
    public void fourInstances4Clocks() {
        String sys = "library l { relation r clock c; {  } relation r1  { clock[c] clock[d] r(c: c) r(c: d) } relation r2 { r1() r1() } } l.r2() }";
        compile(sys);

        Assert.assertEquals(4, clocks.size());

        Assert.assertEquals(0, free.size());

        Assert.assertEquals(4, vocabularies.length);
        Assert.assertEquals(1, vocabularies[0].size());
        Assert.assertEquals(1, vocabularies[1].size());
        Assert.assertEquals(1, vocabularies[2].size());
        Assert.assertEquals(1, vocabularies[3].size());
        Assert.assertNotSame(vocabularies[0].toArray(new ClockValue[1])[0], vocabularies[1].toArray(new ClockValue[1])[0]);
    }

    @Test
    public void twoInstances2Clocks() {
        String sys = "library l { relation r clock c; {  } relation r1  { clock[c] clock[d] r(c: c) r(c: d) } } l.r1() }";
        compile(sys);

        Assert.assertEquals(2, clocks.size());

        Assert.assertEquals(0, free.size());

        Assert.assertEquals(2, vocabularies.length);
        Assert.assertEquals(1, vocabularies[0].size());
        Assert.assertEquals(1, vocabularies[1].size());
        Assert.assertNotSame(vocabularies[0].toArray(new ClockValue[1])[0], vocabularies[1].toArray(new ClockValue[1])[0]);
    }

    @Test
    public void oneInternalFreeClock() {
        String sys = "library l { relation r clock c; {  } relation r1  { clock[c] clock[d] r(c: c) } } l.r1() }";
        compile(sys);

        Assert.assertEquals(2, clocks.size());

        Assert.assertEquals(1, free.size());

        Assert.assertEquals(1, vocabularies.length);
        Assert.assertEquals(1, vocabularies[0].size());
    }

    @Test
    public void oneInternalClock() {
        String sys = "library l { relation r clock c; {  } relation r1  { clock[c] r(c: c) } } l.r1() }";
        compile(sys);

        Assert.assertEquals(1, clocks.size());

        Assert.assertEquals(0, free.size());

        Assert.assertEquals(1, vocabularies.length);
        Assert.assertEquals(1, vocabularies[0].size());
    }

    @Test
    public void oneDefaultClock() {
        String sys = "library l { relation r clock a := clock[b]; {  } } l.r() }";
        compile(sys);

        Assert.assertEquals(1, clocks.size());

        Assert.assertEquals(0, free.size());

        Assert.assertEquals(1, vocabularies.length);
        Assert.assertEquals(1, vocabularies[0].size());
    }

    @Test
    public void passingOneConstrainedClock() {
        String sys = "library l { relation r clock c; {  } relation r1 clock c; { r(c: c) } } l.r1(c: clock[b]) }";
        compile(sys);

        Assert.assertEquals(1, clocks.size());

        Assert.assertEquals(0, free.size());

        Assert.assertEquals(1, vocabularies.length);
        Assert.assertEquals(1, vocabularies[0].size());
    }

    @Test
    public void oneUnconstrainedClock() {
        String sys = "library l { relation r {  } relation r1 { clock[b] r() } } l.r1() }";
        compile(sys);

        Assert.assertEquals(1, clocks.size());

        Assert.assertEquals(1, free.size());

        Assert.assertEquals(1, vocabularies.length);
        Assert.assertEquals(0, vocabularies[0].size());
    }

    @Test
    public void oneConstrainedClock() {
        String sys = "library l { relation r clock a; {  } } l.r(a: clock[b]) }";
        compile(sys);

        Assert.assertEquals(1, clocks.size());

        Assert.assertEquals(0, free.size());

        Assert.assertEquals(1, vocabularies.length);
        Assert.assertEquals(1, vocabularies[0].size());
    }

    public void compile(String input) {
        SystemDecl system = (SystemDecl)ClockRDLCompiler.compile(input, null);
        interpreter.initialize(system.getRoot());
        clocks = interpreter.getAllClocks();
        free = interpreter.getFreeClocks();
        vocabularies = interpreter.getVocabularies();
    }
}
