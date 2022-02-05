package compile;

import entity.MultiComment;
import entity.SingleComment;
import entity.TokenList;
import frame.OutText;
import frame.ReadText;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @description This is the concrete implementation of lexical analysis
 */
@SuppressWarnings("all")
public class Lexer extends MainComplier{

    //Lexical analysis interpretation rules, defining character arrays
    String[] keyWords = {"if", "else", "while", "read", "write", "int", "double","for"};  //array of keywords
    String[] operator = {"+", "-", "*", "/"};//operator array
    String[] roperator = {">", "<", "==", "<>", "and"};//Array of relational operators
    String[] sepretor = {";", "{", "}", "(", ")", "."};//delimiter array
    String RegexToId = "^[a-zA-Z]([a-zA-Z_0-9])*[a-zA-Z0-9]$||[a-zA-Z]";//Regular Expression for Identifiers
    String RegexToNumber = "^^-?\\d+$";//Integer regular expression
    String RegexToFloat = "^(-?\\d+)(\\.\\d+)?$";//Regular expression for floating point numbers
    String RegexToArray = "[a-zA-Z]+(\\[[0-9][1-9]*\\])+";//Regular Expression for Array Variables
    
    //Constructor
    //Inherit the readText and outText of the parent class
    public Lexer(ReadText readText, OutText outText) throws HeadlessException {
        super(readText, outText);
    }

    //The analysis process, where the modified source program is provided for parsing and lexical analysis
    public List<TokenList> getTokens() {
        List<TokenList> tokenLists = new ArrayList<>();//Information used to record Token
        String inputText = readText.getText();
        StringTokenizer totalStrt = new StringTokenizer(inputText, "\r\n");
        int row = 0;//line number
        //Get all tokens and token information
        while (totalStrt.hasMoreTokens()) {
            List<String> Tokens = new ArrayList<>();//line notation
            StringTokenizer rowOfStrt = new StringTokenizer(totalStrt.nextToken(), " \n\r\t;(){}\"\'+-<>/=*", true);
            //All possible delimiters, all Tokens are initially obtained, but further merging is required
            while (rowOfStrt.hasMoreTokens()) {
                Tokens.add(rowOfStrt.nextToken());
            }
            TokenList tokenList = new TokenList(row, Tokens);
            tokenLists.add(tokenList);
            row++;
        }
        //For further judgment and integration of the initially obtained token set, it is used to distinguish comments from *, /; and = and ==, and < and <>
        for (int i = 0; i < tokenLists.size(); i++) {
            List<String> tokenList = tokenLists.get(i).getTokenList();//Get line token group
            for (int j = 0; j < tokenList.size() - 1; j++) {
                if (tokenList.get(j).equals("/") && tokenList.get(j + 1).equals("/")) {
                    //Recognition of single-line keynotes
                    tokenList.set(j, "//");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("/") && tokenList.get(j + 1).equals("*")) {
                    //Recognition of multi-line comments
                    tokenList.set(j, "/*");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("*") && tokenList.get(j + 1).equals("/")) {
                    //Recognition of multi-line comments
                    tokenList.set(j, "*/");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("=") && tokenList.get(j + 1).equals("=")) {
                    tokenList.set(j, "==");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("<") && tokenList.get(j + 1).equals(">")) {
                    tokenList.set(j, "<>");
                    tokenList.remove(j + 1);//Judgment is not equal to sign
                }
            }
        }
        //The second time to judge and integrate the tokens, mainly used to remove various separators
        for (int i = 0; i < tokenLists.size(); i++) {
            List<String> tokenList = tokenLists.get(i).getTokenList();//Get line token group
            String Pattern = "\\s+|\t|\r\n";
            int j = 0;
            while(j<tokenList.size())
            {
                if(tokenList.get(j).matches(Pattern))
                {
                    tokenList.remove(j);
                }
                else
                {
                    j++;
                }
            }
        }
        //Uncomment the token for the third time to get the real complete token
        List<MultiComment> multiComments = new ArrayList<>();//Location information for storing multi-line comments
        List<SingleComment> singleComments = new ArrayList<>();//Location information for storing single-line comments
        for (int i = 0; i < tokenLists.size(); i++)//Token acquisition of multi-line comments
        {
            List<String> TokenOfrow = tokenLists.get(i).getTokenList();
            int rowCount = tokenLists.get(i).getRow();//Multi-line comment line number
            for (int j = 0; j < TokenOfrow.size(); j++) {
                if (TokenOfrow.get(j).equals("//")) {
                    SingleComment singleComment = new SingleComment(rowCount, j);
                    singleComments.add(singleComment);//Record single-line comment position
                }
                if (TokenOfrow.get(j).equals("/*")) {
                    MultiComment multiComment = new MultiComment(rowCount, j, "/*");//j is the column number
                    multiComments.add(multiComment);
                } else if (TokenOfrow.get(j).equals("*/")) {
                    MultiComment multiComment = new MultiComment(rowCount, j, "*/");
                    multiComments.add(multiComment);
                }
            }
        }
        for (int i = 0; i < multiComments.size(); i = i + 2)//Remove whole-line comments in multi-line comments
        {
            if ((multiComments.size() % 2) == 0 && i <= multiComments.size() - 2)//Determine if the annotation is not closed
            {
                if (multiComments.get(i).getComment().equals("/*") && multiComments.get(i + 1).getComment().equals("*/")) {
                    for (int j = multiComments.get(i).getRow() + 1; j < multiComments.get(i + 1).getRow(); j++) {
                        tokenLists.remove(j);
                    }
                    List<String> StartLine = tokenLists.get(multiComments.get(i).getRow()).getTokenList();//start of comment line
                    List<String> EndLine = tokenLists.get(multiComments.get(i + 1).getRow()).getTokenList();//end of comment line
                    for (int j = multiComments.get(i).getColumn(); j < StartLine.size(); )//Because as the element is removed, the size will also change
                    {
                        StartLine.remove(j);
                    }
                    int position = multiComments.get(i).getColumn();//position pointer
                    for (int j = 0; j <= position; )//Similarly, a reduction in the number of elements results in a change in size
                    {
                        EndLine.remove(j);
                        position--;
                    }
                }
            } else {
                outText.append("Cannot continue analysis");
                outText.append("the first" + multiComments.get(i).getRow() + "Line No." + multiComments.get(i).getColumn() + "Comment at is not closed");
                break;
            }
        }
        for (int i = 0; i < singleComments.size(); i++) {
            List<String> SignleLine = tokenLists.get(singleComments.get(i).getRow()).getTokenList();
            for (int j = singleComments.get(i).getColumn(); j < SignleLine.size(); ) {
                SignleLine.remove(j);//remove single line comments
            }
        }
        return tokenLists;
    }

    //All token processing is done, pure analytical token here
    public void Analysis() {
        List<TokenList> tokenLists = getTokens();
        for (int i = 0; i < tokenLists.size(); i++) {
            List<String> tokenList = tokenLists.get(i).getTokenList();
            outText.append("--------------------------------------------------Analyze line " + (i + 1) + "--------------------------------------------------" + "\r\n");
            for (int j = 0; j < tokenList.size(); j++) {
                int Count = 0;
                for (int k = 0; k < keyWords.length; k++) {
                    if (tokenList.get(j).equals(keyWords[k])) {
                        outText.append(tokenList.get(j) + " is the keyword" + "\r\n");
                        Count++;
                    }
                }
                for (int k = 0; k < operator.length; k++) {
                    if (tokenList.get(j).equals(operator[k])) {
                        outText.append(tokenList.get(j) + " is the operator" + "\r\n");
                        Count++;
                    }
                }
                for (int k = 0; k < roperator.length; k++) {
                    if (tokenList.get(j).equals(roperator[k])) {
                        outText.append(tokenList.get(j) + " is a relational operator" + "\r\n");
                        Count++;
                    }
                }
                for (int k = 0; k < sepretor.length; k++) {
                    if (tokenList.get(j).equals(sepretor[k])) {
                        outText.append(tokenList.get(j) + " is the delimiter" + "\r\n");
                        Count++;
                    }
                }
                if (tokenList.get(j).matches(RegexToId) && (Count == 0)) {
                    outText.append(tokenList.get(j) + " is the identifier" + "\r\n");
                } else if (tokenList.get(j).matches(RegexToNumber)) {
                    outText.append(tokenList.get(j) + " is an integer" + "\r\n");
                } else if (tokenList.get(j).matches(RegexToFloat)) {
                    outText.append(tokenList.get(j) + " is a floating point number" + "\r\n");
                } else if (tokenList.get(j).matches(RegexToArray)) {
                    outText.append(tokenList.get(j) + " is an array variable" + "\r\n");
                } else if (tokenList.get(j).equals("=")) {
                    outText.append(tokenList.get(j) + " is the equals sign" + "\r\n");
                } else if (Count == 0) {
                    outText.append(tokenList.get(j) + " Identifier named incorrectly" + "\r\n");
                }
            }
        }
    }
}
