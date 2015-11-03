package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Environment;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.model.kernel.Declaration;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.Statement;
import ClockRDL.model.kernel.util.KernelSwitch;

/**
 * Created by ciprian on 22/10/15.
 */
public class KernelEvaluator extends KernelSwitch {
    Interpreter interpreter;
    Environment environment;
    public KernelEvaluator(Interpreter interpreter, Environment env) {
        this.interpreter = interpreter;
        this.environment = env;
    }
    @Override
    public Object caseDeclaration(Declaration object) {
        DeclarationEvaluator ev = new DeclarationEvaluator(interpreter, environment);
        return ev.doSwitch(object);
    }

    @Override
    public Object caseExpression(Expression object) {
        ExpressionEvaluator ev = new ExpressionEvaluator(interpreter, environment);
        return ev.doSwitch(object);
    }

    @Override
    public Object caseStatement(Statement object) {
        StatementEvaluator ev = new StatementEvaluator(interpreter, environment);
        return ev.doSwitch(object);
    }
}
