import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.bytedeco.llvm.global.LLVM.*;
public class LLVMVisitor extends SysYParserBaseVisitor<LLVMValueRef> {
    //创建module
    LLVMModuleRef module = LLVMModuleCreateWithName("module");
    //初始化IRBuilder，后续将使用这个builder去生成LLVM IR
    LLVMBuilderRef builder = LLVMCreateBuilder();
    //考虑到我们的语言中仅存在int一个基本类型，可以通过下面的语句为LLVM的int型重命名方便以后使用
    LLVMTypeRef int32Type = LLVMInt32Type();
    LLVMTypeRef voidType = LLVMVoidType();
    LLVMValueRef zero = LLVMConstInt(int32Type, 0, 0);
    private GlobalScope globalScope = null;
    private Scope currentScope = null;
    private int localScopeCounter = 0;
    private boolean isReturn = false;
    String filePath;
    private final Map<LLVMValueRef, LLVMTypeRef> functions = new LinkedHashMap<>();
    private final Map<LLVMValueRef, LLVMTypeRef> arrayTypes = new LinkedHashMap<>();
    private final Deque<LLVMBasicBlockRef> breakBlocks = new ArrayDeque<>();
    private final Deque<LLVMBasicBlockRef> continueBlocks = new ArrayDeque<>();
    private LLVMValueRef currentFunction = null;
    private LLVMBasicBlockRef ifTrueBlock = null;
    private LLVMBasicBlockRef ifFalseBlock = null;
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
        globalScope = new GlobalScope(null);
        currentScope = globalScope;

        super.visitProgram(ctx);
        if(LLVMPrintModuleToFile(module, filePath, error) != 0) {
            LLVMDisposeMessage(error);
        }
        return null;
    }
    @Override
    public LLVMValueRef visitFuncDef(SysYParser.FuncDefContext ctx) {
        String funcName = ctx.IDENT().getText();
        String retTypeName = ctx.funcType().getText();
        int argCount = 0;
        if(ctx.funcFParams() != null) {
            argCount = ctx.funcFParams().funcFParam().size();
        }
        // 生成函数参数类型
        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(argCount);
        for(int i = 0; i < argCount; i++) {
            if(ctx.funcFParams().funcFParam(i).L_BRACKT() != null && ctx.funcFParams().funcFParam(i).L_BRACKT().size() > 0) {
                argumentTypes.put(i, LLVMPointerType(int32Type, 0));
            } else {
                argumentTypes.put(i, int32Type);
            }
        }
        // 生成返回类型
        LLVMTypeRef retType = getTypeRef(retTypeName);
        // 生成函数类型
        LLVMTypeRef ft = LLVMFunctionType(retType, argumentTypes, /* argumentCount */ argCount, /* isVariadic */ 0);
        // 生成函数，即向之前创建的module中添加函数
        LLVMValueRef function = LLVMAddFunction(module, /*functionName:String*/ctx.IDENT().getText(), ft);
        currentFunction = function;
        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(function, funcName+"Entry");
        // 选择要在哪个基本块后追加指令
        LLVMPositionBuilderAtEnd(builder, entry);
        // 将函数加入符号表
        currentScope.define(funcName, function);
        functions.put(function, ft);
        // 讲函数参数加入符号表
        LocalScope localScope = new LocalScope(currentScope);
        String localScopeName = localScope.getName() + localScopeCounter++;
        localScope.setName(localScopeName);
        currentScope = localScope;
        for(int i = 0; i < argCount; i++) {
            String argName = ctx.funcFParams().funcFParam(i).IDENT().getText();
            LLVMValueRef pointer;
            if(ctx.funcFParams().funcFParam(i).L_BRACKT() != null && ctx.funcFParams().funcFParam(i).L_BRACKT().size() > 0) {
                pointer = LLVMBuildAlloca(builder, LLVMPointerType(int32Type, 0), argName);
                arrayTypes.put(pointer, LLVMPointerType(int32Type, 0));
            } else {
                pointer = LLVMBuildAlloca(builder, int32Type, argName);
            }
            currentScope.define(argName, pointer);
            LLVMValueRef arg = LLVMGetParam(function, i);
            LLVMBuildStore(builder, arg, pointer);
        }
        // 为没有return语句的函数加上return语句
        isReturn = false;
        visit(ctx.block());
        if(!isReturn) {
            LLVMBuildRetVoid(builder);
        }
        currentScope = currentScope.getEnclosingScope();
        return null;
    }
    @Override
    public LLVMValueRef visitBlock(SysYParser.BlockContext ctx) {
        LocalScope localScope = new LocalScope(currentScope);
        String localScopeName = localScope.getName() + localScopeCounter++;
        localScope.setName(localScopeName);
        currentScope = localScope;
        LLVMValueRef res = super.visitBlock(ctx);
        currentScope = currentScope.getEnclosingScope();
        return res;
    }
    @Override
    public LLVMValueRef visitConstDecl(SysYParser.ConstDeclContext ctx) {
        for(SysYParser.ConstDefContext constDefContext : ctx.constDef()) {
            String constName = constDefContext.IDENT().getText();
            LLVMTypeRef constType = int32Type;
            int size = 0;
            if(constDefContext.constExp().size() > 0) {    // 数组
                size = (int) LLVMConstIntGetSExtValue(visit(constDefContext.constExp(0)));
                constType = LLVMVectorType(constType, size);
            }
            LLVMValueRef pointer;
            SysYParser.ConstInitValContext constInitValContext = constDefContext.constInitVal();
            if(currentScope instanceof GlobalScope) {   // 全局常量
                pointer = LLVMAddGlobal(module, constType, constName);
                if(size != 0) {
                    LLVMValueRef[] values = new LLVMValueRef[size];
                    for(int i = 0; i < size; i++) {
                        values[i] = visit(constInitValContext.constInitVal(i));
                    }
                    LLVMSetInitializer(pointer, LLVMConstVector(new PointerPointer(values), size));
                    arrayTypes.put(pointer, constType);
                } else {
                    LLVMSetInitializer(pointer, visit(constInitValContext));
                }
            } else {    // 局部常量
                pointer = LLVMBuildAlloca(builder, constType, constName);
                if(size != 0) {
                    LLVMValueRef[] values = new LLVMValueRef[size];
                    int initCount = constInitValContext.constInitVal().size();
                    for(int i = 0; i < size; i++) {
                        if(i < initCount) {
                            values[i] = visit(constInitValContext.constInitVal(i));
                        } else {
                            values[i] = zero;
                        }
                    }
                    initArray(pointer, constType, values);
                    arrayTypes.put(pointer, constType);
                } else {
                    LLVMBuildStore(builder, visit(constInitValContext), pointer);
                }
            }
            currentScope.define(constName, pointer);
        }
        return null;
    }
    @Override
    public LLVMValueRef visitVarDecl(SysYParser.VarDeclContext ctx) {
        for(SysYParser.VarDefContext varDefContext : ctx.varDef()) {
            String varName = varDefContext.IDENT().getText();
            LLVMTypeRef varType = int32Type;
            int size = 0;
            if(varDefContext.constExp().size() > 0) {
                size = (int) LLVMConstIntGetSExtValue(visit(varDefContext.constExp(0)));
                varType = LLVMVectorType(varType, size);
            }
            LLVMValueRef pointer;
            if(currentScope instanceof GlobalScope) {
                pointer = LLVMAddGlobal(module, varType, varName);
                if(varDefContext.ASSIGN() != null) {
                    SysYParser.InitValContext initValContext = varDefContext.initVal();
                    if(size != 0) {
                        LLVMValueRef[] values = new LLVMValueRef[size];
                        int initCount = initValContext.initVal().size();
                        for(int i = 0; i < size; i++) {
                            if(i < initCount) {
                                values[i] = visit(initValContext.initVal(i));
                            } else {
                                values[i] = zero;
                            }
                        }
                        LLVMSetInitializer(pointer, LLVMConstVector(new PointerPointer(values), size));
                        arrayTypes.put(pointer, varType);
                    } else {
                        LLVMSetInitializer(pointer, visit(initValContext));
                    }
                } else {
                    if(size != 0) {
                        LLVMValueRef[] values = new LLVMValueRef[size];
                        for(int i = 0; i < size; i++) {
                            values[i] = zero;
                        }
                        LLVMSetInitializer(pointer, LLVMConstVector(new PointerPointer(values), size));
                        arrayTypes.put(pointer, varType);
                    } else {
                        LLVMSetInitializer(pointer, zero);
                    }
                }
            } else {
                pointer = LLVMBuildAlloca(builder, varType, varName);
                if(varDefContext.ASSIGN() != null) {
                    SysYParser.InitValContext initValContext = varDefContext.initVal();
                    if(size != 0) {
                        LLVMValueRef[] values = new LLVMValueRef[size];
                        int initCount = initValContext.initVal().size();
                        for(int i = 0; i < size; i++) {
                            if(i < initCount) {
                                values[i] = visit(initValContext.initVal(i));
                            } else {
                                values[i] = zero;
                            }
                        }
                        initArray(pointer, varType, values);
                    } else {
                        LLVMBuildStore(builder, visit(initValContext), pointer);
                    }
                }
                if(size != 0) {
                    arrayTypes.put(pointer, varType);
                }
            }
            currentScope.define(varName, pointer);
        }
        return null;
    }
    @Override
    public LLVMValueRef visitAssignStmt(SysYParser.AssignStmtContext ctx) {
        LLVMValueRef lValPointer = visit(ctx.lVal());
        LLVMValueRef rVal = visit(ctx.exp());
        return LLVMBuildStore(builder, rVal, lValPointer);
    }
    @Override
    public LLVMValueRef visitReturnStmt(SysYParser.ReturnStmtContext ctx) {
        //生成返回值
        isReturn = true;
        if(ctx.exp() == null) {
            return LLVMBuildRetVoid(builder);
        }
        return LLVMBuildRet(builder, visit(ctx.exp()));
    }
    @Override
    public LLVMValueRef visitConditionStmt(SysYParser.ConditionStmtContext ctx) {
        LLVMBasicBlockRef ifTrue = LLVMAppendBasicBlock(currentFunction, "ifTrue");
        LLVMBasicBlockRef ifFalse = LLVMAppendBasicBlock(currentFunction, "ifFalse");
        LLVMBasicBlockRef next = LLVMAppendBasicBlock(currentFunction, "next");
        ifTrueBlock = ifTrue;
        ifFalseBlock = ifFalse;
        LLVMValueRef condition = LLVMBuildICmp(builder, LLVMIntNE, visit(ctx.cond()), zero, "icmp");

        LLVMBuildCondBr(builder, condition, ifTrue, ifFalse);
        // ifTrue
        LLVMPositionBuilderAtEnd(builder, ifTrue);
        visit(ctx.stmt(0));
        LLVMBuildBr(builder, next);
        // ifFalse
        LLVMPositionBuilderAtEnd(builder, ifFalse);
        if(ctx.ELSE() != null) {
            visit(ctx.stmt(1));
        }
        LLVMBuildBr(builder, next);
        // next
        LLVMPositionBuilderAtEnd(builder, next);

        return null;
    }
    @Override
    public LLVMValueRef visitWhileStmt(SysYParser.WhileStmtContext ctx) {
        LLVMBasicBlockRef whileCond = LLVMAppendBasicBlock(currentFunction, "whileCond");
        LLVMBasicBlockRef whileBody = LLVMAppendBasicBlock(currentFunction, "whileBody");
        LLVMBasicBlockRef whileEnd = LLVMAppendBasicBlock(currentFunction, "whileEnd");
        LLVMBuildBr(builder, whileCond);
        LLVMPositionBuilderAtEnd(builder, whileCond);
        ifTrueBlock = whileBody;
        ifFalseBlock = whileEnd;
        LLVMValueRef condition = LLVMBuildICmp(builder, LLVMIntNE, visit(ctx.cond()), zero, "icmp");
        LLVMBuildCondBr(builder, condition, whileBody, whileEnd);
        // ifTrue
        LLVMPositionBuilderAtEnd(builder, whileBody);
        breakBlocks.push(whileEnd);
        continueBlocks.push(whileCond);
        visit(ctx.stmt());
        breakBlocks.pop();
        continueBlocks.pop();
        LLVMBuildBr(builder, whileCond);
        // ifFalse
        LLVMPositionBuilderAtEnd(builder, whileEnd);
        return null;
    }
    @Override
    public LLVMValueRef visitBreakStmt(SysYParser.BreakStmtContext ctx) {
        LLVMBuildBr(builder, breakBlocks.peek());
        return null;
    }
    @Override
    public LLVMValueRef visitContinueStmt(SysYParser.ContinueStmtContext ctx) {
        LLVMBuildBr(builder, continueBlocks.peek());
        return null;
    }
    @Override
    public LLVMValueRef visitExpParenthesis(SysYParser.ExpParenthesisContext ctx) {
        return visit(ctx.exp());
    }
    @Override
    public LLVMValueRef visitLvalExp(SysYParser.LvalExpContext ctx) {
        LLVMValueRef lValPointer = visit(ctx.lVal());
        if(arrayTypes.get(lValPointer) != null) {
            return lValPointer;
        }
        if(arrayTypes.get(currentScope.resolve(ctx.lVal().IDENT().getText())) != null &&
           !arrayTypes.get(currentScope.resolve(ctx.lVal().IDENT().getText())).equals(LLVMPointerType(LLVMInt32Type(), 0))) {
            return lValPointer;
        }
        return LLVMBuildLoad2(builder, int32Type, lValPointer, ctx.lVal().getText());
    }
    @Override
    public LLVMValueRef visitNumberExp(SysYParser.NumberExpContext ctx) {
        return LLVMConstInt(int32Type, parseInt(ctx.number().getText()), 0);
    }
    @Override
    public LLVMValueRef visitCallFuncExp(SysYParser.CallFuncExpContext ctx) {
        String funcName = ctx.IDENT().getText();
        LLVMValueRef function = globalScope.resolve(funcName);
        int argCount = 0;
        if(ctx.funcRParams() != null) {
            argCount = ctx.funcRParams().param().size();
        }
        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(argCount);
        for(int i = 0; i < argCount; i++) {
            LLVMValueRef arg = visit(ctx.funcRParams().param(i));
            LLVMTypeRef argType = arrayTypes.get(arg);
            if(argType != null && !argType.equals(LLVMPointerType(int32Type, 0))) {
                argumentTypes.put(i, LLVMBuildGEP2(builder, argType, arg, new PointerPointer<>(zero, zero), 2, "res"));
            } else {
                argumentTypes.put(i, arg);
            }
        }
        LLVMTypeRef ft = functions.get(function);
        if(LLVMGetReturnType(ft).equals(voidType))
            return LLVMBuildCall2(builder, functions.get(function), function, argumentTypes, argCount, "");
        return LLVMBuildCall2(builder, functions.get(function), function, argumentTypes, argCount, "returnValue");
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
    @Override
    public LLVMValueRef visitLtCond(SysYParser.LtCondContext ctx) {
        LLVMValueRef left = visit(ctx.lhs);
        LLVMValueRef right = visit(ctx.rhs);
        LLVMValueRef res = null;
        switch (ctx.op.getType()) {
            case SysYParser.LT:
                res = LLVMBuildICmp(builder, LLVMIntSLT, left, right, "lt");
                break;
            case SysYParser.GT:
                res = LLVMBuildICmp(builder, LLVMIntSGT, left, right, "gt");
                break;
            case SysYParser.LE:
                res = LLVMBuildICmp(builder, LLVMIntSLE, left, right, "le");
                break;
            case SysYParser.GE:
                res = LLVMBuildICmp(builder, LLVMIntSGE, left, right, "ge");
                break;
            default:
                break;
        }
        return LLVMBuildZExt(builder, res, int32Type, "zext_res");
    }
    @Override
    public LLVMValueRef visitEqCond(SysYParser.EqCondContext ctx) {
        LLVMValueRef left = visit(ctx.lhs);
        LLVMValueRef right = visit(ctx.rhs);
        LLVMValueRef res = null;
        switch (ctx.op.getType()) {
            case SysYParser.EQ:
                res = LLVMBuildICmp(builder, LLVMIntEQ, left, right, "eq");
                break;
            case SysYParser.NEQ:
                res = LLVMBuildICmp(builder, LLVMIntNE, left, right, "neq");
                break;
            default:
                break;
        }
        return LLVMBuildZExt(builder, res, int32Type, "zext_res");
    }
    @Override
    public LLVMValueRef visitAndCond(SysYParser.AndCondContext ctx) {
        LLVMBasicBlockRef andTrue = LLVMAppendBasicBlock(currentFunction, "andTrue");
        LLVMValueRef condition = LLVMBuildICmp(builder, LLVMIntNE, visit(ctx.lhs), zero, "icmp");
        LLVMBuildCondBr(builder, condition, andTrue, ifFalseBlock);
        LLVMPositionBuilderAtEnd(builder, andTrue);
        return visit(ctx.rhs);
    }
    @Override
    public LLVMValueRef visitOrCond(SysYParser.OrCondContext ctx) {
        LLVMBasicBlockRef orFalse = LLVMAppendBasicBlock(currentFunction, "orFalse");
        LLVMValueRef condition = LLVMBuildICmp(builder, LLVMIntNE, visit(ctx.lhs), zero, "icmp");
        LLVMBuildCondBr(builder, condition, ifTrueBlock, orFalse);
        LLVMPositionBuilderAtEnd(builder, orFalse);
        return visit(ctx.rhs);
    }
    @Override
    public LLVMValueRef visitLVal(SysYParser.LValContext ctx) {
        String varName = ctx.IDENT().getText();
        LLVMValueRef pointer = currentScope.resolve(varName);
        if(ctx.exp().size() > 0) {
            PointerPointer<LLVMValueRef> index = new PointerPointer<>(zero, visit(ctx.exp(0)));
            if(arrayTypes.get(pointer).equals(LLVMPointerType(LLVMInt32Type(), 0))) {
                LLVMValueRef tmp = LLVMBuildLoad2(builder, LLVMPointerType(LLVMInt32Type(), 0), pointer, varName);
                return LLVMBuildGEP2(builder, int32Type, tmp, index, 2, "res");
            }
            return LLVMBuildGEP2(builder, arrayTypes.get(pointer), pointer, index, 2, "res");
        }
        return pointer;
    }
    private static int parseInt(String text) {
        if(text.startsWith("0x") || text.startsWith("0X")) {
            return Integer.parseInt(text.substring(2), 16);
        } else if(text.startsWith("0") && text.length() > 1) {
            return Integer.parseInt(text.substring(1), 8);
        }
        return Integer.parseInt(text);
    }
    private LLVMTypeRef getTypeRef(String type) {
        switch (type) {
            case "int":
                return int32Type;
            case "void":
                return voidType;
            default:
                return null;
        }
    }
    private void initArray(LLVMValueRef pointer, LLVMTypeRef ty, LLVMValueRef[] values) {
        for(int i = 0; i < values.length; i++) {
            PointerPointer<LLVMValueRef> index = new PointerPointer<>(zero, LLVMConstInt(int32Type, i, 0));
            LLVMValueRef elementPtr = LLVMBuildGEP2(builder, ty, pointer, index, 2, "pointer");
            LLVMBuildStore(builder, values[i], elementPtr);
        }
    }
}
