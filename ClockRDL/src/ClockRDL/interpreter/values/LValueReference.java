package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Frame;
import ClockRDL.interpreter.Value;
import ClockRDL.model.kernel.NamedDeclaration;

/**
 * Created by ciprian on 21/10/15.
 */
public class LValueReference extends LValue {
    NamedDeclaration lvalue;

    public LValueReference(NamedDeclaration decl) {
        lvalue =decl;
    }

    public void assign(Value value, Frame env) {
        env.update(lvalue, value);
    }
}
