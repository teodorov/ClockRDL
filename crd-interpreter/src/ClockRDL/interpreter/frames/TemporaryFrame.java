package ClockRDL.interpreter.frames;

import ClockRDL.interpreter.Memory;
import ClockRDL.interpreter.Value;
import ClockRDL.model.declarations.VariableDecl;
import ClockRDL.model.kernel.NamedDeclaration;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ciprian on 25/10/15.
 */
public class TemporaryFrame extends AbstractFrame {
    Map<NamedDeclaration, Value> mapping = new IdentityHashMap<>();

    public TemporaryFrame(String name, AbstractFrame env) {
        super(name, env);
    }

    @Override
    public Set<NamedDeclaration> getMappingKeys() {
        return mapping.keySet();
    }

    @Override
    public Value lookup(NamedDeclaration decl, Memory memory) {
        Value result = mapping.get(decl); // look in this Frame
        if (result != null) {
            //the declaration is in this frame get its value from memory
            return result;
        }
        if (enclosingEnvironment != null) { // have an enclosing environment?
            return enclosingEnvironment.lookup(decl, memory); // check enclosing scope
        }
        return null; // not found in this scope or there's no scope above
    }

    @Override
    public void update(NamedDeclaration decl, Value value, Memory memory) {
        if (mapping.get(decl) != null) {
            if (!(decl instanceof VariableDecl)) {
                throw new RuntimeException("Trying to assign a new value to a constant");
            }
            mapping.put(decl, value);
            return;
        }
        //the mapping does not exist locally look it up in the enclosingEnvironment
        if (enclosingEnvironment != null) {
            this.enclosingEnvironment.update(decl, value, memory);
            return;
        }
        throw new RuntimeException("Could not find " + decl.getName() + " in the environment\n");
    }

    @Override
    public void bind(NamedDeclaration decl, Value value, Memory memory) {
        if (mapping.get(decl) != null) {
            throw new RuntimeException("The declaration " + decl.getName() + " is already bound in this scope\n");
        }

        mapping.put(decl, value);
    }
}
