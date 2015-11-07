package ClockRDL.interpreter;

/**
 * Created by ciprian on 07/11/15.
 */
public abstract class StateValue extends Value implements Cloneable {
    public abstract StateValue deepCopy();
}
