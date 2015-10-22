package ClockRDL.interpreter;

import ClockRDL.model.kernel.NamedDeclaration;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by ciprian on 20/10/15.
 */
public class Frame extends Value {
    String name;
    Frame enclosingEnvironment;
    Map<NamedDeclaration, Value> mapping = new IdentityHashMap<>();

    public Frame(String name, Frame env) {
        this.name = name;
        this.enclosingEnvironment = env;
    }

    public Frame(Frame env) {
        this(null, env);
    }

    public Frame(String name) {
        this(name, null);
    }

    public Frame getEnclosingEnvironment() {
        return enclosingEnvironment;
    }

    public void update(NamedDeclaration decl, Value value) {
        boolean isHere = mapping.get(decl) != null;
        if (isHere) {
            mapping.put(decl, value);
            return;
        }
        if (enclosingEnvironment != null) {
            this.enclosingEnvironment.update(decl, value);
            return;
        }
        throw new RuntimeException("Could not find " + decl.getName() + " in the environment\n");
    }

    public void bind(NamedDeclaration decl, Value value) {
        mapping.put(decl, value);
    }

    public void bind(String symbol, Value value) {
        //this changes the value of an existing key
        for (Map.Entry<NamedDeclaration, Value> entry : mapping.entrySet()) {
            if (entry.getKey().getName().equals(name)) {
                entry.setValue(value);
                return;
            }
        }
        System.err.println("Symbol " + symbol + " is not defined in " + name + " context\n");
    }

    public Value lookup(String name) {
        for (Map.Entry<NamedDeclaration, Value> entry : mapping.entrySet()) {
            if (entry.getKey().getName().equals(name)) {
                return entry.getValue();
            }
        }
        if (enclosingEnvironment != null) {
            return enclosingEnvironment.lookup(name);
        }
        return null;
    }

    public Value lookup(NamedDeclaration decl) {
        Value s = mapping.get(decl); // look in this Frame
        if (s != null) return s; // return it if in this Frame
        if (enclosingEnvironment != null) { // have an enclosing environment?
            return enclosingEnvironment.lookup(decl); // check enclosing scope
        }
        return null; // not found in this scope or there's no scope above
    }
}
