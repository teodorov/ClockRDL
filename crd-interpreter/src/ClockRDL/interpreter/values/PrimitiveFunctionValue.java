package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;

import java.util.function.Function;

/**
 * Created by ciprian on 21/10/15.
 */
public class PrimitiveFunctionValue extends Value {
    public String data;
    public Function fct;

    public PrimitiveFunctionValue(String name, Function x) {
        this.data = name;
        this.fct = x;
    }

    @Override
    public boolean isAssignmentCompatible(Value value) {
        return false;
    }

    @Override
    public boolean isPrimitiveFunctionValue() {
        return true;
    }
}
