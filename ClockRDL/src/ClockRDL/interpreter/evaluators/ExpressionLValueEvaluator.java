package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Frame;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.values.*;
import ClockRDL.model.expressions.IndexedExp;
import ClockRDL.model.expressions.ReferenceExp;
import ClockRDL.model.expressions.SelectedExp;

/**
 * Created by ciprian on 21/10/15.
 */
public class ExpressionLValueEvaluator extends ExpressionEvaluator {
    public ExpressionLValueEvaluator(Interpreter interpreter, Frame env) {
        super(interpreter, env);
    }

    @Override
    public Value caseReferenceExp(ReferenceExp object) {
       return new LValueReference(object.getRef());
    }

    @Override
    public Value caseIndexedExp(IndexedExp object) {
        Value prefix = doSwitch(object.getPrefix());
        Value index = doSwitch(object.getIndex());

        if (!(prefix.isArrayValue() || prefix.isQueueValue())) {
            throw new RuntimeException("Expected array or queue value but found " + prefix.getClass().getSimpleName());
        }

        if (!index.isIntegerValue()) {
            throw new RuntimeException("Expected integer value but found " + index.getClass().getSimpleName());
        }

        if (prefix.isArrayValue()) {
            return new LValueArrayElement((ArrayValue)prefix, (IntegerValue) index);
        } else {
            return new LValueQueueElement((QueueValue)prefix, (IntegerValue) index);
        }
    }

    @Override
    public Value caseSelectedExp(SelectedExp object) {
        Value prefix = doSwitch(object.getPrefix());
        //TODO it is not necessarily a record, can be the ramp for type specific fonctions
        if (!prefix.isRecordValue()) {
            throw new RuntimeException("Expected record value but found " + prefix.getClass().getSimpleName());
        }

        Value selectedValue = ((RecordValue) prefix).data.get(object.getSelector());

        if (selectedValue == null) {
            throw new RuntimeException("The record does not have a field named " + object.getSelector());
        }

        return new LValueRecordField((RecordValue)prefix, object.getSelector());
    }
}
