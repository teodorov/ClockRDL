package ClockRDL.interpreter;

import ClockRDL.interpreter.values.PrimitiveFunctionValue;

import java.util.Map;

/**
 * Created by ciprian on 20/10/15.
 */
public abstract class Value {

    public Map<String, PrimitiveFunctionValue> primitives;

    public abstract boolean isAssignmentCompatible(Value value);

    public boolean isArrayValue() {
        return false;
    }
    public boolean isIntegerValue() {
        return false;
    }
    public boolean isBooleanValue() {
        return false;
    }
    public boolean isQueueValue() {
        return false;
    }
    public boolean isRecordValue() { return false; }
    public boolean isFunctionValue() { return false; }
    public boolean isNullValue() {return false;}
    public boolean isPrimitiveFunctionValue() { return false; }
    public boolean isFrame() {return false; }
}
