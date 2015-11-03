package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ciprian on 20/10/15.
 */
public class IntegerValue extends Value {
    public int data;

    public final static Map<Integer, IntegerValue> flyweight = new HashMap<>();

    public static IntegerValue value(int value) {
        IntegerValue iV = flyweight.get((Integer)value);
        if (iV != null) { return iV; }

        iV = new IntegerValue(value);
        flyweight.put(value, iV);
        return iV;
    }

    private IntegerValue(int value) {

        data = value;
    }

    @Override
    public boolean isAssignmentCompatible(Value value) {
        return value.isIntegerValue();
    }

    @Override
    public boolean isIntegerValue() {
        return true;
    }
    @Override
    public boolean equals(Object obj) {
        if (! ( obj instanceof IntegerValue ) ) return false;
        IntegerValue aV = (IntegerValue) obj;
        return data == aV.data;
    }

    @Override
    public String toString() {
        return Integer.toString(data);
    }
}
