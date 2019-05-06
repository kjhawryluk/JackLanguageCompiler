/**
 * This is the driver class. It starts the program and calls the main methods necessary for stripping
 * comments, creating tokenized files of the jack files and then creating final output vm translations of each jack file.
 */
public class JackCompilerMain {


    public static void main(String[] args) {
        String fileName = null;
        //Use file name from args if provided.
        if(args.length > 0)
        {
            fileName = args[0];
        }

        //Create a VirtualMachineTranslator to store the input and output file info.
        JackCompiler jackCompiler =
                JackCompiler.createJackCompiler(fileName);

        //Read through each input file and create a temp file without comments.
        jackCompiler.createTmpFileNoComments();


        try {
            //Reads the temp file(s), tokenizes each line and writes a vm file for each jack file
            jackCompiler.createTokenOutput();
            jackCompiler.createFinalOutput();
        }
        //If the code catches any exception. It will delete the output file and then re-throw the exception.
        catch (Exception anyException)
        {
            jackCompiler.deleteTmpFiles();
            jackCompiler.deleteTokenOutputFiles();
            jackCompiler.deleteOutputFiles();
            throw anyException;
        }
        jackCompiler.deleteTokenOutputFiles();
        jackCompiler.deleteTmpFiles();
        System.out.println("Output File Complete.");
    }
}
