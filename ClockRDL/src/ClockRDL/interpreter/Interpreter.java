package ClockRDL.interpreter;

import ClockRDL.interpreter.evaluators.*;
import ClockRDL.interpreter.values.FunctionValue;
import ClockRDL.interpreter.values.LValue;
import ClockRDL.interpreter.values.PrimitiveFunctionValue;
import ClockRDL.model.declarations.LibraryDecl;
import ClockRDL.model.declarations.PrimitiveRelationDecl;
import ClockRDL.model.declarations.RelationInstanceDecl;
import ClockRDL.model.declarations.TransitionDecl;
import ClockRDL.model.expressions.Literal;
import ClockRDL.model.kernel.Declaration;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.NamedDeclaration;
import ClockRDL.model.kernel.Statement;
import ClockRDL.model.statements.BlockStmt;

import java.util.*;

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

    public void evaluate(Statement statement, Frame env) {
        StatementEvaluator evaluator = new StatementEvaluator(this, env);

        evaluator.doSwitch(statement);
    }

    public void initialize(RelationInstanceDecl instance,  Frame env) {
        DeclarationEvaluator evaluator = new DeclarationEvaluator(this, env);
        env.bind(instance, evaluator.doSwitch(instance));
    }

    public Set<FireableTransition> fireableTransitions(RelationInstanceDecl instance,  Frame env) {
        TransitionCollector collector = new TransitionCollector();
        return collector.collectTransitions(instance, env, this);
    }

    public void evaluate(FireableTransition fireableTransition) {
        this.evaluate(fireableTransition.transition.getAction(), fireableTransition.executionContext);
    }

    //TODO clarify the difference between the Scope computed during parsing and the execution Frame
    //Actually I think that during parsing we can build the SymbolTable
    //in the SymbolTable we could add references to the memory representation which is a composed value of:
    //<Clocks, Constants, Variables>
    //The Variables component of the memory represents the configuration needed for exploration

    //TODO the current implementation of functions should be implementable with static references to outerScope
    //TODO implement Function CALL
}
