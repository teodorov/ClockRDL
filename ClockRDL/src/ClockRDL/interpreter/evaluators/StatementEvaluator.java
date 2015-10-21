package ClockRDL.interpreter.evaluators;

import ClockRDL.interpreter.Interpreter;
import ClockRDL.interpreter.Value;
import ClockRDL.interpreter.values.BooleanValue;
import ClockRDL.interpreter.values.LValue;
import ClockRDL.model.kernel.Declaration;
import ClockRDL.model.kernel.Statement;
import ClockRDL.model.statements.*;
import ClockRDL.model.statements.util.StatementsSwitch;
import ClockRDL.transformations.AssignmentInliner;

/**
 * Created by ciprian on 21/10/15.
 */
public class StatementEvaluator extends StatementsSwitch<Boolean> {
    Interpreter interpreter;

    public StatementEvaluator(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    AssignmentInliner assignmentInliner = new AssignmentInliner();
    @Override
    public Boolean caseAssignmentStmt(AssignmentStmt object) {
        //TODO
        assignmentInliner.rewrite(object);

        Value rhs = interpreter.evaluate(object.getRhs());
        LValue lhs = interpreter.lvalue(object.getLhs());

        return true;
    }

    @Override
    public Boolean caseConditionalStmt(ConditionalStmt object) {
        BooleanValue condition = interpreter.evaluate(object.getCondition(), BooleanValue.class);

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
        BooleanValue condition = interpreter.evaluate(object.getCondition(), BooleanValue.class);

        if (condition.data == true) {
            doSwitch(object.getBody());
        }
        return true;
    }

    @Override
    public Boolean caseReturnStmt(ReturnStmt object) {
        //TODO
        return super.caseReturnStmt(object);
    }

    @Override
    public Boolean caseBlockStmt(BlockStmt object) {
        //TODO
        //This pushes a new Frame with its local variables
        //the evaluates the statements using the environment extended with this new frame

        for (Declaration decl : object.getDeclarations()) {
            //TODO: here I should bind the declarations in a new frame
        }

        for (Statement stmt : object.getStatements()) {
            doSwitch(stmt);
        }

        //TODO: here I should dispose of the frame of this block

        return true;
    }
}
