import org.antlr.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.util.List;


public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(sysYLexer);

        SysYParser sysYParser = new SysYParser(tokenStream);

        sysYParser.removeErrorListeners();
        ErrorListener errorListener = new ErrorListener();
        sysYParser.addErrorListener(errorListener);

        ParseTree tree = sysYParser.program();

        if (!errorListener.isError) {
            //没有词法错误
            String[] ruleNames = sysYLexer.getRuleNames();
            for (Token token : allTokens) {
                String ruleName = ruleNames[token.getType() - 1];
                if (ruleName.equals("INTEGER_CONST")) {
                    int num = parseInt(token.getText());
                    System.err.println(ruleName + " " + num + " at Line " + token.getLine() + ".");
                } else {
                    System.err.println(ruleName + " " + token.getText() + " at Line " + token.getLine() + ".");
                }
            }
        }
    }

    private static int parseInt(String text) {
        if (text.startsWith("0x") || text.startsWith("0X")) {
            return Integer.parseInt(text.substring(2), 16);
        } else if (text.startsWith("0") && text.length() > 1) {
            return Integer.parseInt(text.substring(1), 8);
        }
        return Integer.parseInt(text);
    }
}