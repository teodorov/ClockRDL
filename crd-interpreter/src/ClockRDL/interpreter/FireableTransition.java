package ClockRDL.interpreter;

import ClockRDL.interpreter.frames.PrimitiveRelationFrame;
import ClockRDL.interpreter.values.ClockValue;
import ClockRDL.model.declarations.TransitionDecl;
import ClockRDL.model.expressions.ClockReference;
import plug.modules.synchronization.byClocks.IClockedTransition;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ciprian on 23/10/15.
 */
public class FireableTransition implements IClockedTransition {
    PrimitiveRelationFrame executionContext;
    TransitionDecl transition;
    int[] vector;
    public FireableTransition(TransitionDecl transition, PrimitiveRelationFrame executionContext, Memory memory) {
        this.executionContext = executionContext;
        this.transition = transition;

        List<ClockReference> clocks = transition.getVector();
        vector = new int[clocks.size()];
        int i = 0;
        for (ClockReference clk : clocks) {
            ClockValue value = (ClockValue)executionContext.lookup(clk.getRef(), memory);
            vector[i++] = value.getID();
        }

        Arrays.sort(vector);
    }

    public int getPrimitiveRelationID() {
        return executionContext.getPrimitiveID();
    }

    @Override
    public int[] vector() {
        return vector;
    }
}
