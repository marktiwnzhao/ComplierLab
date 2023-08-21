import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Visitor extends SysYParserBaseVisitor<Void> {
    private final String[] parserRuleNames;
    private final String[] lexerRuleNames;
    private final String[] highLightTypes;
    public Visitor(String[] lexerRuleNames, String[] parserRuleNames) {
        this.lexerRuleNames = lexerRuleNames;
        this.parserRuleNames = parserRuleNames;
        this.highLightTypes = new String[lexerRuleNames.length];

        for(int i = 0; i < lexerRuleNames.length; i++) {
            if(i + 1 < 10){
                highLightTypes[i] = "[orange]";
            } else if(i + 1 < 25){
                highLightTypes[i] = "[blue]";
            } else if(i + 1 == 33){
                highLightTypes[i] = "[red]";
            } else if(i + 1 == 34){
                highLightTypes[i] = "[green]";
            } else {
                highLightTypes[i] = null;
            }
        }

    }

    @Override
    public Void visitChildren(RuleNode ruleNode) {
        RuleContext ruleContext = ruleNode.getRuleContext();
        int skip = ruleContext.depth();
        String ruleName = parserRuleNames[ruleContext.getRuleIndex()];
        Indent(skip);
        System.err.println(upperCase(ruleName));
        return super.visitChildren(ruleNode);
    }

    @Override
    public Void visitTerminal(TerminalNode terminalNode) {
        RuleContext ruleContext = (RuleContext) terminalNode.getParent();
        int skip = ruleContext.depth() + 1;
        int type = terminalNode.getSymbol().getType();// EOF的type值为 -1，要注意
        String ruleName;
        if((type > 0 && type < 25) || type == 33){
            Indent(skip);
            ruleName = lexerRuleNames[type - 1];
            System.err.println(terminalNode.getText() + " " + ruleName + highLightTypes[type - 1]);
        } else if(type == 34) {
            Indent(skip);
            ruleName = lexerRuleNames[type - 1];
            System.err.println(parseInt(terminalNode.getText()) + " " + ruleName + highLightTypes[type - 1]);
        }
        return super.visitTerminal(terminalNode);
    }

    private static int parseInt(String text) {
        if(text.startsWith("0x") || text.startsWith("0X")) {
            return Integer.parseInt(text.substring(2), 16);
        } else if(text.startsWith("0") && text.length() > 1) {
            return Integer.parseInt(text.substring(1), 8);
        }
        return Integer.parseInt(text);
    }

    private void Indent(int skip) {
        System.err.print(" ".repeat(2*(skip-1)));
    }

    private String upperCase(String ruleName) {
        return ruleName.substring(0, 1).toUpperCase() + ruleName.substring(1);
    }
}
