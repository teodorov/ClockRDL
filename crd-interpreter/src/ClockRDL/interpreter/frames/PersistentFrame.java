package ClockRDL.interpreter.frames;

import ClockRDL.interpreter.Memory;
import ClockRDL.interpreter.Value;
import ClockRDL.model.declarations.ConstantDecl;
import ClockRDL.model.declarations.VariableDecl;
import ClockRDL.model.kernel.NamedDeclaration;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ciprian on 25/10/15.
 */
public abstract class PersistentFrame extends AbstractFrame {
    Map<NamedDeclaration, Integer> mapping = new IdentityHashMap<>();

    public PersistentFrame(String name, AbstractFrame env) {
        super(name, env);
    }

    public Value lookup(NamedDeclaration decl, Memory memory) {
        Integer address = mapping.get(decl); // look in this Frame
        if (address != null) {
            //the declaration is in this frame get its value from memory
            if (decl instanceof VariableDecl) {
                return memory.getVariable(address);
            }
            else {
                return memory.getConstant(address);
            }
        }
        if (enclosingEnvironment != null) { // have an enclosing environment?
            return enclosingEnvironment.lookup(decl, memory); // check enclosing scope
        }
        return null; // not found in this scope or there's no scope above
    }

    public void update(NamedDeclaration decl, Value value, Memory memory) {
        Integer address = mapping.get(decl);
        if (address != null) {
            if (!(decl instanceof VariableDecl)) {
                throw new RuntimeException("Trying to assign a new value to a constant");
            }
            memory.updateVariable(address, value);
            return;
        }
        //the mapping does not exist locally look it up in the enclosingEnvironment
        if (enclosingEnvironment != null) {
            this.enclosingEnvironment.update(decl, value, memory);
            return;
        }
        throw new RuntimeException("Could not find " + decl.getName() + " in the environment\n");
    }

    public void bind(NamedDeclaration decl, Value value, Memory memory) {
        if (mapping.get(decl) != null) {
            throw new RuntimeException("The declaration " + decl.getName() + " is already bound in this scope\n");
        }

        int address = (decl instanceof VariableDecl) ? memory.allocateVariable(value) : memory.allocateConstant(value);
        mapping.put(decl, address);
    }

    @Override
    public Set<NamedDeclaration> getMappingKeys() {
        return mapping.keySet();
    }
}
