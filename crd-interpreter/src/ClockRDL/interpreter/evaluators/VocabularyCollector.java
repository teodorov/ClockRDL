package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Environment;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.interpreter.values.ClockValue;
import ClockRDL.interpreter.values.NullValue;
import ClockRDL.model.declarations.ClockDecl;
import ClockRDL.model.declarations.CompositeRelationDecl;
import ClockRDL.model.declarations.PrimitiveRelationDecl;
import ClockRDL.model.declarations.RelationInstanceDecl;
import ClockRDL.model.declarations.util.DeclarationsSwitch;
import ClockRDL.model.expressions.literals.ClockLiteral;
import ClockRDL.model.kernel.NamedDeclaration;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ciprian on 07/11/15.
 */
public class VocabularyCollector extends DeclarationsSwitch<Boolean> {
    Interpreter interpreter;
    Environment environment;
    int currentPrimitiveID = 0;
    Set<ClockLiteral> vocabulary[];

    public VocabularyCollector(Interpreter interpreter, Environment env, int numberOfRelations) {
        this.interpreter = interpreter;
        this.environment = env;
        vocabulary = new Set[numberOfRelations];
        for (int i = 0; i<numberOfRelations;i++) {
            vocabulary[i] = new HashSet<>();
        }
    }

    public Set<ClockLiteral>[] getVocabulary() {
        return vocabulary;
    }

    @Override
    public Boolean caseRelationInstanceDecl(RelationInstanceDecl object) {
        environment.push((AbstractFrame)environment.lookup(object));
        doSwitch(object.getRelation());
        environment.pop();
        return true;
    }

    @Override
    public Boolean casePrimitiveRelationDecl(PrimitiveRelationDecl object) {
        for (NamedDeclaration decl : object.getDeclarations()) {
            if (decl instanceof ClockDecl) {
                Value value = environment.lookup(decl);

                if (value == NullValue.uniqueInstance) {
                    throw new RuntimeException("Clock named " + decl.getName() + " is not bound in " + object.getName());
                }

                if (value instanceof ClockValue) {
                    throw new RuntimeException("Clock named " + decl.getName() + " is not bound to a real clock in " + object.getName());
                }

                vocabulary[currentPrimitiveID].add(((ClockValue)value).literal);
            }
        }
        currentPrimitiveID++;
        return true;
    }

    @Override
    public Boolean caseCompositeRelationDecl(CompositeRelationDecl object) {
        for (RelationInstanceDecl instance : object.getInstances()) {
            doSwitch(instance);
        }

        return true;
    }
}
