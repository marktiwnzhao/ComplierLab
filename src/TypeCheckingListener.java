import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class TypeCheckingListener extends SysYParserBaseListener {
    private GlobalScope globalScope = null;
    private Scope currentScope = null;
    private int localScopeCounter = 0;
    public boolean isError = false;
    private boolean isRedef = false;
    // 语法标注树，用于存储每个节点的类型
    private final ParseTreeProperty<Type> typeTree = new ParseTreeProperty<>();

    @Override
    public void enterProgram(SysYParser.ProgramContext ctx) {
        globalScope = new GlobalScope(null);
        currentScope = globalScope;
    }

    @Override
    public void enterFuncDef(SysYParser.FuncDefContext ctx) {
        String typeName = ctx.funcType().getText();

        String funcName = ctx.IDENT().getText();
        // 类型4：函数重复定义
        if (currentScope.resolve(funcName) != null) {
            System.err.println("Error type 4 at Line " + ctx.IDENT().getSymbol().getLine() + ": Redefined function '" + funcName + "'.");
            isError = true;
            isRedef = true;
        } else {
            // 参数类型在exitFuncFParams中处理
            Type retType = (Type) globalScope.resolve(typeName);
            FunctionType funcType = new FunctionType(retType, null);
            FunctionSymbol func = new FunctionSymbol(funcName, currentScope, funcType);
            currentScope.define(func);
            currentScope = func;
        }
    }

    @Override
    public void exitFuncDef(SysYParser.FuncDefContext ctx) {
        if(!isRedef) {
            currentScope = currentScope.getEnclosingScope();
        }
        isRedef = false;
    }

    @Override
    public void exitFuncFParams(SysYParser.FuncFParamsContext ctx) {
        if (isRedef) {
            return;
        }
        ArrayList<Type> parameterTypes = new ArrayList<>();
        for(int i = 0; i < ctx.funcFParam().size(); i++) {
            String typeName = ctx.funcFParam(i).bType().getText();
            String varName = ctx.funcFParam(i).IDENT().getText();
            // 在当前作用域查找
            Symbol symbol = currentScope.resolveCurrentScope(varName);
            // 类型3：变量重复声明
            if(symbol != null) {
                System.err.println("Error type 3 at Line " + ctx.funcFParam(i).IDENT().getSymbol().getLine() + ": Redefined variable '" + varName + "'.");
                isError = true;
            } else {
                List<TerminalNode> lBrackt = ctx.funcFParam(i).L_BRACKT();
                Type type = (Type) globalScope.resolve(typeName);
                if(lBrackt != null && lBrackt.size() == 1) {
                    type = new ArrayType(type, 1);// 一维数组（测试只会有一维数组）
                }
                parameterTypes.add(type);
                VariableSymbol varSymbol = new VariableSymbol(varName, type);
                currentScope.define(varSymbol);
            }
        }
        ((FunctionType)((FunctionSymbol) currentScope).getType()).setParameterTypes(parameterTypes);
    }

    @Override
    public void enterBlock(SysYParser.BlockContext ctx) {
        if (isRedef) {
            return;
        }
        LocalScope localScope = new LocalScope(currentScope);

        String localScopeName = localScope.getName() + localScopeCounter;
        localScope.setName(localScopeName);
        localScopeCounter++;
        currentScope = localScope;
    }

    @Override
    public void exitBlock(SysYParser.BlockContext ctx) {
        if (isRedef) {
            return;
        }
        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void exitConstDecl(SysYParser.ConstDeclContext ctx) {
        if (isRedef) {
            return;
        }
        String typeName = ctx.bType().getText();
        Type consType = (Type)globalScope.resolve(typeName);
        for(int i = 0; i < ctx.constDef().size(); i++) {
            String varName = ctx.constDef(i).IDENT().getText();
            Symbol symbol = currentScope.resolveCurrentScope(varName);
            if(currentScope.getEnclosingScope() instanceof FunctionSymbol && symbol == null) {
                symbol = currentScope.getEnclosingScope().resolveCurrentScope(varName);
            }
            // 类型3：变量重复声明
            if(symbol != null) {
                System.err.println("Error type 3 at Line " + ctx.constDef(i).IDENT().getSymbol().getLine() + ": Redefined variable '" + varName + "'.");
                isError = true;
            } else {
                List<SysYParser.ConstExpContext> constExpContexts = ctx.constDef(i).constExp();
                if(constExpContexts.isEmpty()) {
                    Type type = typeTree.get(ctx.constDef(i).constInitVal().constExp());
                    if(type != null) {
                        if(!type.equals(consType)) {
                            // 类型5：赋值号两侧类型不匹配
                            System.err.println("Error type 5 at Line " + ctx.constDef(i).IDENT().getSymbol().getLine() + ": Type mismatched for assignment.");
                            isError = true;
                        }
                    }
                } else {
                    consType = new ArrayType(consType, constExpContexts.size());
                }
                VariableSymbol varSymbol = new VariableSymbol(varName, consType);
                currentScope.define(varSymbol);
            }
        }
    }

    @Override
    public void exitVarDecl(SysYParser.VarDeclContext ctx) {
        if (isRedef) {
            return;
        }
        String typeName = ctx.bType().getText();
        Type varType = (Type) globalScope.resolve(typeName);
        for(int i = 0; i < ctx.varDef().size(); i++) {
            String varName = ctx.varDef(i).IDENT().getText();
            Symbol symbol = currentScope.resolveCurrentScope(varName);
            if(currentScope.getEnclosingScope() instanceof FunctionSymbol && symbol == null) {
                symbol = currentScope.getEnclosingScope().resolveCurrentScope(varName);
            }
            // 类型3：变量重复声明
            if(symbol != null) {
                System.err.println("Error type 3 at Line " + ctx.varDef(i).IDENT().getSymbol().getLine() + ": Redefined variable '" + varName + "'.");
                isError = true;
            } else {
                List<SysYParser.ConstExpContext> constExpContexts = ctx.varDef(i).constExp();
                if(constExpContexts.isEmpty()) {
                    if(ctx.varDef(i).initVal() != null) {
                        Type type = typeTree.get(ctx.varDef(i).initVal().exp());
                        if(type != null) {
                            if(!varType.equals(type)) {
                                // 类型5：赋值号两侧类型不匹配
                                System.err.println("Error type 5 at Line " + ctx.varDef(i).IDENT().getSymbol().getLine() + ": Type mismatched for assignment.");
                                isError = true;
                            }
                        }
                    }
                } else {
                    varType = new ArrayType(varType, constExpContexts.size());
                }
                VariableSymbol varSymbol = new VariableSymbol(varName, varType);
                currentScope.define(varSymbol);
            }
        }
    }

    @Override
    public void exitAssignStmt(SysYParser.AssignStmtContext ctx) {
        if (isRedef) {
            return;
        }
        Type lValType = typeTree.get(ctx.lVal());
        Type expType = typeTree.get(ctx.exp());
        if(lValType != null && expType != null) {
            if(lValType.isFunction()) {
                // 类型11：函数名不能作为左值
                System.err.println("Error type 11 at Line " + ctx.lVal().IDENT().getSymbol().getLine() + ": '" + ctx.lVal().IDENT().getText() + "' is not a variable.");
                isError = true;
            } else if(!lValType.equals(expType)) {
                // 类型5：赋值号两侧类型不匹配
                System.err.println("Error type 5 at Line " + ctx.lVal().IDENT().getSymbol().getLine() + ": Type mismatched for assignment.");
                isError = true;
            }
        }
    }
    @Override
    public void exitReturnStmt(SysYParser.ReturnStmtContext ctx) {
        if (isRedef) {
            return;
        }
        Scope funcScope = currentScope;
        while(!(funcScope instanceof FunctionSymbol)) {
            funcScope = funcScope.getEnclosingScope();
        }
        Type retType = ((FunctionType)((FunctionSymbol) funcScope).getType()).getRetType();
        if(retType.equals(new BasicTypeSymbol("void"))) {
            if(ctx.exp() != null) {
                Type type = typeTree.get(ctx.exp());
                if(type != null && !type.equals(new BasicTypeSymbol("void"))) {
                    // 类型7：返回值类型不匹配
                    System.err.println("Error type 7 at Line " + ctx.RETURN().getSymbol().getLine() + ": Type mismatched for return.");
                    isError = true;
                }
            }
        } else {
            if(ctx.exp() == null) {
                // 类型7：返回值类型不匹配
                System.err.println("Error type 7 at Line " + ctx.RETURN().getSymbol().getLine() + ": Type mismatched for return.");
                isError = true;
            } else {
                Type type = typeTree.get(ctx.exp());
                if(type != null && !type.equals(retType)) {
                    // 类型7：返回值类型不匹配
                    System.err.println("Error type 7 at Line " + ctx.RETURN().getSymbol().getLine() + ": Type mismatched for return.");
                    isError = true;
                }
            }
        }
    }
    @Override
    public void exitExpParenthesis(SysYParser.ExpParenthesisContext ctx) {
        if(isRedef) {
            return;
        }
        typeTree.put(ctx, typeTree.get(ctx.exp()));
    }
    @Override
    public void exitLvalExp(SysYParser.LvalExpContext ctx) {
        if(isRedef) {
            return;
        }
        typeTree.put(ctx, typeTree.get(ctx.lVal()));
    }
    @Override
    public void exitNumberExp(SysYParser.NumberExpContext ctx) {
        if(isRedef) {
            return;
        }
        typeTree.put(ctx, (Type)globalScope.resolve("int"));
    }
    @Override
    public void exitCallFuncExp(SysYParser.CallFuncExpContext ctx) {
        if(isRedef) {
            return;
        }
        String funcName = ctx.IDENT().getText();
        Symbol symbol = currentScope.resolve(funcName);
        if(symbol == null) {
            // 类型2：函数未定义
            System.err.println("Error type 2 at Line " + ctx.IDENT().getSymbol().getLine() + ": Undefined function '" + funcName + "'.");
            isError = true;
        } else {
            if(!symbol.getType().isFunction()) {
                // 类型10：对变量使用函数调用
                System.err.println("Error type 10 at Line " + ctx.IDENT().getSymbol().getLine() + ": '" + funcName + "' is not a function.");
                isError = true;
            } else {
                typeTree.put(ctx, ((FunctionType)symbol.getType()).getRetType());
                // 参数匹配
                ArrayList<Type> parameterTypes = ((FunctionType)symbol.getType()).getParameterTypes();
                if(parameterTypes == null) {
                    parameterTypes = new ArrayList<>();
                }
                ArrayList<Type> realParameterTypes = new ArrayList<>();
                if(ctx.funcRParams() != null) {
                    for(int i = 0; i < ctx.funcRParams().param().size(); i++) {
                        realParameterTypes.add(typeTree.get(ctx.funcRParams().param(i).exp()));
                    }
                }
                if(realParameterTypes.size() != parameterTypes.size()) {
                    // 类型8：参数不匹配
                    System.err.println("Error type 8 at Line " + ctx.IDENT().getSymbol().getLine() + ": Function '" + funcName + "' is not applicable for arguments.");
                    isError = true;
                } else {
                    for(int i = 0; i < realParameterTypes.size(); i++) {
                        if(!parameterTypes.get(i).equals(realParameterTypes.get(i))) {
                            // 类型8：参数不匹配
                            System.err.println("Error type 8 at Line " + ctx.IDENT().getSymbol().getLine() + ": Function '" + funcName + "' is not applicable for arguments.");
                            isError = true;
                            break;
                        }
                    }
                }
            }
        }
    }
    @Override
    public void exitUnaryOpExp(SysYParser.UnaryOpExpContext ctx) {
        if(isRedef) {
            return;
        }
        Type type = typeTree.get(ctx.exp());
        if(type != null) {
            if(type.isArray() || type.isFunction()) {
                // 类型6：运算符需求类型与提供类型不匹配
                System.err.println("Error type 6 at Line " + ctx.exp().getStart().getLine() + ": The operand of unary operator '" + ctx.unaryOp().getText() + "' should be an integer.");
                isError = true;
            } else {
                typeTree.put(ctx, type);
            }
        }
    }
    @Override
    public void exitMulExp(SysYParser.MulExpContext ctx) {
        if(isRedef) {
            return;
        }
        Type type1 = typeTree.get(ctx.exp(0));
        Type type2 = typeTree.get(ctx.exp(1));
        if(type1 != null && type2 != null) {
            if(type1.isArray() || type1.isFunction() || type2.isArray() || type2.isFunction()) {
                // 类型6：运算符需求类型与提供类型不匹配
                System.err.println("Error type 6 at Line " + ctx.getStart().getLine() + ": The operands of binary operator should be integers.");
                isError = true;
            } else {
                typeTree.put(ctx, (Type)globalScope.resolve("int"));
            }
        }
    }
    @Override
    public void exitPlusExp(SysYParser.PlusExpContext ctx) {
        if(isRedef) {
            return;
        }
        Type type1 = typeTree.get(ctx.exp(0));
        Type type2 = typeTree.get(ctx.exp(1));
        if(type1 != null && type2 != null) {
            if(type1.isArray() || type1.isFunction() || type2.isArray() || type2.isFunction()) {
                // 类型6：运算符需求类型与提供类型不匹配
                System.err.println("Error type 6 at Line " + ctx.getStart().getLine() + ": The operands of binary operator should be integers.");
                isError = true;
            } else {
                typeTree.put(ctx, (Type)globalScope.resolve("int"));
            }
        }
    }
    @Override
    public void exitExpCond(SysYParser.ExpCondContext ctx) {
        if(isRedef) {
            return;
        }
        typeTree.put(ctx, typeTree.get(ctx.exp()));
    }
    @Override
    public void exitLtCond(SysYParser.LtCondContext ctx) {
        if(isRedef) {
            return;
        }
        Type type1 = typeTree.get(ctx.cond(0));
        Type type2 = typeTree.get(ctx.cond(1));
        if(type1 != null && type2 != null) {
            if(type1.isArray() || type1.isFunction() || type2.isArray() || type2.isFunction()) {
                // 类型6：运算符需求类型与提供类型不匹配
                System.err.println("Error type 6 at Line " + ctx.getStart().getLine() + ": The operands of binary operator should be integers.");
                isError = true;
            } else {
                typeTree.put(ctx, type1);
            }
        }
    }
    @Override
    public void exitEqCond(SysYParser.EqCondContext ctx) {
        if(isRedef) {
            return;
        }
        Type type1 = typeTree.get(ctx.cond(0));
        Type type2 = typeTree.get(ctx.cond(1));
        if(type1 != null && type2 != null) {
            if(type1.isArray() || type1.isFunction() || type2.isArray() || type2.isFunction()) {
                // 类型6：运算符需求类型与提供类型不匹配
                System.err.println("Error type 6 at Line " + ctx.getStart().getLine() + ": The operands of binary operator should be integers.");
                isError = true;
            } else {
                typeTree.put(ctx, type1);
            }
        }
    }
    @Override
    public void exitAndCond(SysYParser.AndCondContext ctx) {
        if(isRedef) {
            return;
        }
        Type type1 = typeTree.get(ctx.cond(0));
        Type type2 = typeTree.get(ctx.cond(1));
        if(type1 != null && type2 != null) {
            if(type1.isArray() || type1.isFunction() || type2.isArray() || type2.isFunction()) {
                // 类型6：运算符需求类型与提供类型不匹配
                System.err.println("Error type 6 at Line " + ctx.getStart().getLine() + ": The operands of binary operator should be integers.");
                isError = true;
            } else {
                typeTree.put(ctx, type1);
            }
        }
    }
    @Override
    public void exitOrCond(SysYParser.OrCondContext ctx) {
        if(isRedef) {
            return;
        }
        Type type1 = typeTree.get(ctx.cond(0));
        Type type2 = typeTree.get(ctx.cond(1));
        if(type1 != null && type2 != null) {
            if(type1.isArray() || type1.isFunction() || type2.isArray() || type2.isFunction()) {
                // 类型6：运算符需求类型与提供类型不匹配
                System.err.println("Error type 6 at Line " + ctx.getStart().getLine() + ": The operands of binary operator should be integers.");
                isError = true;
            } else {
                typeTree.put(ctx, type1);
            }
        }
    }
    @Override
    public void exitLVal(SysYParser.LValContext ctx) {
        if (isRedef) {
            return;
        }
        String varName = ctx.IDENT().getText();
        Symbol symbol = currentScope.resolve(varName);
        List<SysYParser.ExpContext> expContexts = ctx.exp();
        if(symbol == null) {
            // 类型1：变量未声明
            System.err.println("Error type 1 at Line " + ctx.IDENT().getSymbol().getLine() + ": Undefined variable '" + varName + "'.");
            isError = true;
        } else {
            Type type = symbol.getType();
            if(!type.isArray() && !type.isFunction()) {
                // 变量
                if(!expContexts.isEmpty()) {
                    // 类型9：对非数组变量使用下标
                    System.err.println("Error type 9 at Line " + ctx.IDENT().getSymbol().getLine() + ": '" + varName + "' is not an array.");
                    isError = true;
                } else {
                    typeTree.put(ctx, type);
                }
            } else if(type.isArray()) {
                // 数组
                if(!expContexts.isEmpty()) {
                    if(expContexts.size() > ((ArrayType) type).getDimension()) {
                        // 类型9：对非数组变量使用下标
                        System.err.println("Error type 9 at Line " + ctx.IDENT().getSymbol().getLine() + ": '" + varName + "' is out of range.");
                        isError = true;
                    } else if(expContexts.size() == ((ArrayType) type).getDimension()) {
                        type = (Type)globalScope.resolve("int");
                        typeTree.put(ctx, type);
                    } else {
                        type = new ArrayType(((ArrayType) type).getSubType(), ((ArrayType) type).getDimension() - expContexts.size());
                        typeTree.put(ctx, type);
                    }
                } else {
                    typeTree.put(ctx, type);
                }
            } else {
                // 函数
                if(!expContexts.isEmpty()) {
                    // 类型9：对非数组变量使用下标
                    System.err.println("Error type 9 at Line " + ctx.IDENT().getSymbol().getLine() + ": '" + varName + "' is not a variable.");
                    isError = true;
                } else {
                    typeTree.put(ctx, type);
                }
            }
        }
    }
    @Override
    public void exitConstExp(SysYParser.ConstExpContext ctx) {
        if(isRedef) {
            return;
        }
        typeTree.put(ctx, typeTree.get(ctx.exp()));
    }
}

