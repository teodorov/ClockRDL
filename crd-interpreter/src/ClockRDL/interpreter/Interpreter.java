package ClockRDL.interpreter;

import ClockRDL.interpreter.evaluators.*;
import ClockRDL.interpreter.values.ClockValue;
import ClockRDL.interpreter.values.LValue;
import ClockRDL.model.declarations.RelationInstanceDecl;
import ClockRDL.model.expressions.Literal;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.NamedDeclaration;
import ClockRDL.model.kernel.Statement;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by ciprian on 19/10/15.
 */
public class Interpreter {
    int relationCount;
    private Environment environment;
    Set<ClockValue> vocabularies[];
    Set<ClockValue> allClocks;
    Set<ClockValue> freeClocks; // here we keep the clocks that are not constrained by the relations

    public Environment getEnvironment() {
        return environment == null ? environment = new Environment() : environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public int getRelationCount() {
        return relationCount;
    }

    public <T extends Value> T evaluate(Expression exp, Class<T> type) {
        ExpressionEvaluator evaluator = new ExpressionEvaluator(this);
        Value value = evaluator.doSwitch(exp);
        if (type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        }
        throw new RuntimeException("Expected " + type.getSimpleName() + " but found " + value.getClass().getSimpleName());
    }

    public Value evaluate(Expression exp) {
        return evaluate(exp, Value.class);
    }

    public <T extends Value> T evaluate(Literal exp, Class<T> type) {
        LiteralEvaluator evaluator = new LiteralEvaluator(this);
        Value value = evaluator.doSwitch(exp);
        if (type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        }
        throw new RuntimeException("Expected " + type.getSimpleName() + " but found " + value.getClass().getSimpleName());
    }

    public Value evaluate(Literal exp) {
        return evaluate(exp, Value.class);
    }

    public LValue lvalue(Expression exp) {
        ExpressionLValueEvaluator evaluator = new ExpressionLValueEvaluator(this);
        Value value = evaluator.doSwitch(exp);
        if (value instanceof LValue) {
            return (LValue)value;
        }
        throw new RuntimeException("Expected an Lvalue but found " + value.getClass().getSimpleName());
    }

    public Value evaluate(NamedDeclaration decl) {
        DeclarationEvaluator evaluator = new DeclarationEvaluator(this);

        return evaluator.doSwitch(decl);
    }

    public void evaluate(Statement statement) {
        StatementEvaluator evaluator = new StatementEvaluator(this);

        evaluator.doSwitch(statement);
    }

    //returns the number of primitive relations
    public void initialize(RelationInstanceDecl instance) {
        getEnvironment().setMemory(new Memory());
        DeclarationEvaluator evaluator = new DeclarationEvaluator(this);
        getEnvironment().bind(instance, evaluator.doSwitch(instance));
        relationCount = evaluator.getPrimitiveRelationCount();
        collectVocabularies(instance);
    }

    private void collectVocabularies(RelationInstanceDecl instance) {
        VocabularyCollector collector = new VocabularyCollector(this, relationCount);
        collector.doSwitch(instance);


        allClocks = collector.getAllClocks();
        freeClocks = collector.getFreeClocks();
        vocabularies = collector.getVocabularies();

        int i = 0;
        for (ClockValue clock : allClocks) {
            clock.setID(i++);
        }
    }

    public Set<ClockValue>[] getVocabularies() {
        return vocabularies;
    }

    public Set<ClockValue> getAllClocks() {
        return allClocks;
    }

    public Set<ClockValue> getFreeClocks() {
        return freeClocks;
    }

    public int[] getRelationVocabulary(int relationID) {
        int[] vocabulary = new int[vocabularies[relationID].size()];
        int i = 0;
        for (ClockValue clock : vocabularies[relationID]) {
            vocabulary[i++] = clock.getID();
        }
        Arrays.sort(vocabulary);
        return vocabulary;
    }

    public void reset() {
        environment = null;
        vocabularies = null;
        allClocks = null;
        freeClocks = null;
    }


    public Set<FireableTransition> fireableTransitions(RelationInstanceDecl instance) {
        TransitionCollector collector = new TransitionCollector();
        return collector.collectTransitions(instance, this);
    }

    public void evaluate(FireableTransition fireableTransition) {
        Statement action = fireableTransition.transition.getAction();
        if (action != null) {
            getEnvironment().push(fireableTransition.executionContext);
            this.evaluate(fireableTransition.transition.getAction());
            getEnvironment().pop();
        }
    }

    public int getClockCount() {
        return allClocks.size();
    }

    //TODO clarify the difference between the Scope computed during parsing and the execution Frame
    //Actually I think that during parsing we can build the SymbolTable
    //in the SymbolTable we could add references to the memory representation which is a composed value of:
    //<Clocks, Constants, Variables>
    //The Variables component of the memory represents the configuration needed for exploration

    //TODO the current implementation of functions should be implementable with static references to outerScope
    //TODO implement Function CALL
}
