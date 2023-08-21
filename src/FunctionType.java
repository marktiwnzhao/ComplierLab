import java.util.ArrayList;

public class FunctionType implements Type {
    private Type retType;
    ArrayList<Type> parameterTypes;

    public FunctionType(Type retType, ArrayList<Type> parameterTypes) {
        this.retType = retType;
        this.parameterTypes = parameterTypes;
    }

    public Type getRetType() {
        return retType;
    }

    public ArrayList<Type> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(ArrayList<Type> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    @Override
    public boolean isArray() {
        return false;
    }
    @Override
    public boolean isFunction() {
        return true;
    }
    @Override
    public boolean equals(Type other) {
        return false;
    }
}

