package ClockRDL.rdl2st80;

import ClockRDL.model.declarations.*;
import ClockRDL.model.declarations.util.DeclarationsSwitch;
import ClockRDL.model.expressions.*;
import ClockRDL.model.expressions.literals.*;
import ClockRDL.model.expressions.literals.util.LiteralsSwitch;
import ClockRDL.model.expressions.util.ExpressionsSwitch;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.NamedDeclaration;
import ClockRDL.model.kernel.Statement;
import ClockRDL.model.statements.*;
import ClockRDL.model.statements.util.StatementsSwitch;
import javafx.scene.input.DataFormat;
import org.eclipse.emf.ecore.EObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by ciprian on 25/10/15.
 */
public class RDL2Smalltalk {

    void convert(RepositoryDecl repositoryDecl) {
        declarationTransformer.doSwitch(repositoryDecl);
    }

    LiteralsSwitch literalTransformer = new LiteralsSwitch<String>() {
        @Override
        public String caseBooleanLiteral(BooleanLiteral object) {
            return object.isValue() ? "true" : "false";
        }

        @Override
        public String caseIntegerLiteral(IntegerLiteral object) {
            return Integer.toString(object.getValue());
        }

        @Override
        public String caseArrayLiteral(ArrayLiteral object) {
            String str = "(RDLArray withAll: {";

            boolean isFirst = true;
            for (Expression exp : object.getValue()) {
                if (!isFirst) str += ". ";
                str += doSwitch(exp);
                isFirst = false;
            }

            str += "})";
            return str;
        }

        @Override
        public String caseQueueLiteral(QueueLiteral object) {
            String str = "(RDLQueue withAll: {";

            boolean isFirst = true;
            for (Expression exp : object.getValue()) {
                if (!isFirst) str += ". ";
                str += doSwitch(exp);
                isFirst = false;
            }

            str += "})";
            return str;
        }

        @Override
        public String caseFieldLiteral(FieldLiteral object) {
            return "#" + object.getName() + "->" + doSwitch(object.getValue());
        }

        @Override
        public String caseRecordLiteral(RecordLiteral object) {
            String str = "{";

            boolean isFirst = true;
            for (FieldLiteral fieldLiteral : object.getValue()) {
                if (!isFirst) str += ". ";
                str += doSwitch(fieldLiteral);
                isFirst = false;
            }

            str += "} asDictionary";
            return str;
        }

        @Override
        public String caseClockLiteral(ClockLiteral object) {
            return "Clock named: #"+ object.getName();
        }

        @Override
        public String caseExpression(Expression object) {
            return expressionTransformer.doSwitch(object);
        }
    };

    class ExpressionTransformer extends ExpressionsSwitch<String> {
        @Override
        public String caseParenExp(ParenExp object) {
            return "(" + doSwitch(object.getExp()) + ")";
        }

        @Override
        public String caseIndexedExp(IndexedExp object) {
            return "(" + doSwitch(object.getPrefix()) + " at: " + doSwitch(object.getIndex()) + ")";
        }

        @Override
        public String caseSelectedExp(SelectedExp object) {
            return "(" + doSwitch(object.getPrefix()) + " at: #" + object.getSelector() + ")";
        }

        @Override
        public String caseFunctionCallExp(FunctionCallExp object) {
            String argList = "{";
            boolean isFirst = true;
            for (Expression arg : object.getArguments()) {
                if (!isFirst) argList +=  ". ";
                argList += doSwitch(arg);
                isFirst = false;
            }
            argList += "}";

            String function = doSwitch(object.getPrefix());

            if (object.getPrefix() instanceof SelectedExp) {
                return "(" + function + " value: " + argList + ")";
            }
            return "("+function + argList+ ")";
        }

        @Override
        public String caseReferenceExp(ReferenceExp object) {
            if (object.getRef() instanceof FunctionDecl) {
                return "self " + object.getRef().getName() + ": ";
            }
            return "(self " + object.getRef().getName() + ")";
        }

        @Override
        public String caseClockReference(ClockReference object) {
            return "(self " + object.getRef().getName() + ")";
        }

        @Override
        public String caseUnaryExp(UnaryExp object) {
            String exp = "(" + doSwitch(object.getOperand());
            switch (object.getOperator()) {
                case UMINUS: exp += " negated)"; break;
                case UPLUS: exp += ")"; break;
                case UNOT: exp += " not)"; break;
            }
            return exp;
        }

        @Override
        public String caseBinaryExp(BinaryExp object) {
            String lhs = doSwitch(object.getLhs());
            String rhs = doSwitch(object.getRhs());

            switch (object.getOperator()) {
                //boolean
                case BAND: return "(" + lhs + " & " + rhs + ")";
                case BOR:  return "(" + lhs + " | " + rhs + ")";
                case BNOR: return "((" + lhs + " | " + rhs + ") & ((" + lhs + " & " + rhs + ") not))";
                case BXOR: return "((" + lhs + " | " + rhs + ") not)";
                case BNAND: return "((" + lhs + " & " + rhs + ") not)";
                //equality
                case BNE: return "(" + lhs + "~=" + rhs + ")";
                case BEQ: return "(" + lhs + " = " + rhs + ")";
                //relational
                case BGT: return "(" + lhs + " > " + rhs + ")";
                case BGE: return "(" + lhs + " >= " + rhs + ")";
                case BLT: return "(" + lhs + " < " + rhs + ")";
                case BLE: return "(" + lhs + " < " + rhs + ")";
                //arithmetic
                case BDIV: return "(" + lhs + " // " + rhs + ")";
                case BMINUS: return "(" + lhs + " - " + rhs + ")";
                case BMOD: return "(" + lhs + " \\\\ " + rhs + ")";
                case BMUL: return "(" + lhs + " * " + rhs + ")";
                case BPLUS: return "(" + lhs + " + " + rhs + ")";
            }
            return "ERROR caseBinaryExp";
        }

        @Override
        public String caseConditionalExp(ConditionalExp object) {
            return "(" +doSwitch(object.getCondition())
                    + " ifTrue: [" + object.getTrueBranch()
                    + "] ifFalse: [" + doSwitch(object.getFalseBranch()) + "])";
        }

        @Override
        public String caseLiteral(Literal object) {
            return (String)literalTransformer.doSwitch(object);
        }
    };

    ExpressionTransformer expressionTransformer = new ExpressionTransformer();

    class ExpressionLValueTransformer extends ExpressionTransformer {
        @Override
        public String caseReferenceExp(ReferenceExp object) {
            return "self " + object.getRef().getName() + ": ";
        }

        @Override
        public String caseClockReference(ClockReference object) {
            return "self " + object.getRef().getName() + ": ";
        }
    }

    ExpressionLValueTransformer lValueTransformer = new ExpressionLValueTransformer();

    StatementsSwitch statementTransformer = new StatementsSwitch<String>() {

        @Override
        public String caseAssignmentStmt(AssignmentStmt object) {
            String lhs = lValueTransformer.doSwitch(object.getLhs());
            String rhs = expressionTransformer.doSwitch(object.getRhs());
            String lhsV;
            switch (object.getOperator()) {
                case ASSIGN:
                    return lhs + rhs + ".\n";
                case ANDASSIGN:
                    lhsV = expressionTransformer.doSwitch(object.getLhs());
                    return lhs + lhsV + " & " + rhs + ".\n";
                case DIVASSIGN:
                    lhsV = expressionTransformer.doSwitch(object.getLhs());
                    return lhs + lhsV + " // " + rhs + ".\n";
                case MINUSASSIGN:
                    lhsV = expressionTransformer.doSwitch(object.getLhs());
                    return lhs + lhsV + " - " + rhs + ".\n";
                case MODASSIGN:
                    lhsV = expressionTransformer.doSwitch(object.getLhs());
                    return lhs + lhsV + " \\\\ " + rhs + ".\n";
                case MULTASSIGN:
                    lhsV = expressionTransformer.doSwitch(object.getLhs());
                    return lhs + lhsV + " * " + rhs + ".\n";
                case ORASSIGN:
                    lhsV = expressionTransformer.doSwitch(object.getLhs());
                    return lhs + lhsV + " | " + rhs + ".\n";
                case PLUSASSIGN:
                    lhsV = expressionTransformer.doSwitch(object.getLhs());
                    return lhs + lhsV + " + " + rhs + ".\n";
            }
            return "ERROR caseAssignmentStmt";
        }

        @Override
        public String caseConditionalStmt(ConditionalStmt object) {
            String result = doSwitch(object.getCondition())
                    + " ifTrue: [\n" + doSwitch(object.getTrueBranch()) + "]";
            if (object.getFalseBranch() != null) {
                result += " ifFalse: [\n" + doSwitch(object.getFalseBranch()) + "].";
            }
            else {
                result += ".";
            }
            return result;
        }

        @Override
        public String caseLoopStmt(LoopStmt object) {
            return doSwitch(object.getCondition())
                    + " whileTrue: \n"
                    + doSwitch(object.getBody())
                    + ".";
        }

        @Override
        public String caseReturnStmt(ReturnStmt object) {
            return "^" + doSwitch(object.getExp());
        }

        @Override
        public String caseBlockStmt(BlockStmt object) {
            String block = "[";

            if (object.getDeclarations().size() > 0) {
                block += "|";
            }
            for (NamedDeclaration decl : object.getDeclarations()) {
                block += decl.getName() + " ";
            }
            if (object.getDeclarations().size()>0) {
                block += "|";
            }
            block += "\n";

            for (NamedDeclaration decl : object.getDeclarations()) {
                block += decl.getName() + " := " + doSwitch(decl) + ".\n";
            }

            for (Statement stmt : object.getStatements()) {
                block += doSwitch(stmt) + "\n";
            }

            return block + "] value.\n";
        }

        @Override
        public String defaultCase(EObject object) {
            return expressionTransformer.doSwitch(object);
        }
    };

    Map<NamedDeclaration, String> getters = new IdentityHashMap<>();
    Map<NamedDeclaration, String> setters = new IdentityHashMap<>();
    Map<NamedDeclaration, String> initialization = new IdentityHashMap<>();
    Map<NamedDeclaration, Boolean> visited = new IdentityHashMap<>();
    Map<AbstractRelationDecl, String> relationString = new IdentityHashMap<>();

    DeclarationsSwitch declarationTransformer = new DeclarationsSwitch<String>() {
        int clockID = 1;
        int varID = 1;
        int constID = 1;

        String getter(String name, String listName, int idx) {
            return name + "\n\t^" + listName + " at: " + idx;
        }

        String setter(String name, String listName, int idx) {
            return name + ": " + name+ "\n\t" + listName + " at: " + idx + " put: " + name;
        }

        @Override
        public String caseClockDecl(ClockDecl object) {
            if (visited.get(object) != null) return object.getName();
            getters.put(object, getter(object.getName(), "clocks", clockID));
            setters.put(object, setter(object.getName(), "clocks", clockID));
            if (object.getInitial() != null)
                initialization.put(object, "\tclocks at: " + clockID + " put: " + expressionTransformer.doSwitch(object.getInitial()) +".\n");
            clockID++;
            visited.put(object, true);
            return object.getName();
        }

        @Override
        public String caseVariableDecl(VariableDecl object) {
            if (visited.get(object) != null) return object.getName();
            getters.put(object, getter(object.getName(), "variables", varID));
            setters.put(object, setter(object.getName(), "variables", varID));
            if (object.getInitial() != null)
                initialization.put(object, "\tvariables at: " + varID + " put: " + expressionTransformer.doSwitch(object.getInitial()) +".\n");
            varID++;
            visited.put(object, true);
            return object.getName();
        }

        @Override
        public String caseConstantDecl(ConstantDecl object) {
            if (visited.get(object) != null) return object.getName();
            getters.put(object, getter(object.getName(), "constants", constID));
            setters.put(object, setter(object.getName(), "constants", constID));
            if (object.getInitial() != null)
                initialization.put(object, "\tconstants at: " + constID + " put: " + expressionTransformer.doSwitch(object.getInitial()) +".\n");
            constID++;
            visited.put(object, true);
            return object.getName();
        }

        int argID = 1;
        @Override
        public String caseArgumentDecl(ArgumentDecl object) {
            if (visited.get(object) != null) return object.getName();

            getters.put(object, "(args at:" + argID+")");
            setters.put(object, "args at: " + argID + " put: ");
            argID++;

            visited.put(object, true);
            //TODO do I really need these ?
            return object.getName();
        }

        @Override
        public String caseFunctionDecl(FunctionDecl object) {
            return object.getName() + ": args \n\t" + statementTransformer.doSwitch(object.getBody()) + "\n\n";
        }

        @Override
        public String caseTransitionDecl(TransitionDecl object) {
            String vector = " { ";
            boolean isFirst = true;
            for (ClockReference cR : object.getVector()) {
                if (!isFirst) vector += ". ";
                vector += expressionTransformer.doSwitch(cR);
                isFirst = false;
            }
            vector += " } ";

            return "^" + expressionTransformer.doSwitch(object.getGuard()) +
                    " ifTrue: [ RDLTransition "
                    + " vector: " + vector +""
                    + (object.getAction() != null ? " action: [" + statementTransformer.doSwitch(object.getAction()) + "]]" : "]");
        }

        private String buildHierarchicalClassName(AbstractRelationDecl decl) {
            String name = "";
            NamedDeclaration current = decl;
            while (current != null) {
                name = "_" + current.getName() + name;
                current = (NamedDeclaration)((LibraryItemDecl)current).getLibrary();
                if (!(current instanceof LibraryDecl)) {
                    break;
                }
            }
            return name;
        }

        @Override
        public String casePrimitiveRelationDecl(PrimitiveRelationDecl object) {
            String className = "Relation"+buildHierarchicalClassName(object);

            //TODO if one day we will have shared vars I need to handle the args
            String initializeCode = "";
            String setterString = "";
            String getterString = "";
            String functionBody = "";

            for (NamedDeclaration cR : object.getDeclarations()) {
                if (cR instanceof ClockDecl) {
                    doSwitch(cR);
                    setterString += annotateMethod(className, setters.get(cR)) + "\n";
                    getterString += annotateMethod(className, getters.get(cR)) + "\n";
                    continue;
                }
                if (cR instanceof FunctionDecl) {
                    functionBody += annotateMethod(className, doSwitch(cR));
                    continue;
                }
                doSwitch(cR);
                if (initialization.get(cR) != null) {
                    initializeCode += initialization.get(cR) + "\n";
                }
                setterString += annotateMethod(className,setters.get(cR)) + "\n";
                getterString += annotateMethod(className,getters.get(cR)) + "\n";
            }

            initializeCode = "initialize\n" +
                    "\tsuper initialize.\n" +
                    "\tclocks := Array new: " + (clockID-1) + ".\n" +
                    "\tvariables := Array new: " + (varID-1) + ".\n" +
                    "\tconstants := Array new: " + (constID-1) + ".\n" +
                    initializeCode;

            String transitionCollection = "transitions\n" +
                    "\t|transitions t|\n" +
                    "\ttransitions := OrderedCollection new.\n";
            String transitionMethods = "";
            int transitionID = 1;
            for (TransitionDecl tr : object.getTransitions()) {

                transitionMethods += annotateMethod(className,"t" + transitionID + "\n\t" + doSwitch(tr)) + "\n\n";
                transitionCollection += "\t(t := self t"+transitionID+") ifNotNil: [ transitions add: t ].\n";
                transitionID++;
            }

            transitionCollection += "\t^transitions";


            String classString = "''!\nRDLPrimitiveRelation subclass: #"+ className+"\n" +
                    "\tinstanceVariableNames: ''\n" +
                    "\tclassVariableNames: ''\n" +
                    "\tcategory: 'RDL-RelationLibrary-Test'!\n\n";

            relationString.put(object, classString + annotateMethod(className, initializeCode) + functionBody + getterString + setterString + transitionMethods + annotateMethod(className, transitionCollection));
            return className;
        }

        public String annotateMethod(String className, String body) {
            SimpleDateFormat dataFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm");
            return "!"+className+" methodsFor: 'generated' stamp: 'CiprianTeodorov " + dataFormat.format(new Date()) + "'!\n" + body + "! !\n\n";
        }

        @Override
        public String caseLibraryDecl(LibraryDecl object) {
            String result = "";
            for (LibraryItemDecl libItem : object.getLibraries()) {
                result += doSwitch(libItem);
            }
            return result;
        }

        @Override
        public String caseRepositoryDecl(RepositoryDecl object) {
            String result = "";
            for (LibraryItemDecl libItem : object.getLibraries()) {
                result += doSwitch(libItem);
            }
            return result;
        }
    };
}
