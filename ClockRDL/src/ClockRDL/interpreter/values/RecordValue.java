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
