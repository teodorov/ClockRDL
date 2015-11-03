package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Environment;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.interpreter.frames.GlobalFrame;
import ClockRDL.interpreter.frames.PersistentFrame;
import ClockRDL.interpreter.values.FunctionValue;
import ClockRDL.interpreter.values.NullValue;
import ClockRDL.model.declarations.*;
import ClockRDL.model.declarations.util.DeclarationsSwitch;
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

    public DeclarationEvaluator(Interpreter interpreter, Environment env) {
        this.interpreter = interpreter;
        this.environment = env;
    }

    @Override
    public Value caseClockDecl(ClockDecl object) {
        Value value = NullValue.uniqueInstance;
        if (object.getInitial() != null)  value = interpreter.evaluate(object.getInitial(), environment);
//        if (value.isNulValue()) {
//            throw new RuntimeException("Clock named '" + object.getName() + "' is not mapped");
//        }
        return value;
    }

    @Override
    public Value caseVariableDecl(VariableDecl object) {
        Value value = NullValue.uniqueInstance;
        if (object.getInitial() != null) value = interpreter.evaluate(object.getInitial(), environment);
        return value;
    }

    @Override
    public Value caseConstantDecl(ConstantDecl object) {
        Value value = NullValue.uniqueInstance;
        if (object.getInitial() != null) value = interpreter.evaluate(object.getInitial(), environment);
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
        //set the actuals in the frame of the relation, these values are interpreted in the currentFrame
        for (Map.Entry<String, Expression> entry :  object.getArgumentMap()) {
            Value value = interpreter.evaluate(entry.getValue(), environment);
            currentArgumentMap.put(entry.getKey(), value);
        }
        //now with the actuals set evaluate the initialization expressions dont create a new frame
        return doSwitch(object.getRelation());
    }

    @Override
    public AbstractFrame casePrimitiveRelationDecl(PrimitiveRelationDecl object) {
        //the primitive relation declaration does not bind itself to the parent environment
        //its Frame should be bound the relation instantiation
        AbstractFrame myFrame = new PersistentFrame(object.getName(), environment.currentFrame());
        environment.push(myFrame); //the initialization expressions should be interpreted in the context of the frame

        for (ArgumentDecl arg : object.getArguments()) {
            Value actualValue = currentArgumentMap.get(arg.getName());
            Value value = actualValue == null ? doSwitch(arg) : actualValue;
            environment.bind(arg, value);
        }

        for (NamedDeclaration decl : object.getDeclarations()) {
            Value actualValue = currentArgumentMap.get(decl.getName());
            Value value = actualValue == null ? doSwitch(decl) : actualValue;
            environment.bind(decl, value);
        }


        return environment.pop();
    }

    @Override
    public AbstractFrame caseCompositeRelationDecl(CompositeRelationDecl object) {
        //the composite relation declaration does not bind itself to the parent environment
        //its Frame should be bound the relation instantiation
        AbstractFrame myFrame = new PersistentFrame(object.getName(), environment.currentFrame());
        environment.push(myFrame);//the initialization expressions should be interpreted in the context of the frame

        for (ArgumentDecl arg : object.getArguments()) {
            Value actualValue = currentArgumentMap.get(arg.getName());
            Value value = actualValue == null ? doSwitch(arg) : actualValue;
            environment.bind(arg, value);
        }

        for (NamedDeclaration decl : object.getDeclarations()) {
            Value actualValue = currentArgumentMap.get(decl.getName());
            Value value = actualValue == null ? doSwitch(decl) : actualValue;
            environment.bind(decl, value);
        }

        for (RelationInstanceDecl instance : object.getInstances()) {
            environment.bind(instance, doSwitch(instance));
        }

        return environment.pop();
    }
}
