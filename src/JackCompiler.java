import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * The JackCompiler object stores the info on the input and output files, and it provides the main driving methods
 * for the conversion process.
 */
class JackCompiler {

    private String fileNameAndPath = null;

    //This is only used if the input arg points to a directory.
    private File[] inputFiles = null;
    private List<File> tmpFiles = new ArrayList<>();
    private List<File> tokenOutputFiles = new ArrayList<>();
    private List<File> outputFiles = new ArrayList<>();
    final static String IN_EXTENSION = ".jack";
    final static String TMP_EXTENSION = ".tmp";
    final static String TOUT_EXTENSION = "T.xml";
    final static String OUT_EXTENSION = ".vm";

    //This constructor parses the argument provided as input and tries to create the inputFiles
    //if possible. inputFiles will include input files that end with the .jack extension. If the path is to a jack file,
    //the array will be of size 1, otherwise, it will contain all .jack files in the directory.
    private JackCompiler(String args) {
        if (args != null) {
            File fileFromPath = new File(args);

            //Check if files exist at args path.
            if (fileFromPath.exists()) {

                //Set fileName
                fileNameAndPath = fileFromPath.getAbsolutePath();

                //Find inputFile(s)
                if (fileFromPath.isDirectory()) {
                    inputFiles = fileFromPath.listFiles((file) -> file.getName().endsWith(IN_EXTENSION));
                } else if (fileFromPath.getName().endsWith(IN_EXTENSION)) {
                    inputFiles = new File[1];
                    inputFiles[0] = fileFromPath;
                }
            }
        }
    }

    //Create the JackCompiler. If given an argument that points to usable files, this will return that.
    //otherwise it will ask the user to enter appropriate info in order to find the input to be used and wrapped in the
    //JackCompiler object.
    static JackCompiler createJackCompiler(String arg) {

        //Build assembly converter from args.
        JackCompiler jackCompiler = new JackCompiler(arg);


        //Try to find files at provided path. If none exist, print an error and let them try again  or type Quit to exit.
        while (jackCompiler.getFileNameAndPath() == null || jackCompiler.getInputFiles() == null ||
                jackCompiler.getInputFiles().length == 0) {
            System.out.println("Please enter an appropriate file name." +
                    " If you wish to exit this program, enter Quit.");
            Scanner in = new Scanner(System.in);

            //Get the input and try to create another JackCompiler.
            if (in.hasNext()) {
                String newInput = in.nextLine();

                //Check if user wants to quit.
                if (newInput.toLowerCase().equals("quit")) {
                    System.out.println("Good Bye!");
                    System.exit(0);
                }

                //Try to create it based on the new user input.
                jackCompiler = new JackCompiler(newInput);

            }
        }
        return jackCompiler;
    }


    //This reads in the input files and strips the comments, writing the output to a tmp file.
    void createTmpFileNoComments() {

            //Create a reader and writer to read the input file and write a tmp file
            for (File inputFile : inputFiles) {
                File outputFile = createFile(inputFile, IN_EXTENSION, TMP_EXTENSION, tmpFiles);
                try (FileWriter outputWriter = new FileWriter(outputFile)) {
                //For each line, check if it's a symbol, variable, or computation.
                try (Scanner reader = new Scanner(inputFile)) {

                    boolean inBulkComment = false;

                    while (reader.hasNext()) {
                        String line = reader.nextLine();

                        if(!inBulkComment) {
                            //Bulk comment starts in this line.
                            if(line.contains("/*") && !line.contains("*/"))
                            {
                                inBulkComment = true;
                            }
                            //This strips extra whitespace and comments.
                            line = stripCommentsAndLineBreaks(line);

                            if(line != null && line.length() > 0)
                            {
                                //Write the remaining Jack code to a tmp file.
                                outputWriter.write(line + "\n");
                            }
                        }
                        else if(line.contains("*/"))
                        {
                            inBulkComment = false;
                        }
                    }
                }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    //This reads in the tmp files and converts them to the token xml output.
    void createTokenOutput() {
        //Create a reader and writer to read the input file and write a tmp tokenized file
        for (File tmpFile : tmpFiles) {
            File outputFile = createFile(tmpFile, TMP_EXTENSION, TOUT_EXTENSION, tokenOutputFiles);
            try (FileWriter outputWriter = new FileWriter(outputFile)) {
                outputWriter.write("<tokens>\n");
                try (Scanner reader = new Scanner(tmpFile)) {

                    while (reader.hasNext()) {
                        String line = reader.nextLine();
                        int charNum = 0;
                        Tokenizer tokenizer = new Tokenizer(outputWriter, line, charNum);
                        tokenizer.writeTokenToTokenOutput();
                    }
                }
                outputWriter.write("</tokens>\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //This reads in the token files and converts them to the final vm output.
    void createFinalOutput() {
        //Create a reader and writer to read the token file and write a vm file
        for (File tokenFile : tokenOutputFiles) {
            File outputFile = createFile(tokenFile, TOUT_EXTENSION, OUT_EXTENSION, outputFiles);
            try (FileWriter outputWriter = new FileWriter(outputFile)) {
                try (Scanner reader = new Scanner(tokenFile)) {
                    Compiler compiler = new Compiler(outputWriter, reader);
                    compiler.compileClass();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Create file with correct extension and add to respective list.
    private File createFile(File inputFile, String inputExtension, String outputExtension, List<File> targetArray)
    {
        String fileName = inputFile.getAbsolutePath();
        String tmpFileName = fileName.replace(inputExtension, outputExtension);
        File newTmpFile = new File(tmpFileName);
        targetArray.add(newTmpFile);
        return newTmpFile;
    }

    //This strips in line comments
    private static String stripCommentsAndLineBreaks(String line) {

        //If there's only white space on a line, return null.
        if(line.matches("[\\s]"))
            return null;

        //Remove comments
        String[] splitLine = line.split("//|/\\*|\\*/");

        //If there's anything left, return it. Otherwise, return null.
        if (splitLine.length > 0) {
            return splitLine[0];
        } else {
            return null;
        }
    }

    //Delete the output file upon error.
    void deleteOutputFiles() {

        outputFiles.forEach(File::delete);
    }

    //Delete the token output file upon error.
    void deleteTokenOutputFiles() {

        tokenOutputFiles.forEach(File::delete);
    }

    //Delete the temporary file once the process is done or if there's an error.
    void deleteTmpFiles() {

        tmpFiles.forEach(File::delete);
    }

    //Getters
    private String getFileNameAndPath() {
        return fileNameAndPath;
    }

    private File[] getInputFiles() {
        return inputFiles;
    }


}
