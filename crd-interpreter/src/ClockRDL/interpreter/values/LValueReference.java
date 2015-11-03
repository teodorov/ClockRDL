package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Environment;
import ClockRDL.interpreter.Value;
import ClockRDL.model.kernel.NamedDeclaration;

/**
 * Created by ciprian on 21/10/15.
 */
public class LValueReference extends LValue {
    NamedDeclaration lvalue;

    public LValueReference(NamedDeclaration decl) {
        lvalue=decl;
    }

    public void assign(Value value, Environment env) {
        env.update(lvalue, value);
    }

    @Override
    public boolean isAssignmentCompatible(Value value) {
        return false;
    }
}
