public class ArrayType implements Type {
    private Type subType;
    private int dimension;
    public ArrayType(Type subType, int dimension) {
        this.subType = subType;
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }

    public Type getSubType() {
        return subType;
    }

    @Override
    public boolean isArray() {
        return true;
    }
    @Override
    public boolean isFunction() {
        return false;
    }
    @Override
    public boolean equals(Type other) {
        return (other instanceof ArrayType) && dimension == ((ArrayType)other).getDimension();
    }
}
