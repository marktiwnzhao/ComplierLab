import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("The number of arguments is wrong!");
        }
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(sysYLexer);

        SysYParser sysYParser = new SysYParser(tokenStream);

        ParseTree tree = sysYParser.program();
        LLVMVisitor visitor = new LLVMVisitor(args[1]);
        visitor.visit(tree);
    }
}