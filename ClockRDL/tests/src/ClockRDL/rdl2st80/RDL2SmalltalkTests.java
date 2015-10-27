package ClockRDL.rdl2st80;

import ClockRDL.compiler.ClockRDLCompiler;
import ClockRDL.model.declarations.RepositoryDecl;
import org.junit.Test;

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

    public void assertString(String libraryString) {
        RepositoryDecl sys = ClockRDLCompiler.compile(libraryString);
        RDL2Smalltalk transformer = new RDL2Smalltalk();

        transformer.convert(sys);

        assertTrue(sys != null);
    }
}
