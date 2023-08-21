import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

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
            Visitor visitor = new Visitor(sysYLexer.getRuleNames(), sysYParser.getRuleNames());
            visitor.visit(tree);
        }
    }
}