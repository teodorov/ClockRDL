package ClockRDL.interpreter;

import ClockRDL.interpreter.evaluators.*;
import ClockRDL.interpreter.values.LValue;
import ClockRDL.model.declarations.RelationInstanceDecl;
import ClockRDL.model.expressions.Literal;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.NamedDeclaration;
import ClockRDL.model.kernel.Statement;

import java.util.Set;

/**
 * Created by ciprian on 19/10/15.
 */
public class Interpreter {

    public <T extends Value> T evaluate(Expression exp, Environment env, Class<T> type) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(this, env);
        Value value = evaluator.doSwitch(exp);
        if (type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        }
        throw new RuntimeException("Expected " + type.getSimpleName() + " but found " + value.getClass().getSimpleName());
    }

    public Value evaluate(Expression exp, Environment env) {
        return evaluate(exp, env, Value.class);
    }

    public <T extends Value> T evaluate(Literal exp, Environment env, Class<T> type) {
        LiteralEvaluator evaluator = new LiteralEvaluator(this, env);
        Value value = evaluator.doSwitch(exp);
        if (type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        }
        throw new RuntimeException("Expected " + type.getSimpleName() + " but found " + value.getClass().getSimpleName());
    }

    public Value evaluate(Literal exp, Environment env) {
        return evaluate(exp, env, Value.class);
    }

    public LValue lvalue(Expression exp, Environment env) {
        ExpressionLValueEvaluator evaluator = new ExpressionLValueEvaluator(this, env);
        Value value = evaluator.doSwitch(exp);
        if (value instanceof LValue) {
            return (LValue)value;
        }
        throw new RuntimeException("Expected an Lvalue but found " + value.getClass().getSimpleName());
    }

    public Value evaluate(NamedDeclaration decl, Environment env) {
        DeclarationEvaluator evaluator = new DeclarationEvaluator(this, env);

        return evaluator.doSwitch(decl);
    }

    public void evaluate(Statement statement, Environment env) {
        StatementEvaluator evaluator = new StatementEvaluator(this, env);

        evaluator.doSwitch(statement);
    }

    public void initialize(RelationInstanceDecl instance,  Environment env) {
        env.setMemory(new Memory());
        DeclarationEvaluator evaluator = new DeclarationEvaluator(this, env);
        env.bind(instance, evaluator.doSwitch(instance));
    }

    public Set<FireableTransition> fireableTransitions(RelationInstanceDecl instance,  Environment env) {
        TransitionCollector collector = new TransitionCollector();
        return collector.collectTransitions(instance, env, this);
    }

    public void evaluate(FireableTransition fireableTransition, Environment env) {
        Statement action = fireableTransition.transition.getAction();
        if (action != null) {
            env.push(fireableTransition.executionContext);
            this.evaluate(fireableTransition.transition.getAction(), env);
            env.pop();
        }
    }

    //TODO clarify the difference between the Scope computed during parsing and the execution Frame
    //Actually I think that during parsing we can build the SymbolTable
    //in the SymbolTable we could add references to the memory representation which is a composed value of:
    //<Clocks, Constants, Variables>
    //The Variables component of the memory represents the configuration needed for exploration

    //TODO the current implementation of functions should be implementable with static references to outerScope
    //TODO implement Function CALL
}
