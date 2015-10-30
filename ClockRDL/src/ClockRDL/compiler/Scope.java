package ClockRDL.compiler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ciprian on 15/10/15.
 */
public class Scope<T> {
    String name;
    Scope<T> enclosingScope = null;
    Map<String, T> symbols = new HashMap<>();

    public Scope(String name, Scope<T> parent) {
        this.name = name;
        this.enclosingScope = parent;
    }

    public Scope(String name) {
        this.name = name;
    }

    public String getScopeName() {
        return name;
    }

    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    public void define(String sym, T value) {
        T previous = null;
        try {
            previous = resolve(sym);
        } catch (RuntimeException e) {
            symbols.put(sym, value);
            return;
        }
        throw new RuntimeException("Illegal redefinition of " + sym + "[" + previous.getClass().getSimpleName() + "] as " + value.getClass().getSimpleName());
    }

    public T resolve(String name) {
        T s = symbols.get(name); // look in this scope
        if (s != null) return s; // return it if in this scope
        if (enclosingScope != null) { // have an enclosing scope?
            return enclosingScope.resolve(name); // check enclosing scope
        }
        throw new RuntimeException("Symbol " + name + " is not present in scope");
        //return null; // not found in this scope or there's no scope above
    }
}
