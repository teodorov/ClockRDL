package ClockRDL.transformations;

import ClockRDL.model.expressions.BinaryExp;
import ClockRDL.model.expressions.BinaryOperator;
import ClockRDL.model.expressions.ExpressionsFactory;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.statements.AssignmentStmt;
import ClockRDL.model.statements.StatementsFactory;
import ClockRDL.model.statements.util.StatementsSwitch;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Created by ciprian on 22/10/15.
 */
public class AssignmentInliner extends StatementsSwitch<AssignmentStmt> {

    public void rewrite(AssignmentStmt object) {
        doSwitch(object);
    }

    @Override
    public AssignmentStmt caseAssignmentStmt(AssignmentStmt object) {
        BinaryOperator operator;
        switch (object.getOperator()) {
            case ANDASSIGN: operator = BinaryOperator.BAND; break;
            case DIVASSIGN: operator = BinaryOperator.BDIV; break;
            case MINUSASSIGN: operator = BinaryOperator.BMINUS; break;
            case MODASSIGN: operator = BinaryOperator.BMOD; break;
            case MULTASSIGN: operator = BinaryOperator.BMUL; break;
            case ORASSIGN: operator = BinaryOperator.BOR; break;
            case PLUSASSIGN: operator = BinaryOperator.BPLUS; break;
            default: return object;
        }

        Expression lhs = object.getLhs();
        Expression rhs = object.getRhs();

        BinaryExp newRhs = ExpressionsFactory.eINSTANCE.createBinaryExp();
        newRhs.setLhs(EcoreUtil.copy(lhs));
        newRhs.setOperator(operator);
        newRhs.setRhs(rhs);

        object.setRhs(newRhs);

        return object;
    }
}
