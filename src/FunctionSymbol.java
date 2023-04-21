public class FunctionSymbol extends BaseScope implements Symbol {
    final Type type;
    public FunctionSymbol(String name, Scope enclosingScope, Type type) {
        super(name, enclosingScope);
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }
}
