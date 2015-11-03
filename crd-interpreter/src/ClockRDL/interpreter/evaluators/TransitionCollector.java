package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Environment;
import ClockRDL.interpreter.FireableTransition;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.interpreter.values.BooleanValue;
import ClockRDL.model.declarations.CompositeRelationDecl;
import ClockRDL.model.declarations.PrimitiveRelationDecl;
import ClockRDL.model.declarations.RelationInstanceDecl;
import ClockRDL.model.declarations.TransitionDecl;
import ClockRDL.model.declarations.util.DeclarationsSwitch;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Created by ciprian on 23/10/15.
 */
public class TransitionCollector extends DeclarationsSwitch<Set<FireableTransition>> {
    Environment environment;
    Interpreter interpreter;

    //Frame is first because it represents the execution context,
    // in the same execution context we cannot have two transitions with the same identity
    // in different execution contexts we can have two transitions with the same identity due to relation instantiation
    public Set<FireableTransition> collectTransitions(RelationInstanceDecl instance, Environment env, Interpreter interpreter) {
        this.interpreter = interpreter;
        this.environment = env;
        return doSwitch(instance);
    }

    @Override
    public Set<FireableTransition> casePrimitiveRelationDecl(PrimitiveRelationDecl object) {
        Set<FireableTransition> fireable = Collections.newSetFromMap(new IdentityHashMap<>());
        for (TransitionDecl transitionDecl : object.getTransitions()) {
            BooleanValue guard = interpreter.evaluate(transitionDecl.getGuard(), environment, BooleanValue.class);
            if (guard.data) {
                fireable.add(new FireableTransition(environment.currentFrame(), transitionDecl));
            }
        }
        return fireable;
    }

    @Override
    public Set<FireableTransition> caseCompositeRelationDecl(CompositeRelationDecl object) {
        Set<FireableTransition> fireable = Collections.newSetFromMap(new IdentityHashMap<>());
        for (RelationInstanceDecl instanceDecl : object.getInstances()) {
            fireable.addAll(doSwitch(instanceDecl));
        }
        return fireable;
    }

    @Override
    public Set<FireableTransition> caseRelationInstanceDecl(RelationInstanceDecl object) {
        Set<FireableTransition> fireable;
        //currentEnvironment = (AbstractFrame) currentEnvironment.lookup(object);
        environment.push((AbstractFrame)environment.lookup(object));
        fireable = doSwitch(object.getRelation());
        //currentEnvironment = currentEnvironment.getEnclosingEnvironment();
        environment.pop();
        return fireable;
    }
}
