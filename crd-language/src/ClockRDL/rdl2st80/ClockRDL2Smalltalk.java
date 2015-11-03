package ClockRDL.rdl2st80;

import ClockRDL.compiler.ClockRDLCompiler;
import ClockRDL.model.declarations.RepositoryDecl;

import java.io.*;

/**
 * Created by ciprian on 29/10/15.
 */
public class ClockRDL2Smalltalk {
    public static void main(String[] args) {
        File inFile = null;
        File outFile = null;

        if (args.length < 4) {
            System.err.println("usage: ClockRDL2Smalltalk -in <.crd filename> -out <.st filename>\n");
            return;
        }

        int i=0;
        while (i < args.length) {
            if (args[i].equals("-in")) {
                i++;
                inFile = new File(args[i]);
            }
            else if (args[i].equals("-out")) {
                i++;
                outFile = new File(args[i]);
            }
            else {
                System.err.println("usage: ClockRDL2Smalltalk -in <.crd filename> -out <.st filename>\n");
                return;
            }
            i++;
        }

        if (inFile == null || outFile == null) {
            System.err.println("ERROR: could not get the inputfile or the outputfile\n");
        }
        ClockRDL2Smalltalk instance = new ClockRDL2Smalltalk();
        instance.generateSmalltalk(inFile, outFile);
    }

    public void generateSmalltalk(File inFile, File outFile) {
        try {
            RepositoryDecl repo = ClockRDLCompiler.compile(inFile);

            RDL2Smalltalk transformer = new RDL2Smalltalk();

            String result = transformer.convert(repo);

            BufferedWriter br = new BufferedWriter(new FileWriter(outFile));

            br.write(result);
            br.close();

        }catch (IOException | RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }
}
