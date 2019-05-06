# Jack Compiler

Overview: This is a project for my Intro to Computer Systems course. For this project, I wrote a compiler to compile Jack code to run a virtual machine. Jack is a basic objet oriented language designed for the nand2tetris tutorial from here: https://www.nand2tetris.org/

**Compilation Instructions:**

In order to compile my program, please use terminal and navigate to the src folder contained in this folder. Type the following command, and press enter:

javac JackCompilerMain.java

This will compile the source code and create a java class file in the folder. You can now run the program according to the instructions below. 

How to run the code:

If you just compiled the program, you can use your active terminal window. If not, open terminal, navigate to the src folder contained in this folder. As assigned, this program will read in an input file (with the extension .jack) or a directory containing 1 or more .jack files and writes 1 output files for each input file, which will be [YourFileName].vm and have a vm translation of the corresponding jack file. The output files will be written into the directory where the target jack files are held according to the source path. (i.e. if you provide a source path to a directory, the output will write into that directory. If you provide a path to a .jack file, the output will be in the same folder as that file.) If the code meets an exception, the output and tmp files will be deleted.

File Path:
You may enter either an absolute file path such as "/Usr/.../directory_of_inputs/mytext.jack" (changed to reflect the actual location of your input file or folder) or a relative file path from the src directory to your input file/folder. The relative path could be as simple as "myinput.jack" if the file is in src directory, or it could use relative pathing directions such as "../directory_of_inputs/myinput.jack" if the input file is in another folder called directory_of_inputs that you place in this folder. Note: the quotes above are used to explicitly highlight examples of possible file paths; however, your path should not have quotes around it. 

To run the code, from the src directory in terminal, type:
java VmToAsmMain path-to-your-input-file-or-folder

Three Examples:
Absolute Path 
java VmToAsmMain /Users/kevinhawryluk/Documents/Computer_Systems/Kevin_Hawryluk_Project6/directory_of_inputs/mytext.jack

Relative Path
java VmToAsmMain ../directory_of_inputs/myinput.jack

Relative Path To Folder Containing Jack files.
java VmToAsmMain ../directory_of_inputs

Description of what works in this project:
This project effectively reads in an absolute or relative file path to a jack file or folder containing jack files. If you enter an incorrect path, the input file does not have a .jack extension, or the input file is unable to be read by the program, then terminal will respond with "Please enter an appropriate file name. If you wish to exit this program, enter Quit.". From here, if you re-enter your argument with a correct path to an appropriate input file/folder, the program will run as expected. If it cannot find the file again, it will respond with the same message. You can also enter Quit (not case sensitive) to exit the program if you choose. With the file path, the program first creates tmp files for each input file that strip blank lines and comments (both in line and bulk comments). It then reads through each tmp file and creates a tokenized output file for each tmp file, [YourFileName]T.xml. It then reads each tokenized file and creates a respective final output file by parsing each token and translating it into vm code. At the end, it will delete the token and tmp files, leaving just the jack and vm files in the directory. If it hits an error, it will print the error to terminal and delete the tmp, tokenized output and output files. It attempts to do some basic syntax validation by throwing an error if it expects a specific symbol and gets something else. 

Possible bugs:
Overall the program works and passes all the tests outlined in the book. Syntax validation could be expanded, and the code could probably be tightened up. I had some difficulty getting the VMEmulator to let me enter a number to convert to binary; however, as of writing this, it appears to work and be able to pass the test. Also, I modeled my if statements after how the book explained to do it, but when comparing the output to the compilation from the nand2tetris compiler, the code looks a bit different. This does not appear to effect performance at all. 