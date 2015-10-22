package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Frame;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.values.FunctionValue;
import ClockRDL.interpreter.values.NulValue;
import ClockRDL.model.declarations.*;
import ClockRDL.model.declarations.util.DeclarationsSwitch;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.NamedDeclaration;

import java.util.Map;

/**
 * Created by ciprian on 21/10/15.
 */
public class DeclarationEvaluator extends DeclarationsSwitch<Value> {
    Interpreter interpreter;
    Frame currentFrame;

    public DeclarationEvaluator(Interpreter interpreter, Frame env) {
        this.interpreter = interpreter;
        this.currentFrame = env;
    }

    public void setCurrentFrame(Frame frame) {
        this.currentFrame = frame;
    }

    @Override
    public Value caseClockDecl(ClockDecl object) {
        Value value = NulValue.uniqueInstance;
        if (object.getInitial() != null)  value = interpreter.evaluate(object.getInitial(), currentFrame);
        return value;
    }

    @Override
    public Value caseVariableDecl(VariableDecl object) {
        Value value = NulValue.uniqueInstance;
        if (object.getInitial() != null) value = interpreter.evaluate(object.getInitial(), currentFrame);
        return value;
    }

    @Override
    public Value caseConstantDecl(ConstantDecl object) {
        Value value = NulValue.uniqueInstance;
        if (object.getInitial() != null) value = interpreter.evaluate(object.getInitial(), currentFrame);
        return value;
    }

    @Override
    public Value caseArgumentDecl(ArgumentDecl object) {
        return NulValue.uniqueInstance;
    }

    @Override
    public Value caseFunctionDecl(FunctionDecl object) {
        FunctionValue value = new FunctionValue();
        value.data = object;
        value.declarationEnvironment = currentFrame;
        return value;
    }

    @Override
    public Value caseParameterDecl(ParameterDecl object) {
        return NulValue.uniqueInstance;
    }

    boolean isCreatingFrame = true;
    @Override
    public Value caseRelationInstanceDecl(RelationInstanceDecl object) {
        //this creates a frame with the default values
        isCreatingFrame = true;
        Frame frame = (Frame)doSwitch(object.getRelation());

        //set the actuals in the frame of the relation, these values are interpreted in the currentFrame
        for (Map.Entry<String, Expression> entry :  object.getArgumentMap()) {
            Value value = interpreter.evaluate(entry.getValue(), currentFrame);
            frame.bind(entry.getKey(), value);
        }
        //now with the actuals set reevaluate the initialization expressions dont create a new frame
        isCreatingFrame = false;
        currentFrame = frame;
        frame = (Frame) doSwitch(object.getRelation());
        currentFrame = frame.getEnclosingEnvironment();
        return frame;
    }

    @Override
    public Frame casePrimitiveRelationDecl(PrimitiveRelationDecl object) {
        //the primitive relation declaration does not bind itself to the parent environment
        //its Frame should be bound the relation instantiation
        Frame myFrame = currentFrame;
        if (isCreatingFrame) {
            myFrame = new Frame(object.getName(), currentFrame);
            currentFrame = myFrame;//the initialization expressions should be interpreted in the context of the frame
        }

        for (ArgumentDecl arg : object.getArguments()) {
            myFrame.bind(arg, doSwitch(arg));
        }

        for (NamedDeclaration decl : object.getDeclarations()) {
            myFrame.bind(decl, doSwitch(decl));
        }

        if (isCreatingFrame) {
            currentFrame = myFrame.getEnclosingEnvironment();
        }
        return myFrame;
    }

    @Override
    public Frame caseCompositeRelationDecl(CompositeRelationDecl object) {
        //the composite relation declaration does not bind itself to the parent environment
        //its Frame should be bound the relation instantiation
        Frame myFrame = currentFrame;
        if (isCreatingFrame) {
            myFrame = new Frame(object.getName(), currentFrame);
            currentFrame = myFrame;//the initialization expressions should be interpreted in the context of the frame
        }

        for (ArgumentDecl arg : object.getArguments()) {
            myFrame.bind(arg, doSwitch(arg));
        }

        for (NamedDeclaration decl : object.getDeclarations()) {
            myFrame.bind(decl, doSwitch(decl));
        }

        for (RelationInstanceDecl instance : object.getInstances()) {
            myFrame.bind(instance, doSwitch(instance));
        }

        if (isCreatingFrame) {
            currentFrame = myFrame.getEnclosingEnvironment();
        }
        return myFrame;
    }
}
