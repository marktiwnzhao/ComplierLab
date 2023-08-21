public class BasicTypeSymbol extends BaseSymbol implements Type {
    public BasicTypeSymbol(String name) {
        super(name, null);
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isFunction() {
        return false;
    }

    @Override
    public boolean equals(Type other) {
        return (other instanceof BasicTypeSymbol) && name.equals(((BasicTypeSymbol)other).getName());
    }
}
