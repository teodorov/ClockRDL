package ClockRDL.interpreter;

import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.interpreter.frames.PrimitiveRelationFrame;
import ClockRDL.model.declarations.TransitionDecl;

/**
 * Created by ciprian on 23/10/15.
 */
public class FireableTransition {
    PrimitiveRelationFrame executionContext;
    TransitionDecl transition;
    public FireableTransition(PrimitiveRelationFrame executionContext, TransitionDecl transition) {
        this.executionContext = executionContext;
        this.transition = transition;
    }

    public int getPrimitiveRelationID() {
        return executionContext.getPrimitiveID();
    }
}
