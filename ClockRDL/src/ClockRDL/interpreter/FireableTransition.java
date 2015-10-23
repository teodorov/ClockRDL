package ClockRDL.interpreter;

import ClockRDL.model.declarations.TransitionDecl;

/**
 * Created by ciprian on 23/10/15.
 */
public class FireableTransition {
    Frame executionContext;
    TransitionDecl transition;
    public FireableTransition(Frame executionContext, TransitionDecl transition) {
        this.executionContext = executionContext;
        this.transition = transition;
    }
}
