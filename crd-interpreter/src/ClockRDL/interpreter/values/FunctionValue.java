package ClockRDL.interpreter.values;

import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.model.declarations.FunctionDecl;

/**
 * Created by ciprian on 20/10/15.
 */
public class FunctionValue extends Value{
    //a function value is a closure
    public FunctionDecl data;
    //the declaration environment is set when interpreting the function declaration
    //this way I get to know what is the environment captured
    //I don't really need this since all the functions are statically bound
    //I would need this if I ever wanted to pass functions as arguments
    public AbstractFrame declarationEnvironment;

    @Override
    public boolean isAssignmentCompatible(Value value) {
        return false;
    }

    @Override
    public boolean isFunctionValue() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof FunctionValue)) return false;
        FunctionValue fV = (FunctionValue)obj;
        //we are the same function value if our data points to the same function declaration
        return data == fV.data;
    }
}
