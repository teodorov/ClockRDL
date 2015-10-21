package ClockRDL.interpreter;

import ClockRDL.interpreter.values.FunctionValue;
import ClockRDL.interpreter.values.NulValue;
import ClockRDL.model.declarations.ClockDecl;
import ClockRDL.model.declarations.ConstantDecl;
import ClockRDL.model.declarations.FunctionDecl;
import ClockRDL.model.declarations.VariableDecl;
import ClockRDL.model.declarations.util.DeclarationsSwitch;
import ClockRDL.model.kernel.NamedDeclaration;

/**
 * Created by ciprian on 20/10/15.
 */
public class ValueInitializer {
    Frame currentEnvironment;

    public Value initialValue(NamedDeclaration decl, Frame env){
        currentEnvironment = env;
        return declarationsSwitch.doSwitch(decl);
    }

    Interpreter expressionEvaluator = new Interpreter();
    DeclarationsSwitch<Value> declarationsSwitch = new DeclarationsSwitch<Value>() {
        @Override
        public Value caseVariableDecl(VariableDecl object) {
            if (object.getInitial() == null) return NulValue.uniqueInstance;
            Value initial = expressionEvaluator.eval(object.getInitial(), currentEnvironment);
            return initial;
        }

        @Override
        public Value caseConstantDecl(ConstantDecl object) {
            if (object.getInitial() == null) return NulValue.uniqueInstance;
            Value initial = expressionEvaluator.eval(object.getInitial(), currentEnvironment);
            return initial;
        }

        @Override
        public Value caseFunctionDecl(FunctionDecl object) {
            FunctionValue initial = new FunctionValue();
            initial.data = object;
            initial.declarationEnvironment = currentEnvironment;
            return initial;
        }

        @Override
        public Value caseClockDecl(ClockDecl object) {
            return NulValue.uniqueInstance;
        }
    };
}
