package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    public boolean isArrayValue() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ArrayValue)) return false;
        ArrayValue aV = (ArrayValue) obj;
        return Arrays.equals(data, aV.data);
    }
}
