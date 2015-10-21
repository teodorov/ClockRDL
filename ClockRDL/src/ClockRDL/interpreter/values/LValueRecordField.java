package ClockRDL.interpreter.values;

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
}
