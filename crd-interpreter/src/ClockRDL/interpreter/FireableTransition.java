package ClockRDL.interpreter;

import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.model.declarations.TransitionDecl;

/**
 * Created by ciprian on 23/10/15.
 */
public class FireableTransition {
    AbstractFrame executionContext;
    TransitionDecl transition;
    public FireableTransition(AbstractFrame executionContext, TransitionDecl transition) {
        this.executionContext = executionContext;
        this.transition = transition;
    }
}
