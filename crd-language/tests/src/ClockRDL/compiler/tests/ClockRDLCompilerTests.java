package ClockRDL.compiler.tests;

import ClockRDL.compiler.ClockRDLCompiler;
import ClockRDL.model.declarations.RepositoryDecl;
import org.eclipse.emf.common.util.URI;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;


public class ClockRDLCompilerTests {
	@Test
	public void testBlockStmt() {
		assertString(
				"library x {" +
				"relation y " +
				"clock a b; " +
				"var t; " +
				"def xx {t += 1} " +
				"{ [true]{}; }}");
	}

	@Test
	public void testBlockWithVar() {
		assertString(
				"library x {" +
						"relation y " +
						"clock a b; " +
						"var t; " +
						"def xx {var y := 3; t += y} " +
						"{ [true]{}; }}");
	}

	String blockStmt(String code) {
		return "library xx { relation y { [true]{}["+code+"]; }}";
	}

	@Test
	public void testAssignInBlock1() {
		assertString(blockStmt("{var x;}"));
	}

	@Test
	public void testAssignInBlock2() {
		assertString(blockStmt("{var x := 1; assert(x=1) }"));
	}

	@Test
	public void testAssignInBlock3() {
		assertString(blockStmt("{var x := 1; x := 2 assert(x=2)}"));
	}

	@Test
	public void testAssignInBlock4() {
		assertString(blockStmt("{var x := 1; x := 2+x assert(x=3) x += 2 assert(x=5)}"));
	}

    @Test
    public void testXMIGeneration() {
        try {
            RepositoryDecl lib = ClockRDLCompiler.compile(new File("../examples/ccsl-kernel.crd"), null);
            URI uri = ClockRDLCompiler.generateModelXMI(lib, "tmp/ccsl-kernel.xmi");
            System.out.println("XMI saved in: " + uri + "\n");

            lib = ClockRDLCompiler.compile(new File("../examples/sdf_pam.crd"), null);
			uri = ClockRDLCompiler.generateModelXMI(lib, "tmp/sdf_pam.xmi");
            System.out.println("XMI saved in: " + uri+ "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Test
	public void testBSystem() {
		assertFile("../examples/ccsl-kernel.crd");
	}
	
	@Test
	public void testExamples() {
		File testDir = new File("../examples/");
		
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
	    			java.lang.System.out.println(child.getName());
	    			assertFile(child.getPath());
	    		}
	    	}
	    }
	}

	public void assertString(String libraryString) {
		RepositoryDecl sys = null;
        sys = ClockRDLCompiler.compile(libraryString, null);
		assertTrue(sys != null);
	}
	
	public void assertFile(String filename) {
		RepositoryDecl sys = null;
		try {
			sys = ClockRDLCompiler.compile(new File(filename), null);
		} catch (IOException e) {
			java.lang.System.err.println("testing "+ filename);
			e.printStackTrace();
		}
		assertTrue("testing "+ filename, sys != null);
	}
}

