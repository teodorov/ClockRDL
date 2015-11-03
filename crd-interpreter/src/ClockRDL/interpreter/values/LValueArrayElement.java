package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Environment;
import ClockRDL.interpreter.Value;

/**
 * Created by ciprian on 21/10/15.
 */
public class LValueArrayElement extends LValue {
    ArrayValue prefix;
    IntegerValue index;

    public LValueArrayElement(ArrayValue prefix, IntegerValue index) {
        this.prefix = prefix;
        this.index = index;
    }

    @Override
    public void assign(Value value, Environment env) {
        prefix.data[index.data] = value;
    }

    @Override
    public boolean isAssignmentCompatible(Value value) {
        return false;
    }
}
