package ClockRDL.interpreter.values;

import ClockRDL.model.kernel.NamedDeclaration;

/**
 * Created by ciprian on 21/10/15.
 */
public class LValueQueueElement extends LValue {
    QueueValue prefix;
    IntegerValue index;
    public LValueQueueElement(QueueValue prefix, IntegerValue index) {
        this.prefix = prefix;
        this.index = index;
    }
}