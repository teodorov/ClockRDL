package ClockRDL.interpreter.values;

import ClockRDL.interpreter.StateValue;
import ClockRDL.interpreter.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by ciprian on 20/10/15.
 */
public class IntegerValue extends StateValue {
    private int data;

    public final static Map<Integer, IntegerValue> flyweight = new WeakHashMap<>();

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

    public int getData() {
        return data;
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
    public StateValue deepCopy() {
        return this;
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
