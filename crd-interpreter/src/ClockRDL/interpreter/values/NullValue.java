package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;

/**
 * Created by ciprian on 20/10/15.
 */
public class NullValue extends Value {
    public final static NullValue uniqueInstance = new NullValue();
    private NullValue() {}

    @Override
    public boolean isAssignmentCompatible(Value value) {
        return true;
    }

    @Override
    public boolean isNulValue() {
        return true;
    }

    @Override
    public String toString() {
        return "null";
    }
}
