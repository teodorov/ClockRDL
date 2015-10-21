package ClockRDL.interpreter.values;

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
}
