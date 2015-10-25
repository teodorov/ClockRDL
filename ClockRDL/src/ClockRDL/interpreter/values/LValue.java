package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Frame;
import ClockRDL.interpreter.Value;

/**
 * Created by ciprian on 21/10/15.
 */
public abstract class LValue extends Value {

    public abstract void assign(Value value, Frame env);
}
