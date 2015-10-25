package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by ciprian on 20/10/15.
 */
public class ArrayValue extends Value {
    public Value data[];

    public ArrayValue() {
        primitives = new HashMap<String, PrimitiveFunctionValue>() {{
            put("size", new PrimitiveFunctionValue("size", value -> IntegerValue.value(data.length)
            ));
        }};
    }

    @Override
    public boolean isAssignmentCompatible(Value value) {
        if (!value.isArrayValue()) return false;
        ArrayValue val = (ArrayValue)value;
        if (data.length != val.data.length) return false;
        //TODO do we care about changing the types of elements in an array by assignment?
        return true;
    }

    @Override
    public boolean isArrayValue() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ArrayValue)) return false;
        ArrayValue aV = (ArrayValue) obj;
        return Arrays.equals(data, aV.data);
    }

    @Override
    public String toString() {
        String s = "[";
        boolean space = false;
        for (Value v:data) {
            if (space) s += " ";
            s += v.toString();
            space = true;
        }
        return s+"]";
    }
}
