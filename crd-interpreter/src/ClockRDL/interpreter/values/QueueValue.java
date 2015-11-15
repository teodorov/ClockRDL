package ClockRDL.interpreter.values;

import ClockRDL.interpreter.StateValue;
import ClockRDL.interpreter.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ciprian on 20/10/15.
 */
public class QueueValue extends StateValue {
    public LinkedList<StateValue> data;

    public QueueValue() {
        primitives = new HashMap<String, PrimitiveFunctionValue>() {{
            put("size", new PrimitiveFunctionValue("size", value ->
                    IntegerValue.value(data.size())

            ));

            put("add", new PrimitiveFunctionValue("add", (value) ->
                    BooleanValue.value(data.add(((List<StateValue>) value).get(0)))
            ));

            put("addFirst", new PrimitiveFunctionValue("addFirst", (value) -> {
                int before = data.size();
                data.addFirst(((List<StateValue>) value).get(0));
                return BooleanValue.value(data.size() == before + 1);
            }
            ));

            put("addLast", new PrimitiveFunctionValue("addLast", (value) -> {
                int before = data.size();
                data.addLast(((List<StateValue>) value).get(0));
                return BooleanValue.value(data.size() == before + 1);
            }
            ));

            put("remove", new PrimitiveFunctionValue("remove", (value) ->
                    data.remove()
            ));

            put("removeFirst", new PrimitiveFunctionValue("removeFirst", (value) ->
                    data.removeFirst()
            ));

            put("removeLast", new PrimitiveFunctionValue("removeLast", (value) ->
                    data.removeLast()
            ));

            put("first", new PrimitiveFunctionValue("first", (value) -> data.peekFirst()));

            put("last", new PrimitiveFunctionValue("last", (value) -> data.peekLast()));

            put("isEmpty", new PrimitiveFunctionValue("isEmpty", (value) -> BooleanValue.value(data.isEmpty())));

            put("isNotEmpty", new PrimitiveFunctionValue("isNotEmpty", (value) -> BooleanValue.value(!data.isEmpty())));

        }};
    }

    @Override
    public boolean isAssignmentCompatible(Value value) {
        if (!value.isQueueValue()) return false;
        //TODO do we care about changing the types of elements in an array by assignment?
        return true;
    }


    @Override
    public boolean isQueueValue() {
        return true;
    }

    @Override
    public StateValue deepCopy() {
        QueueValue newValue = new QueueValue();
        LinkedList<StateValue> newData = new LinkedList<>();
        for (int i = 0; i<data.size(); i++) {
            newData.add(i, data.get(i).deepCopy());
        }
        newValue.data = newData;
        return newValue;
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QueueValue)) return false;
        QueueValue aV = (QueueValue) obj;
        return data.equals(aV.data);
    }

    @Override
    public String toString() {
        String s = "{|";
        boolean space = false;
        for (Value v:data) {
            if (space) s += " ";
            s += v.toString();
            space = true;
        }
        return s+"|}";
    }
}
