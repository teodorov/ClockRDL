package ClockRDL.rdl2st80;

import ClockRDL.compiler.ClockRDLBuilderAST;
import ClockRDL.compiler.ClockRDLCompiler;
import ClockRDL.compiler.GlobalScope;
import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;
import ClockRDL.interpreter.Environment;
import ClockRDL.model.declarations.RelationInstanceDecl;
import ClockRDL.model.declarations.RepositoryDecl;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by ciprian on 26/10/15.
 */
public class RDL2SmalltalkTests {

    @Test
    public void test1() {
        String lib = "library kernel {\n" +
                "\trelation alternates\n" +
                "\t\tclock a b;\n" +
                "\t\tvar state := true;\n" +
                "\t{\n" +
                "\t\t[state] \t{a} [state := ! state];\n" +
                "\t\t[! state] {b} [state := ! state];\n" +
                "\t}\n" +
                "}";
        assertString(lib);
    }

    @Test
    public void test2() {
        String lib = "library kernel {\n" +
                "\trelation filtering\n" +
                "\t\tclock baseClock filtered;\n" +
                "\t\tvar state := 0; //I think that the filtering clock starts at 0\n" +
                "\t\tconst binaryWord offset;\n" +
                "\t\n" +
                "\t\tdef doesFilteredTicks {\n" +
                "\t\t\tif state + 1 <= binaryWord.size() & binaryWord[state+1] = 1 {\n" +
                "\t\t\t\treturn true\n" +
                "\t\t\t}\n" +
                "\t\t\treturn false \n" +
                "\t\t}\n" +
                "\n" +
                "\t\tdef nextState {\n" +
                "\t\t\tif ! (offset = binaryWord.size() & state + 1 > offset) { \n" +
                "\t\t\t\tif state + 1 = binaryWord.size() {\n" +
                "\t\t\t\t\tstate := offset\n" +
                "\t\t\t\t} else {\n" +
                "\t\t\t\t\tstate += 1\n" +
                "\t\t\t\t}\n" +
                "\t\t\t} \n" +
                "\t\t}\n" +
                "\t\n" +
                "\t{\n" +
                "\t\t[state + 1 <= offset & doesFilteredTicks() ] { baseClock filtered } [ state += 1 ];\n" +
                "\t\t[state + 1 <= offset & ! doesFilteredTicks() ] { baseClock } [ state += 1 ];\n" +
                "\n" +
                "\t\t[state + 1 > offset & doesFilteredTicks() ] { baseClock filtered } [ nextState() ];\n" +
                "\t\t[state + 1 > offset & ! doesFilteredTicks() ] { baseClock } [ nextState() ];\n" +
                "\t}\n" +
                "}";
        assertString(lib);
    }

    @Test
    public void testScheduler4() {
        String sched = "library mocos {\n" +
                "\trelation scheduler4\n" +
                "\t\tclock schedule1 schedule2 schedule3 schedule4 execute1 execute2 execute3 execute4 block1 block2 block3 block4 stop1 stop2 stop3 stop4;\n" +
                "\t\tvar isExecuting := false queue := {||} eID := 0;\n" +
                "\t\tconst isPreemptive := false;\n" +
                "\n" +
                "\t{\n" +
                "\t    //schedule tasks that are ready\n" +
                "\t\t[!isExecuting] { schedule1 } [queue.add(1)];\n" +
                "\t\t[!isExecuting] { schedule2 } [queue.add(2)];\n" +
                "\t\t[!isExecuting] { schedule3 } [queue.add(3)];\n" +
                "\t\t[!isExecuting] { schedule4 } [queue.add(4)];\n" +
                "\t\t//start the task at the beginning of the queue\n" +
                "\t\t[!isExecuting & queue.isNotEmpty() & queue.first() = 1] { execute1 } [{ isExecuting := true eID := queue.removeFirst() }];\n" +
                "\t\t[!isExecuting & queue.isNotEmpty() & queue.first() = 2] { execute2 } [{ isExecuting := true eID := queue.removeFirst() }];\n" +
                "\t\t[!isExecuting & queue.isNotEmpty() & queue.first() = 3] { execute2 } [{ isExecuting := true eID := queue.removeFirst() }];\n" +
                "\t\t[!isExecuting & queue.isNotEmpty() & queue.first() = 4] { execute2 } [{ isExecuting := true eID := queue.removeFirst() }];\n" +
                "\n" +
                "        //I think that the scheduling transitions should be available no matter what\n" +
                "        //This is based on the observation that a task that is not ready won't try to schedule itself\n" +
                "        [isExecuting & eID = 2] { schedule1 } [ queue.add(1) ];\n" +
                "        [isExecuting & eID = 2] { schedule3 } [ queue.add(3) ];\n" +
                "        [isExecuting & eID = 2] { schedule4 } [ queue.add(4) ];\n" +
                "        [isExecuting & eID = 1] { schedule2 } [ queue.add(2) ];\n" +
                "        [isExecuting & eID = 1] { schedule3 } [ queue.add(3) ];\n" +
                "        [isExecuting & eID = 1] { schedule4 } [ queue.add(4) ];\n" +
                "        [isExecuting & eID = 3] { schedule1 } [ queue.add(1) ];\n" +
                "        [isExecuting & eID = 3] { schedule2 } [ queue.add(2) ];\n" +
                "        [isExecuting & eID = 3] { schedule4 } [ queue.add(4) ];\n" +
                "        [isExecuting & eID = 4] { schedule1 } [ queue.add(1) ];\n" +
                "        [isExecuting & eID = 4] { schedule2 } [ queue.add(2) ];\n" +
                "        [isExecuting & eID = 4] { schedule4 } [ queue.add(3) ];\n" +
                "\n" +
                "        //continue the task execution -- Jerome wants this removed\n" +
                "\t\t[isExecuting & eID = 1] { execute1 };\n" +
                "\t\t[isExecuting & eID = 2] { execute2 };\n" +
                "\t\t[isExecuting & eID = 3] { execute3 };\n" +
                "\t\t[isExecuting & eID = 4] { execute4 };\n" +
                "\n" +
                "\t\t//if the executing task blocks, go back to not isExecuting\n" +
                "\t\t//if the task executing is not controlled by the scheduler the executing clock would disappear\n" +
                "\t\t[isExecuting & eID = 1] { execute1 block1 } [{ isExecuting := false eID := 0 }];\n" +
                "\t\t[isExecuting & eID = 2] { execute2 block2 } [{ isExecuting := false eID := 0 }];\n" +
                "\t\t[isExecuting & eID = 3] { execute3 block3 } [{ isExecuting := false eID := 0 }];\n" +
                "\t\t[isExecuting & eID = 4] { execute4 block4 } [{ isExecuting := false eID := 0 }];\n" +
                "\n" +
                "\t\t//if the scheduler is preemptive and the queue is not empty then it can decide to stop the executing task at any time\n" +
                "\t\t[isExecuting & eID = 1 & isPreemptive & queue.isNotEmpty() ] { stop1 } [{ isExecuting := false eID := 0 }];\n" +
                "\t\t[isExecuting & eID = 2 & isPreemptive & queue.isNotEmpty() ] { stop2 } [{ isExecuting := false eID := 0 }];\n" +
                "\t\t[isExecuting & eID = 3 & isPreemptive & queue.isNotEmpty() ] { stop3 } [{ isExecuting := false eID := 0 }];\n" +
                "\t\t[isExecuting & eID = 4 & isPreemptive & queue.isNotEmpty() ] { stop4 } [{ isExecuting := false eID := 0 }];\n" +
                "\t}\n" +
                "}";
        assertString(sched);
    }

    @Test
    public void testComposite() {
        String comp = "library shared {\n" +
                "    relation r1\n" +
                "        var x;\n" +
                "    {\n" +
                "        {}[x := (x + 1) % 3];\n" +
                "    }\n" +
                "\n" +
                "    relation c1\n" +
                "        // 'a' is passed by value\n" +
                "        // no subrelation can change it so\n" +
                "        // it does not make sense to declare it as a variable\n" +
                "        const a := 5;\n" +
                "    {\n" +
                "        i1:r1(x: a)\n" +
                "        i2:r1(x: a)\n" +
                "    }\n" +
                "}";
        assertString(comp);
    }


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

    @Test
    public void testInstanceNoClocks() {
        String result = transformInstance(simpleInstance, simpleLib);
        assertNotNull(result);
    }

    @Test
    public void testInstanceWithClocks() {
        String result = transformInstance(simpleInstanceWithClocks, simpleLib);
        assertNotNull(result);
    }

    public String transformInstance(String blockCode, String libraryString) {
        RelationInstanceDecl instance = compile(blockCode, libraryString);

        RDL2Smalltalk transformer = new RDL2Smalltalk();

        return  transformer.convert(instance);
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


    public void assertString(String libraryString) {
        RepositoryDecl sys = ClockRDLCompiler.compile(libraryString);
        RDL2Smalltalk transformer = new RDL2Smalltalk();

        transformer.convert(sys);

        assertTrue(sys != null);
    }
}
