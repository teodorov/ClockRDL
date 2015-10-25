package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Environment;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.frames.AbstractFrame;
import ClockRDL.interpreter.frames.TemporaryFrame;
import ClockRDL.interpreter.values.*;
import ClockRDL.model.declarations.ArgumentDecl;
import ClockRDL.model.expressions.*;
import ClockRDL.model.expressions.util.ExpressionsSwitch;
import ClockRDL.model.kernel.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ciprian on 21/10/15.
 */
public class ExpressionEvaluator extends ExpressionsSwitch<Value> {

    Interpreter interpreter;
    Environment environment;

    public ExpressionEvaluator(Interpreter interpreter, Environment env) {
        this.interpreter = interpreter;
        this.environment = env;
    }

    @Override
    public Value caseParenExp(ParenExp object) {
        return doSwitch(object.getExp());
    }

    @Override
    public Value caseIndexedExp(IndexedExp object) {
        Value prefix = doSwitch(object.getPrefix());
        Value index = doSwitch(object.getIndex());

        if (!(prefix.isArrayValue() || prefix.isQueueValue())) {
            throw new RuntimeException("Expected array or queue value but found " + prefix.getClass().getSimpleName());
        }

        if (!index.isIntegerValue()) {
            throw new RuntimeException("Expected integer value but found " + index.getClass().getSimpleName());
        }

        if (prefix.isArrayValue()) {
            return ((ArrayValue) prefix).data[((IntegerValue) index).data];
        } else {
            return ((QueueValue) prefix).data.get(((IntegerValue) index).data);
        }
    }

    @Override
    public Value caseSelectedExp(SelectedExp object) {
        Value prefix = doSwitch(object.getPrefix());
        //TODO it is not necessarily a record, can be the ramp for type specific fonctions
        if (!prefix.isRecordValue()) {

            PrimitiveFunctionValue primValue = prefix.primitives.get(object.getSelector());
            if (primValue == null) {
                throw new RuntimeException("Expected record value but found " + prefix.getClass().getSimpleName());
            }
            return primValue;
        }

        Value selectedValue = ((RecordValue) prefix).data.get(object.getSelector());

        if (selectedValue == null) {
            throw new RuntimeException("The record does not have a field named " + object.getSelector());
        }

        return selectedValue;
    }

    @Override
    public Value caseFunctionCallExp(FunctionCallExp object) {
        Value opaqueValue = doSwitch(object.getPrefix());

        if (!(opaqueValue.isFunctionValue() || opaqueValue.isPrimitiveFunctionValue())) {
            throw new RuntimeException("Expected function reference but found " + opaqueValue.getClass().getSimpleName());
        }

        //evaluate the arguments in order in the current environment
        List<Value> argList = new ArrayList<>(object.getArguments().size());
        for (Expression arg : object.getArguments()) {
            Value argValue = doSwitch(arg);
            argList.add(argValue);
        }

        if (opaqueValue.isFunctionValue()) {
            //interpret the functionDeclaration in the current environment extended with the functionFrame
            return applyClosure(((FunctionValue) opaqueValue), environment, argList);
        }
        //primitive call
        return (Value) ((PrimitiveFunctionValue)opaqueValue).fct.apply(argList);
    }

    public Value applyClosure(FunctionValue closure, Environment env, List<Value> argList) {
        AbstractFrame myFrame = new TemporaryFrame(closure.data.getName(), closure.declarationEnvironment);
        environment.push(myFrame); //the function should be interpreted in the context of the frame

        List<ArgumentDecl> formalList = closure.data.getArguments();
        if (argList.size() != formalList.size()) {
            throw new RuntimeException("Function '"+ closure.data.getName() +"' expects " +  formalList.size() + " arguments but was called with " + argList.size() + " arguments");
        }

        //bind the formals to actuals in the frame of the function
        int argIndex = 0;
        for (argIndex = 0; argIndex < argList.size(); argIndex++) {
            environment.bind(formalList.get(argIndex), argList.get(argIndex));
        }

        interpreter.evaluate(closure.data.getBody(), environment);

        environment.pop();

        Value returnValue = environment.returnRegister;
        environment.returnRegister = null;

        return returnValue;
    }

    @Override
    public Value caseReferenceExp(ReferenceExp object) {
        return environment.lookup(object.getRef());
    }

    @Override
    public Value caseClockReference(ClockReference object) {
        return environment.lookup(object.getRef());
    }

    @Override
    public Value caseUnaryExp(UnaryExp object) {
        Value operand = doSwitch(object.getOperand());
        switch (object.getOperator()) {
            case UNOT: {
                if (!operand.isBooleanValue()) {
                    throw new RuntimeException("Negation expression expects a boolean operand but found " + operand.getClass().getSimpleName());
                }
                return BooleanValue.value(!((BooleanValue) operand).data);
            }
            case UMINUS: {
                if (!operand.isIntegerValue()) {
                    throw new RuntimeException("Unary Minus expression expects an integer operand but found " + operand.getClass().getSimpleName());
                }
                return IntegerValue.value(-((IntegerValue) operand).data);
            }
            case UPLUS: {
                if (!operand.isIntegerValue()) {
                    throw new RuntimeException("Unary Plus expression expects an integer operand but found " + operand.getClass().getSimpleName());
                }
                return IntegerValue.value(((IntegerValue) operand).data);
            }
        }
        return null;
    }

    @Override
    public Value caseBinaryExp(BinaryExp object) {
        Value lhsV = doSwitch(object.getLhs());
        Value rhsV = doSwitch(object.getRhs());
        //need booleans
        switch (object.getOperator()) {
            //boolean
            case BAND:
            case BOR:
            case BNOR:
            case BXOR:
            case BNAND:
                if (!(lhsV.isBooleanValue() && rhsV.isBooleanValue())) {
                    throw new RuntimeException("Expected boolean operands but found " + lhsV.getClass().getSimpleName() + " and " + rhsV.getClass().getSimpleName());
                }
        }

        switch (object.getOperator()) {
            //relational
            case BGT:
            case BGE:
            case BLT:
            case BLE:
                //arithmetic
            case BDIV:
            case BMINUS:
            case BMOD:
            case BMUL:
            case BPLUS:
                if (!(lhsV.isIntegerValue() && rhsV.isIntegerValue())) {
                    throw new RuntimeException("Expected integer operands but found " + lhsV.getClass().getSimpleName() + " and " + rhsV.getClass().getSimpleName());
                }
        }


        switch (object.getOperator()) {
            //boolean
            case BAND: {
                BooleanValue lhs = ((BooleanValue) lhsV);
                BooleanValue rhs = ((BooleanValue) rhsV);
                return BooleanValue.value(lhs.data && rhs.data);
            }
            case BOR: {
                BooleanValue lhs = ((BooleanValue) lhsV);
                BooleanValue rhs = ((BooleanValue) rhsV);
                return BooleanValue.value(lhs.data || rhs.data);
            }
            case BNOR: {
                BooleanValue lhs = ((BooleanValue) lhsV);
                BooleanValue rhs = ((BooleanValue) rhsV);
                return BooleanValue.value(!(lhs.data || rhs.data));
            }
            case BXOR: {
                BooleanValue lhs = ((BooleanValue) lhsV);
                BooleanValue rhs = ((BooleanValue) rhsV);
                return BooleanValue.value((lhs.data || rhs.data) && !(lhs.data && rhs.data));

            }
            case BNAND: {
                BooleanValue lhs = ((BooleanValue) lhsV);
                BooleanValue rhs = ((BooleanValue) rhsV);
                return BooleanValue.value(!(lhs.data && rhs.data));

            }
            //equality
            case BNE: {
                return BooleanValue.value(!(lhsV.equals(rhsV)));

            }
            case BEQ: {
                return BooleanValue.value(lhsV.equals(rhsV));

            }
            //relational
            case BGT: {
                IntegerValue lhs = ((IntegerValue) lhsV);
                IntegerValue rhs = ((IntegerValue) rhsV);
                return BooleanValue.value(lhs.data > rhs.data);

            }
            case BGE: {
                IntegerValue lhs = ((IntegerValue) lhsV);
                IntegerValue rhs = ((IntegerValue) rhsV);
                return BooleanValue.value(lhs.data >= rhs.data);

            }
            case BLT: {
                IntegerValue lhs = ((IntegerValue) lhsV);
                IntegerValue rhs = ((IntegerValue) rhsV);
                return BooleanValue.value(lhs.data < rhs.data);

            }
            case BLE: {
                IntegerValue lhs = ((IntegerValue) lhsV);
                IntegerValue rhs = ((IntegerValue) rhsV);
                return BooleanValue.value(lhs.data <= rhs.data);

            }
            //arithmetic
            case BDIV: {
                IntegerValue lhs = ((IntegerValue) lhsV);
                IntegerValue rhs = ((IntegerValue) rhsV);
                return IntegerValue.value(lhs.data / rhs.data);

            }
            case BMINUS: {
                IntegerValue lhs = ((IntegerValue) lhsV);
                IntegerValue rhs = ((IntegerValue) rhsV);
                return IntegerValue.value(lhs.data - rhs.data);

            }
            case BMOD: {
                IntegerValue lhs = ((IntegerValue) lhsV);
                IntegerValue rhs = ((IntegerValue) rhsV);
                return IntegerValue.value(lhs.data % rhs.data);

            }
            case BMUL: {
                IntegerValue lhs = ((IntegerValue) lhsV);
                IntegerValue rhs = ((IntegerValue) rhsV);
                return IntegerValue.value(lhs.data * rhs.data);

            }
            case BPLUS: {
                IntegerValue lhs = ((IntegerValue) lhsV);
                IntegerValue rhs = ((IntegerValue) rhsV);
                return IntegerValue.value(lhs.data + rhs.data);
            }
        }

        return null;
    }

    @Override
    public Value caseConditionalExp(ConditionalExp object) {
        Value condition = doSwitch(object.getCondition());

        if (!condition.isBooleanValue()) {
            throw new RuntimeException("The value of the condition should be boolean but found " + condition.getClass().getSimpleName());
        }
        BooleanValue booleanCondition = (BooleanValue) condition;
        Value returnValue;
        if (booleanCondition.data) {
            returnValue = doSwitch(object.getTrueBranch());
        } else {
            returnValue = doSwitch(object.getFalseBranch());
        }
        return returnValue;
    }

    @Override
    public Value caseLiteral(Literal object) {
        return interpreter.evaluate(object, environment);
    }
}
