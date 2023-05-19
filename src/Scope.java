import org.bytedeco.llvm.LLVM.LLVMValueRef;
import org.bytedeco.llvm.global.LLVM;

import java.util.Map;

public interface Scope {
    public String getName();

    public void setName(String name);

    public Scope getEnclosingScope();

    public Map<String, LLVMValueRef> getSymbols();

    public void define(String name, LLVMValueRef symbol);

    public LLVMValueRef resolve(String name);
}
