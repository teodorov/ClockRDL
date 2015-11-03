package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Environment;
import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.frames.TemporaryFrame;
import ClockRDL.interpreter.values.BooleanValue;
import ClockRDL.interpreter.values.IntegerValue;
import ClockRDL.interpreter.values.LValue;
import ClockRDL.interpreter.values.NullValue;
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
    Environment environment;

    public StatementEvaluator(Interpreter interpreter, Environment env) {
        this.interpreter = interpreter;
        this.environment = env;
    }

    @Override
    public Boolean caseAssignmentStmt(AssignmentStmt object) {

        Value rhs = interpreter.evaluate(object.getRhs(), environment);
        LValue lhs = interpreter.lvalue(object.getLhs(), environment);
        Value result = NullValue.uniqueInstance;
        Value lhsV;

        switch (object.getOperator()) {
            case ASSIGN:
                result = rhs;
                break;
            case ANDASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), environment);
                if (!(rhs.isBooleanValue() && lhsV.isBooleanValue())) {
                    throw new RuntimeException("Cannot &= non boolean values");
                }
                result = BooleanValue.value(((BooleanValue)lhsV).data && ((BooleanValue)rhs).data);
                break;
            case ORASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), environment);
                if (!(rhs.isBooleanValue() && lhsV.isBooleanValue())) {
                    throw new RuntimeException("Cannot |= non boolean values");
                }
                result = BooleanValue.value(((BooleanValue)lhsV).data || ((BooleanValue)rhs).data);
                break;
            case DIVASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), environment);
                if (!(rhs.isIntegerValue() && lhsV.isIntegerValue())) {
                    throw new RuntimeException("Cannot /= non integer values");
                }
                result = IntegerValue.value(((IntegerValue) lhsV).data / ((IntegerValue) rhs).data);
                break;
            case MINUSASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), environment);
                if (!(rhs.isIntegerValue() && lhsV.isIntegerValue())) {
                    throw new RuntimeException("Cannot -= non integer values");
                }
                result = IntegerValue.value(((IntegerValue) lhsV).data - ((IntegerValue) rhs).data);
                break;
            case MODASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), environment);
                if (!(rhs.isIntegerValue() && lhsV.isIntegerValue())) {
                    throw new RuntimeException("Cannot %= non integer values");
                }
                result = IntegerValue.value(((IntegerValue) lhsV).data % ((IntegerValue) rhs).data);
                break;
            case MULTASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), environment);
                if (!(rhs.isIntegerValue() && lhsV.isIntegerValue())) {
                    throw new RuntimeException("Cannot *= non integer values");
                }
                result = IntegerValue.value(((IntegerValue) lhsV).data * ((IntegerValue) rhs).data);
                break;
            case PLUSASSIGN:
                lhsV = interpreter.evaluate(object.getLhs(), environment);
                if (!(rhs.isIntegerValue() && lhsV.isIntegerValue())) {
                    throw new RuntimeException("Cannot += non integer values");
                }
                result = IntegerValue.value(((IntegerValue) lhsV).data + ((IntegerValue) rhs).data);
                break;

        }

        lhs.assign(result, environment);
        //the assign statement cannot stop the execution
        return true;
    }

    @Override
    public Boolean caseConditionalStmt(ConditionalStmt object) {
        BooleanValue condition = interpreter.evaluate(object.getCondition(), environment, BooleanValue.class);

        if (condition.data == true) {
            //the execution stops if a return is hit in the branch
            return doSwitch(object.getTrueBranch());
        }
        //the execution stops if a return is hit in the branch
        return doSwitch(object.getFalseBranch());
    }

    @Override
    public Boolean caseLoopStmt(LoopStmt object) {
        BooleanValue condition = interpreter.evaluate(object.getCondition(), environment, BooleanValue.class);

        while (condition.data == true) {
            if (!doSwitch(object.getBody())) return false;
            condition = interpreter.evaluate(object.getCondition(), environment, BooleanValue.class);
        }
        return true;
    }

    @Override
    public Boolean caseReturnStmt(ReturnStmt object) {
        Value result = interpreter.evaluate(object.getExp(), environment);
        environment.returnRegister = result;
        //returning false stops the execution of the block
        return false;
    }

    @Override
    public Boolean caseBlockStmt(BlockStmt object) {
        //This pushes a new Frame with its local variables
        //the evaluates the statements using the environment extended with this new frame
        boolean hadFrame = false;
        if (object.getDeclarations().size()>0) {
            hadFrame = true;
            TemporaryFrame myFrame = new TemporaryFrame("block", environment.currentFrame());
            environment.push(myFrame);

            for (NamedDeclaration decl : object.getDeclarations()) {
                environment.bind(decl, interpreter.evaluate(decl, environment));
            }
        }

        boolean canContinue = true;
        for (Statement stmt : object.getStatements()) {
            //stop the block execution if we encounter a return statement
            if (!doSwitch(stmt)) {
                canContinue = false;
                break;
            }
        }

        if (hadFrame) {
            //restore the current frame
            environment.pop();
        }
        //continue execution of the parent
        return canContinue;
    }

    @Override
    public Boolean defaultCase(EObject object) {
        KernelEvaluator ev = new KernelEvaluator(interpreter, environment);
        ev.doSwitch(object);
        return true;
    }
}
