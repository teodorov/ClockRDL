package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Frame;
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
    Frame currentFrame;
    public KernelEvaluator(Interpreter interpreter, Frame env) {
        this.interpreter = interpreter;
        this.currentFrame = env;
    }
    @Override
    public Object caseDeclaration(Declaration object) {
        DeclarationEvaluator ev = new DeclarationEvaluator(interpreter, currentFrame);
        return ev.doSwitch(object);
    }

    @Override
    public Object caseExpression(Expression object) {
        ExpressionEvaluator ev = new ExpressionEvaluator(interpreter, currentFrame);
        return ev.doSwitch(object);
    }

    @Override
    public Object caseStatement(Statement object) {
        StatementEvaluator ev = new StatementEvaluator(interpreter, currentFrame);
        return ev.doSwitch(object);
    }
}
