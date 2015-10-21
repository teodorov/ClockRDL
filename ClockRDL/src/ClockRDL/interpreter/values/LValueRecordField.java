package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Frame;
import ClockRDL.interpreter.Value;
import ClockRDL.model.kernel.NamedDeclaration;

/**
 * Created by ciprian on 21/10/15.
 */
public class LValueRecordField extends LValue {
    RecordValue prefix;
    String fieldName;

    public LValueRecordField(RecordValue prefix, String selector) {
        this.prefix = prefix;
        this.fieldName = selector;
    }

    @Override
    public void assign(Value value, Frame env) {
        if (prefix.data.get(fieldName) == null)
            throw new RuntimeException("The record does not contain a field named " + fieldName);
        prefix.data.put(fieldName, value);
    }
}
