package ClockRDL.interpreter.values;

import ClockRDL.interpreter.StateValue;
import ClockRDL.interpreter.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ciprian on 20/10/15.
 */
public class RecordValue extends StateValue {
    public Map<String, StateValue> data;

    public RecordValue() {

    }

    @Override
    public boolean isAssignmentCompatible(Value value) {
        if (!value.isRecordValue()) return false;
        RecordValue val = (RecordValue)value;
        if (data.size() != val.data.size()) return false;
        //for now we check only if the fields are the same and that their values are assignment compatible

        for (Map.Entry<String, StateValue> field: data.entrySet()) {
            Value otherFieldValue = val.data.get(field.getKey());
            if (otherFieldValue == null) return false;

            if (!field.getValue().isAssignmentCompatible(otherFieldValue)) return false;
        }

        return true;
    }


    @Override
    public boolean isRecordValue() {
        return true;
    }

    @Override
    public StateValue deepCopy() {
        RecordValue newValue = new RecordValue();
        Map<String, StateValue> newData = new HashMap<>();
        for (Map.Entry<String, StateValue> entry : data.entrySet()) {
            newData.put(entry.getKey(), entry.getValue().deepCopy());
        }
        newValue.data = newData;
        return newValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof RecordValue) ) return false;
        RecordValue rV = (RecordValue)obj;

        return data.equals(rV.data);
    }

    @Override
    public String toString() {
        String s = "{";
        boolean space = false;
        for (Map.Entry<String, StateValue> v : data.entrySet()) {
            if (space) s += " ";
            s += v.getKey() + " = " + v.getValue().toString();
            space = true;
        }
        return s+"}";
    }
}
