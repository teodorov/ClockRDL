package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;
import ClockRDL.model.expressions.literals.ClockLiteral;

/**
 * Created by ciprian on 21/10/15.
 */
public class ClockValue extends Value {
    int id;
    public ClockLiteral literal;

    public ClockValue(ClockLiteral literal)
    {
        this.literal = literal;
    }

    public int getID() {
        return id;
    }

    public void setID(int inID) {
        id = inID;
    }

    @Override
    public String toString() {
        return literal.getName();
    }

    @Override
    public boolean isAssignmentCompatible(Value value) {
        return false;
    }
}
