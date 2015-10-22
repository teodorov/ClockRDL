package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Frame;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.values.BooleanValue;
import ClockRDL.interpreter.values.IntegerValue;
import ClockRDL.interpreter.values.LValue;
import ClockRDL.interpreter.values.NulValue;
import ClockRDL.model.kernel.NamedDeclaration;
import ClockRDL.model.kernel.Statement;
import ClockRDL.model.statements.*;
import ClockRDL.model.statements.util.StatementsSwitch;
import org.eclipse.emf.ecore.EObject;

/**
 * Created by ciprian on 21/10/15.
 */
public class StatementEvaluator extends StatementsSwitch<Boolean> {
    Interpreter interpreter;
    Frame currentFrame;

    public StatementEvaluator(Interpreter interpreter, Frame env) {
        this.interpreter = interpreter;
        this.currentFrame = env;
    }

    @Override
    public Boolean caseAssignmentStmt(AssignmentStmt object) {

        Value rhs = interpreter.evaluate(object.getRhs(), currentFrame);
        LValue lhs = interpreter.lvalue(object.getLhs(), currentFrame);
        Value result = NulValue.uniqueInstance;
        Value lhsV;

        switch (object.getOperator()) {
            case ASSIGN:
                result = rhs;
                break;
            case ANDASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), currentFrame);
                if (!(rhs.isBooleanValue() && lhsV.isBooleanValue())) {
                    throw new RuntimeException("Cannot &= non boolean values");
                }
                result = BooleanValue.value(((BooleanValue)lhsV).data && ((BooleanValue)rhs).data);
                break;
            case ORASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), currentFrame);
                if (!(rhs.isBooleanValue() && lhsV.isBooleanValue())) {
                    throw new RuntimeException("Cannot |= non boolean values");
                }
                result = BooleanValue.value(((BooleanValue)lhsV).data || ((BooleanValue)rhs).data);
                break;
            case DIVASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), currentFrame);
                if (!(rhs.isIntegerValue() && lhsV.isIntegerValue())) {
                    throw new RuntimeException("Cannot /= non integer values");
                }
                result = IntegerValue.value(((IntegerValue) lhsV).data / ((IntegerValue) rhs).data);
                break;
            case MINUSASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), currentFrame);
                if (!(rhs.isIntegerValue() && lhsV.isIntegerValue())) {
                    throw new RuntimeException("Cannot -= non integer values");
                }
                result = IntegerValue.value(((IntegerValue) lhsV).data - ((IntegerValue) rhs).data);
                break;
            case MODASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), currentFrame);
                if (!(rhs.isIntegerValue() && lhsV.isIntegerValue())) {
                    throw new RuntimeException("Cannot %= non integer values");
                }
                result = IntegerValue.value(((IntegerValue) lhsV).data % ((IntegerValue) rhs).data);
                break;
            case MULTASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), currentFrame);
                if (!(rhs.isIntegerValue() && lhsV.isIntegerValue())) {
                    throw new RuntimeException("Cannot *= non integer values");
                }
                result = IntegerValue.value(((IntegerValue) lhsV).data * ((IntegerValue) rhs).data);
                break;
            case PLUSASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), currentFrame);
                if (!(rhs.isIntegerValue() && lhsV.isIntegerValue())) {
                    throw new RuntimeException("Cannot += non integer values");
                }
                result = IntegerValue.value(((IntegerValue) lhsV).data + ((IntegerValue) rhs).data);
                break;

        }

        lhs.assign(result, currentFrame);
        return true;
    }

    @Override
    public Boolean caseConditionalStmt(ConditionalStmt object) {
        BooleanValue condition = interpreter.evaluate(object.getCondition(), currentFrame, BooleanValue.class);

        if (condition.data == true) {
            doSwitch(object.getTrueBranch());
        }
        else {
            doSwitch(object.getFalseBranch());
        }
        return true;
    }

    @Override
    public Boolean caseLoopStmt(LoopStmt object) {
        BooleanValue condition = interpreter.evaluate(object.getCondition(), currentFrame, BooleanValue.class);

        while (condition.data == true) {
            doSwitch(object.getBody());
            condition = interpreter.evaluate(object.getCondition(), currentFrame, BooleanValue.class);
        }
        return true;
    }

    @Override
    public Boolean caseReturnStmt(ReturnStmt object) {
        Value result = interpreter.evaluate(object.getExp(), currentFrame);
        //TODO implement return statement
        return super.caseReturnStmt(object);
    }

    @Override
    public Boolean caseBlockStmt(BlockStmt object) {
        //This pushes a new Frame with its local variables
        //the evaluates the statements using the environment extended with this new frame
        boolean hadFrame = false;
        if (object.getDeclarations().size()>0) {
            hadFrame = true;
            Frame myFrame = new Frame("block", currentFrame);
            currentFrame = myFrame;

            for (NamedDeclaration decl : object.getDeclarations()) {
                myFrame.bind(decl, interpreter.evaluate(decl, currentFrame));
            }
        }

        for (Statement stmt : object.getStatements()) {
            doSwitch(stmt);
        }

        if (hadFrame) {
            //restor the current frame
            currentFrame = currentFrame.getEnclosingEnvironment();
        }
        return true;
    }

    @Override
    public Boolean defaultCase(EObject object) {
        KernelEvaluator ev = new KernelEvaluator(interpreter, currentFrame);
        ev.doSwitch(object);
        return true;
    }
}
