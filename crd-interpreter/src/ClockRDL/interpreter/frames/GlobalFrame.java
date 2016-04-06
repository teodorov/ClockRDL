package ClockRDL.interpreter.frames;

import ClockRDL.interpreter.Memory;
import ClockRDL.interpreter.StateValue;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.values.*;
import ClockRDL.model.declarations.PrimitiveFunctionDecl;
import ClockRDL.model.kernel.NamedDeclaration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ciprian on 22/10/15.
 */
public class GlobalFrame extends TemporaryFrame {
    public static Map<String, PrimitiveFunctionValue> primitives = new HashMap<String, PrimitiveFunctionValue>() {{
        put("assert", new PrimitiveFunctionValue("assert", v -> primAssert((List<Value>)v)));
        put("print", new PrimitiveFunctionValue("print", v->primPrint((List<Value>)v)));
        put("array", new PrimitiveFunctionValue("array", v->primArray( (List<Value>)v ) ));
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

    public static Value primArray(List<Value> value) {
        if (value.size() != 1) throw new RuntimeException("Function array expects one argument but was called with " + value.size() + " arguments");
        int size = ((IntegerValue)(value.get(0))).getData();
        ArrayValue array = new ArrayValue();
        array.data = new StateValue[size];

        for (int i=0; i<size; i++) {
            array.data[i] = NullValue.uniqueInstance;
        }

        return array;
    }

    public GlobalFrame() {
        super("Global", null);
    }


    @Override
    public Value lookup(NamedDeclaration decl, Memory memory) {
        if (decl instanceof PrimitiveFunctionDecl) {
            Value result = primitives.get(decl.getName());
            if (result != null) return result;

            throw new RuntimeException("Function " + decl.getName() + " is not a primitive");
        }
        return super.lookup(decl, memory);
    }
}
