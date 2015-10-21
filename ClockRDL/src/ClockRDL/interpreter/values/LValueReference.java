package ClockRDL.interpreter.values;

import ClockRDL.model.kernel.NamedDeclaration;

/**
 * Created by ciprian on 21/10/15.
 */
public class LValueReference extends LValue {
    NamedDeclaration lvalue;

    public LValueReference(NamedDeclaration decl) {
        lvalue =decl;
    }
}
