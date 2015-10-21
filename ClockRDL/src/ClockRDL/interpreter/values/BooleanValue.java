package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ciprian on 20/10/15.
 */
public class BooleanValue extends Value {
    public boolean data;

    public final static BooleanValue TRUE = new BooleanValue(true);
    public final static BooleanValue FALSE = new BooleanValue(false);

    public static BooleanValue value(boolean value) {
        return value ? TRUE : FALSE;
    }

    private BooleanValue(boolean value) {
        data = value;
    }

    @Override
    public boolean isBooleanValue() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (! ( obj instanceof BooleanValue ) ) return false;
        BooleanValue aV = (BooleanValue) obj;
        return data == aV.data;
    }
}
