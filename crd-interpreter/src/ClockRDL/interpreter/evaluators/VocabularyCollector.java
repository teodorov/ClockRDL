package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.interpreter.values.ClockValue;
import ClockRDL.interpreter.values.NullValue;
import ClockRDL.model.declarations.*;
import ClockRDL.model.declarations.util.DeclarationsSwitch;
import ClockRDL.model.expressions.literals.ClockLiteral;
import ClockRDL.model.kernel.NamedDeclaration;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Created by ciprian on 07/11/15.
 */
public class VocabularyCollector extends DeclarationsSwitch<Boolean> {
    Interpreter interpreter;
    int currentPrimitiveID = 0;
    Set<ClockLiteral> vocabulary[];
    Set<ClockLiteral> allClocks;
    Set<ClockLiteral> freeClocks; // here we keep the clocks that are not constrained by the relations

    public VocabularyCollector(Interpreter interpreter, int numberOfRelations) {
        this.interpreter = interpreter;
        vocabulary = new Set[numberOfRelations];
        freeClocks = Collections.newSetFromMap(new IdentityHashMap<>());
        allClocks  = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int i = 0; i<numberOfRelations;i++) {
            vocabulary[i] = new HashSet<>();
        }
    }

    public Set<ClockLiteral>[] getVocabulary() {
        return vocabulary;
    }

    public Set<ClockLiteral> getAllClocks() {
        return allClocks;
    }

    public Set<ClockLiteral> getFreeClocks() {
        return freeClocks;
    }

    @Override
    public Boolean caseRelationInstanceDecl(RelationInstanceDecl object) {
        interpreter.getEnvironment().push((AbstractFrame)interpreter.getEnvironment().lookup(object));
        doSwitch(object.getRelation());
        interpreter.getEnvironment().pop();
        return true;
    }

    @Override
    public Boolean casePrimitiveRelationDecl(PrimitiveRelationDecl object) {
        for (NamedDeclaration decl : object.getDeclarations()) {
            if (decl instanceof ClockDecl) {
                Value value = interpreter.getEnvironment().lookup(decl);

                checkClockValue(object, decl, value);

                processConstrainedClock(value);
                vocabulary[currentPrimitiveID].add(((ClockValue)value).literal);
            }
        }
        currentPrimitiveID++;
        return true;
    }

    @Override
    public Boolean caseCompositeRelationDecl(CompositeRelationDecl object) {

        for (NamedDeclaration decl : object.getDeclarations()) {
            if (decl instanceof ClockDecl) {
                Value value = interpreter.getEnvironment().lookup(decl);

                checkClockValue(object, decl, value);

                processNewClock(value);
            }
        }

        for (ClockDecl decl : object.getInternalClocks()) {
            Value value = interpreter.getEnvironment().lookup(decl);

            checkClockValue(object, decl, value);

            processNewClock(value);
        }

        for (RelationInstanceDecl instance : object.getInstances()) {
            doSwitch(instance);
        }

        return true;
    }

    void processNewClock(Value clockValue) {
        ClockLiteral clock = ((ClockValue)clockValue).literal;
        if (allClocks.contains(clock)) return;
        allClocks.add(clock);
        freeClocks.add(clock);
    }

    void processConstrainedClock(Value clockValue) {
        ClockLiteral clock = ((ClockValue)clockValue).literal;
        if (allClocks.contains(clock)) {
            freeClocks.remove(clock);
            return;
        }
        allClocks.add(clock);
    }

    public void checkClockValue(AbstractRelationDecl object, NamedDeclaration decl, Value value) {
        if (value == NullValue.uniqueInstance) {
            throw new RuntimeException("Clock named " + decl.getName() + " is not bound in " + object.getName());
        }

        if (!(value instanceof ClockValue)) {
            throw new RuntimeException("Clock named " + decl.getName() + " is not bound to a real clock in " + object.getName());
        }
    }
}
