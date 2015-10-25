package ClockRDL.interpreter;

import ClockRDL.interpreter.values.BooleanValue;
import ClockRDL.interpreter.values.PrimitiveFunctionValue;
import ClockRDL.model.kernel.NamedDeclaration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ciprian on 22/10/15.
 */
public class GlobalFrame extends Frame {
    Map<String, PrimitiveFunctionValue> primitives = new HashMap<String, PrimitiveFunctionValue>() {{
        put("assert", new PrimitiveFunctionValue("assert", v -> primAssert((List<Value>)v)));
        put("print", new PrimitiveFunctionValue("print", v->primPrint((List<Value>)v)));
    }};

    public static Value primAssert(List<Value> value) {
        if (value.size() != 2) throw new RuntimeException("Function assert expects two arguments but was called with " + value.size() + " arguments");

        if (!(value.get(0).equals(value.get(1)))) {
            throw new RuntimeException("RDL Assertion Failed (expected: "+ value.get(0) + " actual: " + value.get(1) + ")");
        }

        return BooleanValue.TRUE;
    }

    public static Value primPrint(List<Value> value) {
        if (value.size() != 1) throw new RuntimeException("Function assert expects one arguments but was called with " + value.size() + " arguments");

        System.out.println(value.get(0));

        return BooleanValue.TRUE;
    }

    public GlobalFrame() {
        super("Global");
    }

    public Value lookup(String name) {
        Value result = primitives.get(name);
        if (result != null) return result;

        result = super.lookup(name);
        if (result != null) return result;

        throw new RuntimeException("Relation instace or Primitive function named "+ name +" not found");
    }

    public Value lookup(NamedDeclaration decl) {
        String name = decl.getName();

        return lookup(name);
    }
}
