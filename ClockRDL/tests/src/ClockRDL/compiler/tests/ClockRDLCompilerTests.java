package ClockRDL.compiler.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import ClockRDL.compiler.ClockRDLCompiler;
import ClockRDL.model.declarations.LibraryDecl;
import ClockRDL.model.declarations.Repository;
import org.eclipse.emf.common.util.URI;
import org.junit.Test;


public class ClockRDLCompilerTests {
	@Test
	public void testBlockStmt() {
		assertString(
				"library x {" +
				"relation y " +
				"clock a b " +
				"var t " +
				"def xx {t += 1} " +
				"{ [true]{} }}");
	}

	@Test
	public void testBlockWithVar() {
		assertString(
				"library x {" +
						"relation y " +
						"clock a b " +
						"var t " +
						"def xx {var y = 3 t += y} " +
						"{ [true]{} }}");
	}

    @Test
    public void testXMIGeneration() {
        try {
            Repository lib = ClockRDLCompiler.compile(new File("examples/ccsl-kernel.crd"));
            URI uri = ClockRDLCompiler.generateModelXMI(lib, "examples/ccsl-kernel.xmi");
            System.out.println("XMI saved in: " + uri + "\n");

            lib = ClockRDLCompiler.compile(new File("examples/sdf_pam.crd"));
			uri = ClockRDLCompiler.generateModelXMI(lib, "examples/sdf_pam.xmi");
            System.out.println("XMI saved in: " + uri+ "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Test
	public void testBSystem() {
		assertFile("examples/ccsl-kernel.crd");
	}
	
	@Test
	public void testExamples() {
		File testDir = new File("examples/");
		
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
		Repository sys = null;
        sys = ClockRDLCompiler.compile(libraryString);
		assertTrue(sys != null);
	}
	
	public void assertFile(String filename) {
		Repository sys = null;
		try {
			sys = ClockRDLCompiler.compile(new File(filename));
		} catch (Exception e) {
			java.lang.System.err.println("testing "+ filename);
			e.printStackTrace();
		}
		assertTrue("testing "+ filename, sys != null);
	}
}

