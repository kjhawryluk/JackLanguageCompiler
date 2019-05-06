import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the compiler class, which is used to parse the tokenized xml file into the final output file.
  */
class Compiler {
    private FileWriter outputWriter;
    private Scanner scanner;
    private String currentInputLine;
    private String nextToken;
    private String className;
    private LinkedList<SymbolTable> symbolTables = new LinkedList<>();
    static Pattern tokenPattern = Pattern.compile("<.*> (.*) </.*>");
    Integer whileLabelCounter = 0;
    Integer ifLabelCounter = 0;

    Compiler(FileWriter outputWriter, Scanner scanner) {
        this.outputWriter = outputWriter;
        this.scanner = scanner;
    }

    //Moves to the next line of input file. If there's a nextToken loaded, place that into
    //the currentInputLine instead.
    private void advanceToken()
    {
        if(nextToken != null)
        {
            currentInputLine = nextToken;
            nextToken = null;
        }
        else if(scanner.hasNext())
        {
            currentInputLine = scanner.nextLine();
        }
        else
        {
            currentInputLine = null;
        }
    }

    //loads the next token into nextToken to be used in look a heads.
    private void loadNextToken()
    {
        if(scanner.hasNext())
        {
            nextToken = scanner.nextLine();
        }
        else
        {
            nextToken = null;
        }
    }

    //Generic write line method
    private  void writeLine(String line)
    {
        try {
            outputWriter.write(line + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Returns vm for operator.
    private String getOp()
    {
        switch (getTokenAndAdvance()) {
            case "+":
                return "add";
            case "-":
                return "sub";
            case "*":
                return "call Math.multiply 2";
            case "/":
                return "call Math.divide 2";
            case "&amp;":
                return "and";
            case "|":
                return "or";
            case "&lt;":
                return "lt";
            case "&gt;":
                return "gt";
            case "=":
                return "eq";
            default:
                throw new IllegalArgumentException("Operator not found. " + currentInputLine);
        }
    }

    //This is the first method called to start compiling the jack tokens into vm code for the entire class.
    void compileClass()
    {
        //Get past tokens first line.
        advanceToken();
        advanceToken();

        // advance past the keyword class line
        advanceToken();

        //write identifier classname line
        className = getTokenAndAdvance();

        // advance past the symbol lne
        advanceToken();

        symbolTables.addLast(new SymbolTable());
        //add any and all class variable declarations to symbolTable
        while(isClassVarDec())
        {
            compileClassVarDec();
        }

        //write any and all subroutine declarations
        while(isSubRoutineDec())
        {
            compileSubroutine();
        }

        validateSymbolAndAdvance("}");

    }

    //Returns true if start of class variable declaration
    private boolean isClassVarDec()
    {
        return currentInputLine.contains("static") || currentInputLine.contains("field");
    }

    //Writes the class variable declaration to the output.
    private void compileClassVarDec()
    {
        //Get keyword (static|field)
        String kind = getTokenAndAdvance();
        //Get type
        String type = getTokenAndAdvance();
        //add varName to symbol table
        addSymbolToTable(type, kind);

        //Write list of other variables.
        writeCommaDelimitedList(type, kind);

        //advance past the semicolon symbol
        advanceToken();

    }

    //Returns true if start of subroutine declaration
    private boolean isSubRoutineDec()
    {
       return currentInputLine.matches("<keyword> (constructor|function|method) </keyword>");
    }

    private void compileSubroutine()
    {
        symbolTables.addLast(new SymbolTable());
        String subroutineType = getTokenAndAdvance();
        boolean isConstructor = subroutineType.equals("constructor");
        boolean isMethod = subroutineType.equals("method");

        //Add "this" as variable to methods' symbol tables
        if(isMethod)
            addThisToTable();


        //Skip return type.
        advanceToken();

        //Subroutine name
        String subroutineName = getSubroutineNameAndAdvance(currentInputLine);

        validateSymbolAndAdvance("(");

        //Parameter list
        compileParameterList();
        validateSymbolAndAdvance(")");

        validateSymbolAndAdvance("{");

        //Record any and all variable declarations to symbol table
        while(isVarDec())
        {
            compileVarDec();
        }

        writeSubroutineDeclaration(subroutineName, isMethod, isConstructor);

        //Compile all the statements in the body.
        compileStatements();

        validateSymbolAndAdvance("}");

    }

    //Writes function f k
    private void writeSubroutineDeclaration(String subroutineName, boolean isMethod, boolean isConstructor) {
        String functionCall = "function " + subroutineName + " " + symbolTables.getLast().getVarCount("local").toString();
        writeLine(functionCall);
        if(isConstructor)
        {

            writeLine("push constant " + symbolTables.getFirst().getVarCount("this").toString() );
            writeLine("call Memory.alloc 1");
            writeLine("pop pointer 0");
        }
        else if(isMethod)
        {
            writeLine("push argument 0");
            writeLine("pop pointer 0");
        }
    }

    //Compiles the comma delimited list of parameters.
    private void compileParameterList()
    {
        String kind = "argument";

        if(!currentInputLine.equals("<symbol> ) </symbol>")) {
            //Get type
            String type = getTokenAndAdvance();
            //Get varName
            addSymbolToTable(type, kind);

            //Write list of other parameters.
            writeCommaDelimitedList(kind);
        }
    }

    //Checks if start of varDec
    private boolean isVarDec()
    {
        return currentInputLine.equals("<keyword> var </keyword>");
    }

    //Write variable declaration to output.
    private void compileVarDec()
    {
        String kind = "var";

        //advance past keyword var
        advanceToken();
        //advance past type
        String type = getTokenAndAdvance();

        //Add to symbol table and advance token
        addSymbolToTable(type, kind);

        //Write list of other variables.
        writeCommaDelimitedList(type, kind);

        //advance past the semicolon symbol
        advanceToken();
    }

    //Returns true if the current line is the start of a statement
    private boolean isStatement()
    {
        return currentInputLine.matches("<keyword> (let|while|if|do|return) </keyword>");
    }

    //Compiles all statements using appropriate compiler
    private void compileStatements()
    {
        while (isStatement())
        {
            switch (currentInputLine){
                case "<keyword> do </keyword>":
                    compileDo();
                    break;
                case "<keyword> let </keyword>":
                    compileLet();
                    break;
                case "<keyword> while </keyword>":
                    compileWhile();
                    break;
                case "<keyword> return </keyword>":
                    compileReturn();
                    break;
                case "<keyword> if </keyword>":
                    compileIf();
                    break;
                default:
                    throw new IllegalArgumentException("Not a statement: " + currentInputLine);
            }
        }
    }

    //compile a do subroutine call statement.
    private void compileDo()
    {
        //advance past do keyword
        advanceToken();
        //Compile subroutineCall Expression.
        compileSubroutineCall();

        //For void functions, pop the return value off the stack
        writeLine("pop temp 0");
        //;
        advanceToken();
    }

    //compile a let statement.
    private void compileLet()
    {
        //Advance past let keyword
        advanceToken();
        //write varName
        String varName = getTokenAndAdvance();
        SymbolLine varLine = getSymbolLine(varName);
        boolean destIsArray = isArray();

        //If the destination is an array, this writes the code to set
        //pointer 1 to the current location in the array.
        if(destIsArray)
        {
            setArrayLocation(varLine);
        }

        //advance past symbol =
        advanceToken();

        //write expression code
        compileExpression();

        if(destIsArray)
        {
            writeLine("pop temp 0");
            writeLine("pop pointer 1");
            writeLine("push temp 0");
            writeLine("pop that 0");
        }
        else
        {
            writeLine(varLine.getPop());
        }
        //;
        advanceToken();
    }

    //Writes code for setting the array location.
    private void setArrayLocation(SymbolLine varLine) {
        //[
        advanceToken();
        compileExpression();
        writeLine(varLine.getPush());
        //]
        advanceToken();
        writeLine("add");
    }

    //compile a while statement
    private void compileWhile()
    {
        String whileLabel1 = "WHILE_EXP" + whileLabelCounter.toString();
        String whileLabel2 = "WHILE_END" + whileLabelCounter.toString();
        whileLabelCounter++;
        //While keyword
        advanceToken();
        validateSymbolAndAdvance("(");

        //Start label
        writeLine("label " + whileLabel1);

        //Expression to determine if time to exit the while loop.
        compileExpression();
        writeLine("not");
        writeLine("if-goto " + whileLabel2);

        //Skip the symbols
        validateSymbolAndAdvance(")");
        validateSymbolAndAdvance("{");

        //Work to do in the while loop
        compileStatements();

        //Go back to the beginning of the while loops
        writeLine("goto " + whileLabel1);

        //Exit the loop
        writeLine("label " + whileLabel2);

        validateSymbolAndAdvance("}");

    }

    //compile return statement
    private void compileReturn()
    {
        //advance past return
        advanceToken();

        //Write expression if there is one.
        if(!currentInputLine.equals("<symbol> ; </symbol>"))
        {
            compileExpression();
        }
        else
        {
            writeLine("push constant 0");
        }

        //write return to confirm completion of subroutine.
        writeLine("return");

        //Advance past ;
        advanceToken();
    }

    //compile if statement
    private void compileIf()
    {
        String ifLabel1 = "IF_TRUE" + ifLabelCounter.toString();
        String ifLabel2 = "IF_FALSE" + ifLabelCounter.toString();
        ifLabelCounter++;

        //Skip keyword if and (
        advanceToken();
        validateSymbolAndAdvance("(");

        compileExpression();
        writeLine("not");
        writeLine("if-goto " + ifLabel1);

        //Skip symbols
        validateSymbolAndAdvance(")");
        validateSymbolAndAdvance("{");

        //Statement 1
        compileStatements();

        writeLine("goto " + ifLabel2);
        writeLine("label " + ifLabel1);
        //Skip symbols
        validateSymbolAndAdvance("}");

        if(currentInputLine.equals("<keyword> else </keyword>"))
        {
            //Skip keyword else
            advanceToken();
            validateSymbolAndAdvance("{");

            compileStatements();

            validateSymbolAndAdvance("}");
        }
        writeLine("label " + ifLabel2);
    }

    //Checks if start of array expression.
    private boolean isArray()
    {
        return currentInputLine.equals("<symbol> [ </symbol>");
    }

    //Checks if current line is an operator
    private boolean isOp()
    {
        String operator = currentInputLine.replace("<symbol> ","").replace(" </symbol>","");
        return operator.matches("(\\+|-|\\*|/|&amp;|\\||&lt;|&gt;|=)");
    }

    //Compile an expression by compiling the term and then writing all subsequent operators and terms.
    private void compileExpression()
    {
        compileTerm();
        while(isOp())
        {
            //write operator
            String op = getOp();
            compileTerm();
            writeLine(op);
        }
    }

    //Method for dertmining what type of term is in the current line and directing it to write the appropriate vm code
    private void compileTerm()
    {
        if(currentInputLine.matches("(<integerConstant> (.*) </integerConstant>)"))
        {
            writeLine("push constant " + getTokenAndAdvance());
        }
        else if(currentInputLine.matches("<keyword> (this|true|false|null) </keyword>"))
        {
            writeKeyword();
        }
        else if(currentInputLine.matches("(<stringConstant> (.*) </stringConstant>)"))
        {
            writeStringConstruction();
        }
        else if(currentInputLine.equals("<symbol> ( </symbol>"))
        {
            validateSymbolAndAdvance("(");

            compileExpression();

            validateSymbolAndAdvance(")");
        }
        else if(currentInputLine.matches("<symbol> (~|-) </symbol>"))
        {
            //Write unary op
            String unaryOp = getUnarySymbol();
            compileTerm();
            writeLine(unaryOp);
        }
        else if(currentInputLine.startsWith("<identifier>"))
        {
            compileIdentifierTerm();
        }

    }

    //This compiles the identifier term by determining if its a symbol, array or subroutine.
    private void compileIdentifierTerm() {
        loadNextToken();

        if(nextToken.matches("<symbol> (\\(|\\.) </symbol>"))
        {
            compileSubroutineCall();
        }
        else
        {
            //Write varName or subroutineName
            String varName = getTokenAndAdvance();
            SymbolLine symbolLine = getSymbolLine(varName);

            //Checks if this is an array and will compile if it is.
            if(isArray())
            {
                setArrayLocation(symbolLine);
                writeLine("pop pointer 1");
                writeLine("push that 0");
            }
            else
            {
                writeLine(symbolLine.getPush());
            }
        }
    }

    //Write the keyword vm to output.
    private void writeKeyword() {
        String keyword = getTokenAndAdvance();
        switch (keyword)
        {
            case "this":
                writeLine("push pointer 0");
                break;
            case "true":
                writeLine("push constant 0");
                writeLine("not");
                break;
            case "false":
            case "null":
                writeLine("push constant 0");
        }
    }

    //Returns unaray ops in their vm code.
    private String getUnarySymbol()
    {
        String symbol = getTokenAndAdvance();
        if(symbol.equals("-"))
        {
            return "neg";
        }
        else
        {
            return "not";
        }
    }

    //Compiles a subroutine call.
    private void compileSubroutineCall()
    {
        String callStatement = "call ";
        Integer argCount = 0;
        //get identifier. This will be a subroutineName, varName or className
        String symbol = getTokenAndAdvance();

        //Checks if this is a subroutine called from a variable or class.
        if(currentInputLine.equals("<symbol> . </symbol>"))
        {
            if(isSymbol(symbol))
            {
                SymbolLine symbolLine = getSymbolLine(symbol);
                writeLine(symbolLine.getPush());
                symbol = symbolLine.getType();
                argCount++;
            }
            callStatement += symbol;

            //Write symbol .
            callStatement += getTokenAndAdvance();
            //write subroutineName
            callStatement += getTokenAndAdvance();
        }
        else
        {
            callStatement += getSubroutineName(symbol);
            writeLine("push pointer 0");
            argCount++;
        }

        validateSymbolAndAdvance("(");
        argCount += compileExpressionList();
        validateSymbolAndAdvance(")");

        callStatement += " " + argCount.toString();
        writeLine(callStatement);
    }



    //Throws an error if unexpected symbol is in next line
    private void validateSymbolAndAdvance(String symbol)
    {
        if(!currentInputLine.equals("<symbol> " + symbol + " </symbol>"))
            throw new IllegalArgumentException("Unexpected Symbol. Expected: " + symbol + ", but found: " + currentInputLine);
        advanceToken();
    }
    
    //Compiles an expressionList
    private int compileExpressionList()
    {
        int countOfArguments = 0;
        if(!currentInputLine.equals("<symbol> ) </symbol>"))
        {
            countOfArguments++;
            compileExpression();
            //If this is not the end of the list, continue writing commas and expressions
            //until the current line is a )
            while(!currentInputLine.equals("<symbol> ) </symbol>"))
            {
                countOfArguments++;
                //advance past comma
                advanceToken();
                compileExpression();
            }

        }
        return countOfArguments;
    }

    //Adds comma delimited lists to the symbol table
    private void writeCommaDelimitedList(String type, String kind)
    {
        while(currentInputLine.contains(","))
        {
            //advance past the symbol comma line
            advanceToken();
            //add var to symbol table and advance
            addSymbolToTable(type, kind);
        }
    }

    //Adds comma delimited lists to the symbol table for parameter lists.
    private void writeCommaDelimitedList(String kind) {
        while(currentInputLine.contains(","))
        {
            //write the symbol comma line, the varType, and the var name.
            advanceToken();
            //varType
            String type = getTokenAndAdvance();
            //varName
            addSymbolToTable(type, kind);
        }
    }

    //Adds a symbol to the symbol table.
    private void addSymbolToTable(String type, String kind) {
        symbolTables.getLast().addLine(currentInputLine, type, kind);
        advanceToken();
    }

    //Adds reference to current object "this" to the symbol table.
    private void addThisToTable() {
        symbolTables.getLast().addLine("this", className, "argument");
    }

    //Look at line and strip out tags.
    static String getTokenFromLine(String line) {
        Matcher tokenMatcher = tokenPattern.matcher(line);
        //Return token.
        if (tokenMatcher.find()) {
            return tokenMatcher.group(1);
        }

        return line;
    }

    //Returns the current token.
    private String getTokenAndAdvance()
    {
        String token = getTokenFromLine(currentInputLine);
        advanceToken();
        return token;
    }

    //Gets the subroutine name with the class name appended.
    private String getSubroutineName(String subroutineName)
    {
        String strippedSubroutineName = className + "." + getTokenFromLine(subroutineName);
        return strippedSubroutineName;
    }

    //Gets the subroutine name with the class name appended.
    private String getSubroutineNameAndAdvance(String subroutineName)
    {
        String strippedSubroutineName = className + "." + getTokenFromLine(subroutineName);
        advanceToken();
        return strippedSubroutineName;
    }

    //Searches symbolTable for symbolLine
    private SymbolLine getSymbolLine(String varName)
    {
        SymbolTable table = symbolTables.getLast();

        //Check current scope
        if(table.hasSymbol(varName))
            return table.getLine(varName);

        //Check class scope
        table = symbolTables.getFirst();
        if(table.hasSymbol(varName))
            return table.getLine(varName);

        throw new IllegalArgumentException("No symbol found named: " + varName);
    }

    //Searches symbolTable for symbolLine
    private boolean isSymbol(String varName)
    {
        SymbolTable table = symbolTables.getLast();

        //Check current scope
        if(table.hasSymbol(varName))
            return true;

        //Check class scope
        table = symbolTables.getFirst();
        if(table.hasSymbol(varName))
            return true;

        return false;
    }

    //Constructs a string if a stringConstant is found.
    private void writeStringConstruction()
    {
        String stringConstant = getTokenAndAdvance();
        stringConstant = stringConstant.replace("\"", "");
        Integer stringLen = stringConstant.length();
        if(stringLen > 0) {
            writeLine("push constant " + stringLen.toString());
            writeLine("call String.new 1");
            for (char character : stringConstant.toCharArray()) {
                Integer charNum = (int) character;
                writeLine("push constant " + charNum.toString());
                writeLine("call String.appendChar 2");
            }
        }
    }
}

