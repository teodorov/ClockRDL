package ClockRDL.interpreter;

import ClockRDL.interpreter.values.PrimitiveFunctionValue;

import java.util.Map;

/**
 * Created by ciprian on 20/10/15.
 */
public class Value {

    public Map<String, PrimitiveFunctionValue> primitives;

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
    public boolean isNulValue() {return false;}
    public boolean isPrimitiveFunctionValue() { return false; }
}
