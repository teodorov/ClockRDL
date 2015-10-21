package ClockRDL.interpreter;

import ClockRDL.interpreter.evaluators.LiteralEvaluator;
import ClockRDL.interpreter.evaluators.ExpressionEvaluator;
import ClockRDL.interpreter.values.FunctionValue;
import ClockRDL.interpreter.values.PrimitiveFunctionValue;
import ClockRDL.model.expressions.Literal;
import ClockRDL.model.kernel.Expression;

import java.util.List;
import java.util.Set;

/**
 * Created by ciprian on 19/10/15.
 */
public class Interpreter {

    Frame environment;

    /**
     * Building the initial configurations for the model associated with this language runtime.
     * Typically there is only one initial configuration. But generally there can be more than one (e.g. TLA+)
     * */
    public Set<Configuration> initialConfigurations() {
        return null;
    }
    /**
     * Computes the next state relations from a given configuration
     * */
    public Set<Configuration> nextStatesFrom(Configuration configuration) {
        return null;
    }
    /**
     * Evaluates the truth value of an atomic predicate expressed in the language this runtime implements
     * */
    public boolean evaluateAtomicPredicate(Predicate predicate, Configuration configuration) {
        return false;
    }

    public Value evaluate(FunctionValue closure, List<Value> args) {
        //TODO
        return null;
    }

    public Value evaluate(Expression exp, Frame env) {
        //TODO
        return null;
    }

    /**
     * Mostly for diagnostics, asks the language runtime for what happened during the source->target transition
     * It returns a list of ITransitions, which can represent the events that happened, the code that was executed, etc.
     * */
    public List<Object> transitions(Configuration source, Configuration target) {
        return null;
    }

    public Value evaluatePrimitive(PrimitiveFunctionValue opaqueValue, List<Value> argList) {
        if (argList.size() == 0) {
            return (Value)opaqueValue.fct.apply(new Object());
        }

        return (Value) opaqueValue.fct.apply(argList);
    }

    public Frame getEnvironment() {
        return environment;
    }

    public Value eval(Expression exp, Frame env) {
        //TODO I should account for the environment here
        return evaluate(exp);
    }

    public Value evaluate(Expression exp) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(this);
        return evaluator.doSwitch(exp);
    }
    public Value evaluate(Literal exp) {
        LiteralEvaluator evaluator = new LiteralEvaluator(this);
        return evaluator.doSwitch(exp);
    }
}
