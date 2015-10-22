package ClockRDL.interpreter;

import ClockRDL.interpreter.evaluators.*;
import ClockRDL.interpreter.values.FunctionValue;
import ClockRDL.interpreter.values.LValue;
import ClockRDL.interpreter.values.PrimitiveFunctionValue;
import ClockRDL.model.expressions.Literal;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.NamedDeclaration;
import ClockRDL.model.statements.BlockStmt;

import java.util.List;
import java.util.Set;

/**
 * Created by ciprian on 19/10/15.
 */
public class Interpreter {

    public Value evaluatePrimitive(PrimitiveFunctionValue opaqueValue, List<Value> argList) {
        if (argList.size() == 0) {
            return (Value)opaqueValue.fct.apply(new Object());
        }

        return (Value) opaqueValue.fct.apply(argList);
    }

    public Value applyClosure(FunctionValue closure, Frame env, List<Value> argList) {
        throw new RuntimeException("Feature mission");
    }

    public <T extends Value> T evaluate(Expression exp, Frame env, Class<T> type) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(this, env);
        Value value = evaluator.doSwitch(exp);
        if (type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        }
        throw new RuntimeException("Expected " + type.getSimpleName() + " but found " + value.getClass().getSimpleName());
        //return null;
    }

    public Value evaluate(Expression exp, Frame env) {
        return evaluate(exp, env, Value.class);
    }

    public <T extends Value> T evaluate(Literal exp, Frame env, Class<T> type) {
        LiteralEvaluator evaluator = new LiteralEvaluator(this, env);
        Value value = evaluator.doSwitch(exp);
        if (type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        }
        throw new RuntimeException("Expected " + type.getSimpleName() + " but found " + value.getClass().getSimpleName());
        //return null;
    }

    public Value evaluate(Literal exp, Frame env) {
        return evaluate(exp, env, Value.class);
    }

    public LValue lvalue(Expression exp, Frame env) {
        ExpressionLValueEvaluator evaluator = new ExpressionLValueEvaluator(this, env);
        Value value = evaluator.doSwitch(exp);
        if (value instanceof LValue) {
            return (LValue)value;
        }
        throw new RuntimeException("Expected an Lvalue but found " + value.getClass().getSimpleName());
    }

    public Value evaluate(NamedDeclaration decl, Frame env) {
        DeclarationEvaluator evaluator = new DeclarationEvaluator(this, env);

        return evaluator.doSwitch(decl);
    }

    public void evaluate(BlockStmt block, Frame env) {
        StatementEvaluator evaluator = new StatementEvaluator(this, env);

        evaluator.doSwitch(block);
    }

    //TODO hardcode a simple relation execution
    /*
    * relation
    * clock a b;
    * variable x:=1;
    * {
    * [x<10]{a b}[x +=1]
    * [x>=10] {a b} [ x := 1]
    * }
    * */
}
