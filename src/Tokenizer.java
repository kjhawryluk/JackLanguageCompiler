import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is created for each line of a file to tokenize the input and write to the tokenized output file.
 */
public class Tokenizer {

    private FileWriter outputWriter;
    private String line;
    private int charNum;
    private char currentCharacter;
    private String stringToTokenize;
    private static String listOfSymbols = "{}[]().,;+-*/&|<>=~";
    private static String keywordRegex = "^(class|constructor|function|method|" +
            "field|static|var|int|char|boolean|void|" +
            "true|false|null|this|let|do|if|else|while|return)(?![a-zA-Z_]).*";
    private static Pattern keywordPattern = Pattern.compile("(class|constructor|function|method|" +
            "field|static|var|int|char|boolean|void|" +
            "true|false|null|this|let|do|if|else|while|return)(?![a-zA-Z_]).*");
    private static Pattern startingDigits = Pattern.compile("^([0-9]+).*");
    private static Pattern identifierPat = Pattern.compile("^([a-zA-Z_]+).*");


    //Constructor for a tokenizer to be made for each line parsed.
    Tokenizer(FileWriter outputWriter, String line, int charNum) {
        this.outputWriter = outputWriter;
        this.line = line;
        this.charNum = charNum;
        this.stringToTokenize = line.substring(charNum);
        this.currentCharacter = stringToTokenize.charAt(0);
    }

    //This determines what type of command is stored in a line, and then it writes that command to the output.
    void writeTokenToTokenOutput() throws IOException
    {
        while(charNum < line.length()) {
            if (Character.isDigit(currentCharacter)) {
                tokenizeIntegerConst();
            }
            else if(listOfSymbols.indexOf(currentCharacter) > -1)
            {
                tokenizeSymbol();
            }
            else if(currentCharacter == '"')
            {
                tokenizeStringConst();
            }
            else if(stringToTokenize.matches(keywordRegex))
            {
                tokenizeKeyword();
            }
            else if(Character.isLetter(currentCharacter) || currentCharacter == '_')
            {
                tokenizeIdentifier();
            }
            else
            {
                updateStringTokenize(1);
            }
        }
    }

    //Finds end of int, writes tokenized line to output, and updates char num.
    private void tokenizeIntegerConst() throws IOException {
        Matcher intMatcher = startingDigits.matcher(stringToTokenize);
        if(intMatcher.find())
        {
        String integerConst = intMatcher.group(1);
        int integerLength = integerConst.length();
        outputWriter.write("<integerConstant> " + integerConst + " </integerConstant>\n");
        updateStringTokenize(integerLength);
    }
        else
    {
        System.out.println(stringToTokenize);
    }
    }

    //Writes tokenized line to output, and advanced charNum by 1.
    private void tokenizeSymbol() throws IOException
    {
        String symbol;
        switch(currentCharacter)
        {
            case '>':
                symbol = "&gt;";
                break;
            case '<':
                symbol = "&lt;";
                break;
            case '&':
                symbol = "&amp;";
                break;
            default:
                symbol = String.valueOf(currentCharacter);
        }

        outputWriter.write("<symbol> " + symbol + " </symbol>\n");
        updateStringTokenize(1);
    }

    //Finds end of string, writes tokenized line to output, and updates char num.
    private void tokenizeStringConst() throws IOException
    {
        //Strip first quote
        updateStringTokenize(1);

        //Pull out everything before the next quote and strip that quote.
        int indexOfNextQuote = stringToTokenize.indexOf('"');
        String stringConst = stringToTokenize.substring(0, indexOfNextQuote);

        //write to output.
        outputWriter.write("<stringConstant> " + stringConst + " </stringConstant>\n");
        updateStringTokenize(indexOfNextQuote + 1);
    }

    //Finds edn of keyword, write tokenized line to output and updates char num of letters in keyword.
    private void tokenizeKeyword() throws IOException
    {
        Matcher keywordMatcher = keywordPattern.matcher(stringToTokenize);
        if(keywordMatcher.find()) {
            String keywordConst = keywordMatcher.group(1);
            int keywordLength = keywordConst.length();
            outputWriter.write("<keyword> " + keywordConst + " </keyword>\n");
            updateStringTokenize(keywordLength);
        }
        else
        {
            System.out.println(stringToTokenize);
        }
    }

    //Find end of identifier, write tokenized line to output and update the char num the num letters in identifier
    private void tokenizeIdentifier() throws IOException
    {
        Matcher identifierMatcher = identifierPat.matcher(stringToTokenize);
        if(identifierMatcher.find()) {
            String identifier = identifierMatcher.group(1);
            int identifierLength = identifier.length();
            outputWriter.write("<identifier> " + identifier + " </identifier>\n");
            updateStringTokenize(identifierLength);
        }
        else
        {
            System.out.println(stringToTokenize);
        }
    }

    //Checks if end of the line. Otherwise, advance to string to tokenize
    //and current character.
    private void updateStringTokenize(int charactersToAdvance) throws IOException {

        charNum += charactersToAdvance;

        //Otherwise it's the end of the line.
        if(charNum < line.length())
        {
            stringToTokenize = line.substring(charNum);
            currentCharacter = stringToTokenize.charAt(0);
        }
    }
}
