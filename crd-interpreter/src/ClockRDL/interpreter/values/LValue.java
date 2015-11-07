package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Environment;
import ClockRDL.interpreter.StateValue;
import ClockRDL.interpreter.Value;

/**
 * Created by ciprian on 21/10/15.
 */
public abstract class LValue extends Value {

    public abstract void assign(StateValue value, Environment env);
}
