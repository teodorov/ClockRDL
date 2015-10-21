package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ciprian on 20/10/15.
 */
public class QueueValue extends Value {
    public LinkedList<Value> data;

    public QueueValue() {
        primitives = new HashMap<String, PrimitiveFunctionValue>() {{
            put("size", new PrimitiveFunctionValue("size", value ->
                    IntegerValue.value(data.size())

            ));

            put("add", new PrimitiveFunctionValue("add", (value) ->
                    BooleanValue.value(data.add(((List<Value>) value).get(0)))
            ));

            put("addFirst", new PrimitiveFunctionValue("addFirst", (value) -> {
                int before = data.size();
                data.addFirst(((List<Value>) value).get(0));
                return BooleanValue.value(data.size() == before + 1);
            }
            ));

            put("addLast", new PrimitiveFunctionValue("addLast", (value) -> {
                int before = data.size();
                data.addLast(((List<Value>) value).get(0));
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

        }};
    }

    @Override
    public boolean isQueueValue() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QueueValue)) return false;
        QueueValue aV = (QueueValue) obj;
        return data.equals(aV.data);
    }
}
