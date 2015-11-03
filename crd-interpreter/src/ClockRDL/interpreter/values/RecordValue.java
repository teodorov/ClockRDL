package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;

import java.util.Map;

/**
 * Created by ciprian on 20/10/15.
 */
public class RecordValue extends Value {
    public Map<String, Value> data;

    public RecordValue() {

    }

    @Override
    public boolean isAssignmentCompatible(Value value) {
        if (!value.isRecordValue()) return false;
        RecordValue val = (RecordValue)value;
        if (data.size() != val.data.size()) return false;
        //for now we check only if the fields are the same and that their values are assignment compatible

        for (Map.Entry<String, Value> field: data.entrySet()) {
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
    public boolean equals(Object obj) {
        if (! (obj instanceof RecordValue) ) return false;
        RecordValue rV = (RecordValue)obj;

        return data.equals(rV.data);
    }

    @Override
    public String toString() {
        String s = "{";
        boolean space = false;
        for (Map.Entry<String, Value> v : data.entrySet()) {
            if (space) s += " ";
            s += v.getKey() + " = " + v.getValue().toString();
            space = true;
        }
        return s+"}";
    }
}
