package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;
import ClockRDL.model.expressions.literals.ClockLiteral;

/**
 * Created by ciprian on 21/10/15.
 */
public class ClockValue extends Value {
    ClockLiteral literal;
    public ClockValue(ClockLiteral literal)
    {
        this.literal = literal;
    }

    @Override
    public String toString() {
        return literal.getName();
    }
}
