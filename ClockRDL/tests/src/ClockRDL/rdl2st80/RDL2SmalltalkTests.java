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
                "\t\t[state] \t{a} [state := ! state]\n" +
                "\t\t[! state] {b} [state := ! state]\n" +
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
                "\t\t[state + 1 <= offset & doesFilteredTicks() ] { baseClock filtered } [ state += 1 ]\n" +
                "\t\t[state + 1 <= offset & ! doesFilteredTicks() ] { baseClock } [ state += 1 ]\n" +
                "\n" +
                "\t\t[state + 1 > offset & doesFilteredTicks() ] { baseClock filtered } [ nextState() ]\n" +
                "\t\t[state + 1 > offset & ! doesFilteredTicks() ] { baseClock } [ nextState() ]\n" +
                "\t}\n" +
                "}";
        assertString(lib);
    }

    public void assertString(String libraryString) {
        RepositoryDecl sys = ClockRDLCompiler.compile(libraryString);
        RDL2Smalltalk transformer = new RDL2Smalltalk();

        transformer.convert(sys);

        assertTrue(sys != null);
    }
}
