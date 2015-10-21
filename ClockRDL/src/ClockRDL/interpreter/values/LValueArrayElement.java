package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Frame;
import ClockRDL.interpreter.Value;
import ClockRDL.model.kernel.NamedDeclaration;

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
    public void assign(Value value, Frame env) {
        prefix.data[index.data] = value;
    }
}
