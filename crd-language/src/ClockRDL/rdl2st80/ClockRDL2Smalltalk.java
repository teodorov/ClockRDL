package ClockRDL.rdl2st80;

import ClockRDL.compiler.ClockRDLCompiler;
import ClockRDL.model.declarations.RepositoryDecl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by ciprian on 29/10/15.
 */
public class ClockRDL2Smalltalk {
    static String usage = "usage: ClockRDL2Smalltalk [-libraryPath <'|' separated list of filepaths>] -in <.crd filename> -out <.st filename>\n";
    public static void main(String[] args) {
        File inFile = null;
        File outFile = null;
        List<URI> libPaths = new ArrayList<>();

        if (args.length < 4) {
            System.err.println(usage);
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
            else if (args[i].equals("-libraryPath")) {
                i++;
                String paths = args[i];
                StringTokenizer sT = new StringTokenizer(paths, "|");
                while (sT.hasMoreElements()) {
                    String path = sT.nextToken();
                    path = path.replaceAll("\"","");
                    File f = new File(path);
                    libPaths.add(f.toURI());
                    if (!f.exists()) {
                        System.err.println("Invalid URI: " + path);
                        throw new RuntimeException("Invalid URI: " + path);
                    }
                }
            }
            else {
                System.err.println(usage);
                return;
            }
            i++;
        }

        if (inFile == null || outFile == null) {
            System.err.println("ERROR: could not get the inputfile or the outputfile\n");
        }
        ClockRDL2Smalltalk instance = new ClockRDL2Smalltalk();
        instance.generateSmalltalk(inFile, outFile, libPaths);
    }

    public void generateSmalltalk(File inFile, File outFile, List<URI> libPaths) {
        try {
            RepositoryDecl repo = ClockRDLCompiler.compile(inFile, libPaths);

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
