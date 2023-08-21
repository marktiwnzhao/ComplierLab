import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaseScope implements Scope {
    private final Scope enclosingScope;
    private final Map<String, LLVMValueRef> symbols = new LinkedHashMap<>();
    private String name;

    public BaseScope(String name, Scope enclosingScope) {
        this.name = name;
        this.enclosingScope = enclosingScope;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Scope getEnclosingScope() {
        return this.enclosingScope;
    }

    public Map<String, LLVMValueRef> getSymbols() {
        return this.symbols;
    }

    @Override
    public void define(String name, LLVMValueRef symbol) {
        symbols.put(name, symbol);
    }

    @Override
    public LLVMValueRef resolve(String name) {
        LLVMValueRef symbol = symbols.get(name);
        if (symbol != null) {
            return symbol;
        }

        if (enclosingScope != null) {
            return enclosingScope.resolve(name);
        }

        return null;
    }
}
