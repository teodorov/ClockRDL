package ClockRDL.interpreter.frames;

import ClockRDL.interpreter.Memory;
import ClockRDL.interpreter.Value;
import ClockRDL.model.kernel.NamedDeclaration;

import java.util.Set;

/**
 * Created by ciprian on 25/10/15.
 */
public abstract class AbstractFrame extends Value {
    String name;
    AbstractFrame enclosingEnvironment;

    public AbstractFrame(String name, AbstractFrame env) {
        this.name = name;
        this.enclosingEnvironment = env;
    }

    @Override
    public boolean isAssignmentCompatible(Value value) {
        return false;
    }

    public AbstractFrame getEnclosingEnvironment() {
        return enclosingEnvironment;
    }

    public abstract Set<NamedDeclaration> getMappingKeys();

    public abstract Value lookup(NamedDeclaration decl, Memory memory);

    public Value lookup(String name, Memory memory) {
        //this is slow it does the lookup twice
        NamedDeclaration decl = find(name);
        return lookup(decl, memory);
    }

    public abstract void update(NamedDeclaration decl, Value value, Memory memory);

    public void update(String declName, Value value, Memory memory) {
        //this is slow it does the lookup twice
        NamedDeclaration decl = find(declName);
        update(decl, value, memory);
    }

    public NamedDeclaration find(String name) {
        for (NamedDeclaration key : getMappingKeys()) {
            if (key.getName().equals(name)){
                return key;
            }
        }
        //not found locally
        if (enclosingEnvironment != null) {
            return this.enclosingEnvironment.find(name);
        }
        throw new RuntimeException("Could not find " + name + " in the environment\n");
    }

    public abstract void bind(NamedDeclaration decl, Value value, Memory memory);

    @Override
    public boolean isFrame() {
        return true;
    }
}
