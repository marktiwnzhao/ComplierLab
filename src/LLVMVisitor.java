import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;
public class LLVMVisitor extends SysYParserBaseVisitor<LLVMValueRef> {
    //创建module
    LLVMModuleRef module = LLVMModuleCreateWithName("module");
    //初始化IRBuilder，后续将使用这个builder去生成LLVM IR
    LLVMBuilderRef builder = LLVMCreateBuilder();
    //考虑到我们的语言中仅存在int一个基本类型，可以通过下面的语句为LLVM的int型重命名方便以后使用
    LLVMTypeRef int32Type = LLVMInt32Type();
    String filePath;
    // 输出LLVM IR
    public static final BytePointer error = new BytePointer();
    public LLVMVisitor(String filePath) {
        //初始化LLVM
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();
        this.filePath = filePath;
    }
    @Override
    public LLVMValueRef visitProgram(SysYParser.ProgramContext ctx) {
        super.visitProgram(ctx);
        if(LLVMPrintModuleToFile(module, filePath, error) != 0) {
            LLVMDisposeMessage(error);
        }
        return null;
    }
    @Override
    public LLVMValueRef visitFuncDef(SysYParser.FuncDefContext ctx) {
        // 生成返回类型
        LLVMTypeRef retType = int32Type;
        // 生成函数参数类型
        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(0);
        //生成函数类型
        LLVMTypeRef ft = LLVMFunctionType(retType, argumentTypes, /* argumentCount */ 0, /* isVariadic */ 0);
        //生成函数，即向之前创建的module中添加函数
        LLVMValueRef function = LLVMAddFunction(module, /*functionName:String*/ctx.IDENT().getText(), ft);
        LLVMBasicBlockRef mainEntry = LLVMAppendBasicBlock(function, "mainEntry");
        //选择要在哪个基本块后追加指令
        LLVMPositionBuilderAtEnd(builder, mainEntry);//后续生成的指令将追加在mainEntry的后面

        return super.visitFuncDef(ctx);
    }
    @Override
    public LLVMValueRef visitReturnStmt(SysYParser.ReturnStmtContext ctx) {
        //生成返回值
        LLVMValueRef ret = LLVMBuildRet(builder, visit(ctx.exp()));
        return super.visitReturnStmt(ctx);
    }
    @Override
    public LLVMValueRef visitExpParenthesis(SysYParser.ExpParenthesisContext ctx) {
        return visit(ctx.exp());
    }
    @Override
    public LLVMValueRef visitNumberExp(SysYParser.NumberExpContext ctx) {
        return LLVMConstInt(int32Type, parseInt(ctx.number().getText()), 0);
    }
    @Override
    public LLVMValueRef visitUnaryOpExp(SysYParser.UnaryOpExpContext ctx) {
        LLVMValueRef exp = visit(ctx.exp());
        String op = ctx.unaryOp().getText();
        if(op.equals("+"))
            return exp;
        else if(op.equals("-"))
            return LLVMBuildNeg(builder, exp, "neg");
        else {
            // 生成icmp
            LLVMValueRef tmp_ = LLVMBuildICmp(builder, LLVMIntNE, LLVMConstInt(int32Type, 0, 0), exp, "tmp_");
            // 生成xor
            tmp_ = LLVMBuildXor(builder, tmp_, LLVMConstInt(LLVMInt1Type(), 1, 0), "tmp_");
            // 生成zext
            return LLVMBuildZExt(builder, tmp_, int32Type, "tmp_");
        }
    }
    @Override
    public LLVMValueRef visitMulExp(SysYParser.MulExpContext ctx) {
        LLVMValueRef left = visit(ctx.lhs);
        LLVMValueRef right = visit(ctx.rhs);
        switch (ctx.op.getType()) {
            case SysYParser.MUL:
                return LLVMBuildMul(builder, left, right, "mul");
            case SysYParser.DIV:
                return LLVMBuildSDiv(builder, left, right, "div");
            case SysYParser.MOD:
                return LLVMBuildSRem(builder, left, right, "mod");
            default:
                return null;
        }
    }
    @Override
    public LLVMValueRef visitPlusExp(SysYParser.PlusExpContext ctx) {
        LLVMValueRef left = visit(ctx.lhs);
        LLVMValueRef right = visit(ctx.rhs);
        switch (ctx.op.getType()) {
            case SysYParser.PLUS:
                return LLVMBuildAdd(builder, left, right, "add");
            case SysYParser.MINUS:
                return LLVMBuildSub(builder, left, right, "sub");
            default:
                return null;
        }
    }
    private static int parseInt(String text) {
        if(text.startsWith("0x") || text.startsWith("0X")) {
            return Integer.parseInt(text.substring(2), 16);
        } else if(text.startsWith("0") && text.length() > 1) {
            return Integer.parseInt(text.substring(1), 8);
        }
        return Integer.parseInt(text);
    }
}
