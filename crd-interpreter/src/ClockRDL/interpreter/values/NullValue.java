package ClockRDL.interpreter.values;

import ClockRDL.interpreter.StateValue;
import ClockRDL.interpreter.Value;

/**
 * Created by ciprian on 20/10/15.
 */
public class NullValue extends StateValue {
    public final static NullValue uniqueInstance = new NullValue();
    private NullValue() {}

    @Override
    public boolean isAssignmentCompatible(Value value) {
        return true;
    }

    @Override
    public boolean isNullValue() {
        return true;
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public StateValue deepCopy() {
        return uniqueInstance;
    }
}
