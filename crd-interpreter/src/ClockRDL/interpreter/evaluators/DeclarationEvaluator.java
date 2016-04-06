package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Environment;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.interpreter.frames.CompositeRelationFrame;
import ClockRDL.interpreter.frames.GlobalFrame;
import ClockRDL.interpreter.frames.PrimitiveRelationFrame;
import ClockRDL.interpreter.values.FunctionValue;
import ClockRDL.interpreter.values.NullValue;
import ClockRDL.model.declarations.*;
import ClockRDL.model.declarations.util.DeclarationsSwitch;
import ClockRDL.model.expressions.literals.ClockLiteral;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.NamedDeclaration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ciprian on 21/10/15.
 */
public class DeclarationEvaluator extends DeclarationsSwitch<Value> {
    Interpreter interpreter;
    Environment environment;
    int currentPrimitiveID = 0;
    Map<String, ClockLiteral> clocks = new HashMap<>();

    public DeclarationEvaluator(Interpreter interpreter) {
        this.interpreter = interpreter;
        this.environment = interpreter.getEnvironment();
    }

    public int getPrimitiveRelationCount() {
        return currentPrimitiveID;
    }

    @Override
    public Value caseClockDecl(ClockDecl object) {
        Value value = NullValue.uniqueInstance;
        if (object.getInitial() != null)  value = interpreter.evaluate(object.getInitial());
//        if (value.isNulValue()) {
//            throw new RuntimeException("Clock named '" + object.getName() + "' is not mapped");
//        }
        return value;
    }

    @Override
    public Value caseVariableDecl(VariableDecl object) {
        Value value = NullValue.uniqueInstance;
        if (object.getInitial() != null) value = interpreter.evaluate(object.getInitial());
        return value;
    }

    @Override
    public Value caseConstantDecl(ConstantDecl object) {
        Value value = NullValue.uniqueInstance;
        if (object.getInitial() != null) value = interpreter.evaluate(object.getInitial());
        return value;
    }

    @Override
    public Value caseArgumentDecl(ArgumentDecl object) {
        return NullValue.uniqueInstance;
    }

    @Override
    public Value caseFunctionDecl(FunctionDecl object) {
        FunctionValue value = new FunctionValue();
        value.data = object;
        value.declarationEnvironment = environment.currentFrame();
        return value;
    }

    @Override
    public Value casePrimitiveFunctionDecl(PrimitiveFunctionDecl object) {
        return GlobalFrame.primitives.get(object.getName());
    }

    Map<String, Value> currentArgumentMap;

    @Override
    public Value caseRelationInstanceDecl(RelationInstanceDecl object) {
        currentArgumentMap = new HashMap<>();
        //set the actuals in the a global currentArgumentMap, which is used by the relation to put the actual in the frame
        for (Map.Entry<String, Expression> entry :  object.getArgumentMap()) {
            Value value = interpreter.evaluate(entry.getValue());
            currentArgumentMap.put(entry.getKey(), value);
        }

        return doSwitch(object.getRelation());
    }

    @Override
    public AbstractFrame casePrimitiveRelationDecl(PrimitiveRelationDecl object) {
        AbstractFrame myFrame = new PrimitiveRelationFrame(object.getName(), currentPrimitiveID, environment.currentFrame());
        currentPrimitiveID++;
        environment.push(myFrame); //the initialization expressions should be interpreted in the context of the frame

        //bind my arguments in my frame
        for (ArgumentDecl arg : object.getArguments()) {
            Value actualValue = currentArgumentMap.get(arg.getName());
            Value value = actualValue == null ? doSwitch(arg) : actualValue;
            environment.bind(arg, value);
        }

        //bind my clocks, constants, variables, and functions in my frame
        for (NamedDeclaration decl : object.getDeclarations()) {
            Value actualValue = currentArgumentMap.get(decl.getName());
            //if there is no actualValue set, get the initial value
            Value value = actualValue == null ? doSwitch(decl) : actualValue;
            environment.bind(decl, value);
        }

        return environment.pop();
    }

    @Override
    public AbstractFrame caseCompositeRelationDecl(CompositeRelationDecl object) {
        AbstractFrame myFrame = new CompositeRelationFrame(object.getName(), environment.currentFrame());
        environment.push(myFrame);//the initialization expressions should be interpreted in the context of the frame

        //bind my arguments in my frame
        for (ArgumentDecl arg : object.getArguments()) {
            Value actualValue = currentArgumentMap.get(arg.getName());
            Value value = actualValue == null ? doSwitch(arg) : actualValue;
            environment.bind(arg, value);
        }

        //bind my clocks, constants, variables, and functions in my frame
        for (NamedDeclaration decl : object.getDeclarations()) {
            Value actualValue = currentArgumentMap.get(decl.getName());
            //if there is no actualValue set, get the initial value
            Value value = actualValue == null ? doSwitch(decl) : actualValue;
            environment.bind(decl, value);
        }

        //bind my internal clocks in my frame
        for (ClockDecl decl : object.getInternalClocks()) {
            Value value = doSwitch(decl);
            environment.bind(decl, value);
        }

        for (RelationInstanceDecl instance : object.getInstances()) {
            //in my frame bind each instance to its corresponding frame
            environment.bind(instance, doSwitch(instance));
        }

        return environment.pop();
    }
}
