package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;

/**
 * Created by ciprian on 20/10/15.
 */
public class NulValue extends Value {
    public final static NulValue uniqueInstance = new NulValue();
    private NulValue() {}
    @Override
    public boolean isNulValue() {
        return true;
    }
}
