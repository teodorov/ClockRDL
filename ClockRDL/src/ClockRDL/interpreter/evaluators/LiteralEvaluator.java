package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Frame;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.values.*;
import ClockRDL.model.expressions.literals.*;
import ClockRDL.model.expressions.literals.util.LiteralsSwitch;
import ClockRDL.model.kernel.Expression;

import java.time.Clock;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ciprian on 21/10/15.
 */
public class LiteralEvaluator extends LiteralsSwitch<Value> {
    Interpreter interpreter;
    Frame currentFrame;
    public LiteralEvaluator(Interpreter interpreter, Frame env) {
        this.interpreter = interpreter;
        currentFrame = env;
    }

    @Override
    public Value caseBooleanLiteral(BooleanLiteral object) {
        return BooleanValue.value(object.isValue());
    }

    @Override
    public Value caseIntegerLiteral(IntegerLiteral object) {
        return IntegerValue.value(object.getValue());
    }

    @Override
    public Value caseArrayLiteral(ArrayLiteral object) {
        ArrayValue aV = new ArrayValue();

        List<Expression> exps = object.getValue();
        aV.data = new Value[exps.size()];

        for (int i = 0; i < exps.size(); i++) {
            aV.data[i] = doSwitch(exps.get(i));
        }

        return aV;
    }

    @Override
    public Value caseQueueLiteral(QueueLiteral object) {
        QueueValue qV = new QueueValue();
        List<Expression> exps = object.getValue();
        qV.data = new LinkedList<>();

        for (Expression exp : exps) {
            Value v = doSwitch(exp);
            qV.data.add(v);
        }

        return qV;
    }

    @Override
    public Value caseRecordLiteral(RecordLiteral object) {
        RecordValue rV = new RecordValue();

        List<FieldLiteral> items = object.getValue();
        rV.data = new HashMap<>(items.size());

        for (FieldLiteral field : items) {
            Value v = doSwitch(field.getValue());
            rV.data.put(field.getName(), v);
        }

        return rV;
    }

    @Override
    public Value caseClockLiteral(ClockLiteral object) {
        return new ClockValue(object);
    }

    @Override
    public Value caseExpression(Expression object) {
        return interpreter.evaluate(object, currentFrame);
    }
}
