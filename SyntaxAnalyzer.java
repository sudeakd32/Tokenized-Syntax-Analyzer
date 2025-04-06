import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

enum TokenType{
    type, identifier, expression, condition, punctuation, returnStatement
}

class Token {
    private final TokenType type;
    private final String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Token{" + "type = " + type + ", value = '" + value + '\'' + '}';
    }
}

public class SyntaxAnalyzer {
    static int statementIndex = 1;
    static String output = "";
    public static void main(String[] args) {

        String input = readInputFile();

        input = fixBracesAndSemicolons(input);

        List<String> statements = splitStatements(input);

        for(String statement : statements) {
            if(statement.contains("if") || statement.contains("while")){
                analyzeSyntaxForConditional(statement);
            }
            else{
                analyzeSyntaxForNonConditionStatements(analyzeTokensForNonConditional(statement));
            }
            statementIndex++;
        }

        writeOutputFile();
        readOutputFile();

    }

    public static void writeOutputFile() {

        String fileName = "C:\\Users\\sude\\IdeaProjects\\206Hw1\\src\\output.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(output);
        } catch (Exception e) {
            System.out.println("Could not write file!");
        }

    }
    public static void readOutputFile() {
        String fileName = "output.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (Exception e) {
            System.out.println("Could not read file!");
        }

    }
    public static String readInputFile(){

        String fileName = "input.txt";
        String input = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                input = input + line + "\n";
            }

        } catch (Exception e) {
            System.out.println("Could not read file!");
        }
        return input;

    }

    //Split statements for ; or { and }
    public static List<String> splitStatements(String input){
        List<String> statements = new ArrayList<>();
        String currentBlock = "";
        boolean insideBlock = false;
        int braceCount = 0;

        for (int i=0; i<input.length(); i++) {
            char c = input.charAt(i);
            currentBlock += c;

            if (c == '{') {
                braceCount++;
                insideBlock = true;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) { //If block is closed
                    statements.add(currentBlock.trim());
                    currentBlock = "";
                    insideBlock = false;
                }
            } else if (c == ';' && !insideBlock) { //If we are not inside of block
                statements.add(currentBlock.trim());
                currentBlock = "";
            }
        }

        if (!currentBlock.isBlank()) {
            statements.add(currentBlock.trim());
        }

        return statements;

    }

    //Detect statements without terminating ;. Detect unmatched braces.
    //Show error message and replace them so that we can check rest of the code
    public static String fixBracesAndSemicolons(String input) {
        List<String> lines = new ArrayList<>(Arrays.asList(input.split("\n")));
        List<String> updatedLines = new ArrayList<>();
        int openBraces = 0;
        int closeBraces = 0;
        Stack<Integer> braceStack = new Stack<>();
        List<Integer> emptyLines = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (line.isEmpty()) {
                emptyLines.add(i); // Save empty lines
                updatedLines.add("");
                continue;
            }

            // Detect unmatched braces
            if (line.contains("{")) {
                openBraces++;
                braceStack.push(i);
            }
            if (line.contains("}")) closeBraces++;

            // Detect missing semicolon
            if (!line.endsWith(";") && !line.endsWith("{") && !line.endsWith("}") &&
                    !line.startsWith("if") && !line.startsWith("while") && !line.startsWith("return")) {

                output += "ERROR: Missing semicolon at the end of: " + line + ". It is replaced\n";
                line += ";";
            }

            if(line.startsWith("return") && !line.endsWith(";")){
                output += "ERROR: Missing semicolon at the end of: " + line + ". It is replaced\n";
                line += ";";
            }

            updatedLines.add(line);

            // Missing '{' check
            if ((line.startsWith("if") || line.startsWith("while")) && !line.contains("{")) {
                if (i + 1 < lines.size() && !lines.get(i + 1).trim().startsWith("{")) {
                    updatedLines.add("{");
                    output += "ERROR: Missing bracelet at the end of: " + line + ". It is replaced\n";
                    openBraces++;
                }
            }
        }

        // Missing '}' check
        while (openBraces > closeBraces) {

            boolean added = false;
            for (int i : emptyLines) {
                updatedLines.set(i, "}");
                closeBraces++;
                added = true;
                break;
            }

            if (!added && !braceStack.isEmpty()) {
                int insertIndex = braceStack.pop() + 1;
                updatedLines.add(insertIndex, "}");
                closeBraces++;
            }
            output += "ERROR: Missing closing brace `}`. It is replaced\n";
        }
        return String.join("\n", updatedLines);
    }

    //Check for nonconditional statements and analyze their tokens for syntax check
    public static List<Token> analyzeTokensForNonConditional(String statement){

        List<Token> tokens = new ArrayList<>();

        String[]words = statement.split("\\s+|;");
        for(int i=0; i<words.length; i++){
            if(Pattern.matches("^(int|char)", words[i]))
                tokens.add(new Token(TokenType.type, words[i]));

            else if(Pattern.matches("=", words[i])){
                String afterTheEqualSign = "";
                for(int j=i+1; j<words.length; j++){
                        afterTheEqualSign += words[j]+" ";
                    }
                    afterTheEqualSign = afterTheEqualSign.trim();
                    if(Pattern.matches("\\s*\\d+(\\s*[\\+\\-\\*/]\\s*\\d+)*\\s*", afterTheEqualSign)){
                        tokens.add(new Token(TokenType.expression, afterTheEqualSign));
                        return tokens;
                    }
                }
            else if(Pattern.matches("return", words[i]))
                tokens.add(new Token(TokenType.returnStatement, words[i]));

            else if(Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", words[i]))
                tokens.add(new Token(TokenType.identifier, words[i]));

            else if(Pattern.matches("([a-z]|\\d+|([a-z]|\\d+) [+*/-] ([a-z]|\\d+))$", words[i]))
                tokens.add(new Token(TokenType.expression, words[i]));
            }

        return tokens;
    }

    //Analyze syntax for conditional statements check inside parenthesis and curly braces
    public static void analyzeSyntaxForConditional(String statement){

        String[] parts = statement.split("\\{|\\}");

        int index1 = parts[0].indexOf("(");
        int index2 = parts[0].indexOf(")");

        String conditionString = parts[0].substring(index1+1, index2);
        conditionString = conditionString.trim();

        if(!Pattern.matches("([a-z]|\\d+|([a-z]|\\d+) [+*/-] ([a-z]|\\d+)) (<=|>=|==|!=|<|>) ([a-z]|\\d+|([a-z]|\\d+) [+*/-] ([a-z]|\\d+))",conditionString)){
            if(statement.contains("if")){
                output += "Statement " + statementIndex + ": ERROR ! for declaring if statement condition\n";
                return;
            }
            else{
                output += "Statement " + statementIndex + ": ERROR ! for declaring while statement condition\n";
                return;
            }

        }

        String []insideLoop = parts[1].trim().split(";");

        List<Token> tokensForInsideLoop;

        for(String s : insideLoop){
            tokensForInsideLoop = analyzeTokensForNonConditional(s);
            analyzeSyntaxForNonConditionStatements(tokensForInsideLoop);
        }

        // Change output String because it is added extra String while checking inside curly braces
        String[] lines = output.split("\n");
        List<String> outputLines = new ArrayList<>();

        for(int i=0; i<lines.length-1; i++){
            outputLines.add(lines[i].trim());
        }

        String temp = "";
        for(String s : outputLines){
            temp += s.trim() + "\n";
        }

        output = temp;

        if(outputLines.get(outputLines.size()-1).contains("ERROR") && outputLines.get(outputLines.size()-1).contains(Integer.toString(statementIndex)))
            return;

        if (statement.contains("if"))
            output += "Statement " + statementIndex + ": Valid If Statement\n";
        else
            output += "Statement " + statementIndex + ": Valid While Loop\n";


    }

    //Check whether they follow expected form or not
    public static void analyzeSyntaxForNonConditionStatements(List<Token> tokens){

            if(tokens.get(0).getType() == TokenType.type){
                if(tokens.size() != 2){
                    output += "Statement " + statementIndex + ": ERROR ! for Variable Declaration\n";
                    return;
                }
                if(tokens.get(1).getType() == TokenType.identifier || tokens.get(1).getType() == TokenType.expression){
                    if(tokens.get(1).getType() == TokenType.identifier && !checkErrorForIdentifiers(tokens.get(1).getValue()))
                        return;
                    output += "Statement " + statementIndex + ": Valid Variable Declaration\n";
                    return;
                }
                output += "Statement " + statementIndex + ": ERROR ! for Variable Declaration\n";
            }
            else if(tokens.get(0).getType() == TokenType.identifier){
                if(!checkErrorForIdentifiers(tokens.get(0).getValue()))
                    return;
                if(tokens.size() != 2){
                    output += "Statement " + statementIndex + ": ERROR ! for Assignment Statement\n";
                    return;
                }
                if((tokens.get(1).getType() == TokenType.identifier || tokens.get(1).getType()
                        == TokenType.expression)){
                    if(tokens.get(1).getType() == TokenType.identifier && !checkErrorForIdentifiers(tokens.get(1).getValue()))
                        return;
                    output += "Statement " + statementIndex + ": Valid Assignment Statement\n";
                    return;
                }
                output += "Statement " + statementIndex + ": ERROR ! for Assignment Statement\n";
            }
            else if(tokens.get(0).getType() == TokenType.returnStatement){
                if(tokens.size() != 2){
                    output += "Statement " + statementIndex + ": ERROR ! for Return Statement\n";
                    return;
                }
                else if(tokens.get(1).getType() == TokenType.identifier || tokens.get(1).getType() == TokenType.expression){
                    if(tokens.get(1).getType() == TokenType.identifier && !checkErrorForIdentifiers(tokens.get(1).getValue()))
                        return;
                    output += "Statement " + statementIndex + ": Valid Return Statement\n";
                    return;
                }
                output += "Statement " + statementIndex + ": ERROR ! for Return Statement\n";
            }

    }

    //Check for identifiers that are not single lowercase letters
    public static boolean checkErrorForIdentifiers(String identifier){
        if( !(identifier.length() == 1 && identifier.charAt(0) >= 'a' && identifier.charAt(0) <= 'z') ){
            output += "Statement " + statementIndex + ": ERROR ! for Invalid Identifier\n";
            return false;
        }
        return true;
    }

}
