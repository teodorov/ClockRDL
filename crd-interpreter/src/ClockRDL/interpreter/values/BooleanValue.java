package ClockRDL.interpreter.values;

import ClockRDL.interpreter.StateValue;
import ClockRDL.interpreter.Value;
/**
 * Created by ciprian on 20/10/15.
 */
public class BooleanValue extends StateValue {
    private boolean data;

    public final static BooleanValue TRUE = new BooleanValue(true);
    public final static BooleanValue FALSE = new BooleanValue(false);

    public static BooleanValue value(boolean value) {
        return value ? TRUE : FALSE;
    }

    private BooleanValue(boolean value) {
        data = value;
    }

    public boolean getData() {
        return data;
    }

    @Override
    public boolean isAssignmentCompatible(Value value) {
        return value.isBooleanValue();
    }

    @Override
    public boolean isBooleanValue() {
        return true;
    }

    @Override
    public StateValue deepCopy() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (! ( obj instanceof BooleanValue ) ) return false;
        BooleanValue aV = (BooleanValue) obj;
        return data == aV.data;
    }

    @Override
    public String toString() {
        return Boolean.toString(data);
    }

}
