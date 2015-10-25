package ClockRDL.interpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ciprian on 25/10/15.
 */
public class Memory {
    //the constants list holds the values of the constants and of the functions
    List<Value> constants = new ArrayList<>();
    //the variables list holds the values of the variables
    List<Value> variables = new ArrayList<>();

    public int allocateVariable(Value initialValue) {
        int address = variables.size();
        variables.add(initialValue);
        return address;
    }
    public int allocateConstant(Value initialValue) {
        int address = constants.size();
        constants.add(initialValue);
        return address;
    }

    public Value getConstant(int address) {
        return constants.get(address);
    }

    public Value getVariable(int address) {
        return variables.get(address);
    }

    public void updateVariable(int address, Value value) {
        if (!variables.get(address).isAssignmentCompatible(value)) {
            throw new RuntimeException("Incompatible assignment of " + value.getClass().getSimpleName() + " to a variable of " + variables.get(address).getClass().getSimpleName());
        }
        variables.set(address, value);
    }
}
