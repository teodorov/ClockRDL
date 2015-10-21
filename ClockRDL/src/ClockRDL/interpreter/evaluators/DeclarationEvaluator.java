package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Frame;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.values.ClockValue;
import ClockRDL.interpreter.values.FunctionValue;
import ClockRDL.interpreter.values.NulValue;
import ClockRDL.model.declarations.*;
import ClockRDL.model.declarations.util.DeclarationsSwitch;
import ClockRDL.model.expressions.literals.BooleanLiteral;

/**
 * Created by ciprian on 21/10/15.
 */
public class DeclarationEvaluator extends DeclarationsSwitch<Boolean> {
    Interpreter interpreter;

    public DeclarationEvaluator(Interpreter interpreter) {
        this.interpreter = interpreter;
    }
    //TODO I think this one should create the environment
    @Override
    public Boolean caseClockDecl(ClockDecl object) {
        Value value = interpreter.evaluate(object.getInitial());
        interpreter.getEnvironment().bind(object, value);
        return true;
    }

    @Override
    public Boolean caseVariableDecl(VariableDecl object) {
        Value value = interpreter.evaluate(object.getInitial());
        interpreter.getEnvironment().bind(object, value);
        return true;
    }

    @Override
    public Boolean caseConstantDecl(ConstantDecl object) {
        Value value = interpreter.evaluate(object.getInitial());
        interpreter.getEnvironment().bind(object, value);
        return true;
    }

    @Override
    public Boolean caseArgumentDecl(ArgumentDecl object) {
        interpreter.getEnvironment().bind(object, NulValue.uniqueInstance);
        return true;
    }

    @Override
    public Boolean caseFunctionDecl(FunctionDecl object) {
        FunctionValue value = new FunctionValue();
        Frame declEnv = interpreter.getEnvironment();
        value.data = object;
        value.declarationEnvironment = declEnv;

        interpreter.getEnvironment().bind(object, value);

        return true;
    }

    @Override
    public Boolean caseParameterDecl(ParameterDecl object) {
        interpreter.getEnvironment().bind(object, NulValue.uniqueInstance);
        return true;
    }
}
