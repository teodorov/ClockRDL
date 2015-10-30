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
import org.eclipse.emf.ecore.EObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by ciprian on 25/10/15.
 */
public class RDL2Smalltalk {

    public String convert(RepositoryDecl repositoryDecl) {
        return (String)declarationTransformer.doSwitch(repositoryDecl);
    }
    public String convert(RelationInstanceDecl instanceDecl) {
        String res;
        String instance = (String)declarationTransformer.doSwitch(instanceDecl);
        res = result + instance;
        return res;
    }
    public String convert(SystemDecl sys) {
        String res;
        String instance = (String)declarationTransformer.doSwitch(sys.getRoot());
        res = result + instance;
        return res;
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
            return "(Clock named: #"+ object.getName() + ")";
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
                case BAND: return "(" + lhs + " and: [ " + rhs + " ])"; //shortcut for and, does not evaluate RHS
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
                case BLE: return "(" + lhs + " <= " + rhs + ")";
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
    Map<NamedDeclaration, Boolean> visited = new IdentityHashMap<>();
    Map<AbstractRelationDecl, String> relationString = new IdentityHashMap<>();
    public String result = "";

    DeclarationsSwitch declarationTransformer = new DeclarationsSwitch<String>() {
        int clockID = 1;
        int varID = 1;
        int constID = 1;

        String getter(String name, String initial, String listName, int idx) {
            String elementAccess = listName + " at: " + idx;
            String result = name + "\n\t";
            if (initial != null) {
                result += "<RDLInitialization>\n\t";
                result += "(" + elementAccess + ") ifNil: [" + elementAccess + " put: " + initial + "].\n\t";
            }
            result += "^"+elementAccess;

            return result;
        }

        String setter(String name, String listName, int idx) {
            return name + ": " + name+ "\n\t" + listName + " at: " + idx + " put: " + name;
        }

        @Override
        public String caseClockDecl(ClockDecl object) {
            if (visited.get(object) != null) return object.getName();

            String initial = object.getInitial() != null ? ( "(" + expressionTransformer.doSwitch(object.getInitial()) + " internal: "+(object.getInitial().isIsInternal()?"true" : "false")+")" ) : null;
            getters.put(object, getter(object.getName(), initial, "clocks", clockID));
            setters.put(object, setter(object.getName(), "clocks", clockID));
            clockID++;
            visited.put(object, true);
            return object.getName();
        }

        @Override
        public String caseVariableDecl(VariableDecl object) {
            if (visited.get(object) != null) return object.getName();
            String initial = object.getInitial() != null ? expressionTransformer.doSwitch(object.getInitial()) : null;
            getters.put(object, getter(object.getName(), initial, "variables", varID));
            setters.put(object, setter(object.getName(), "variables", varID));
            varID++;
            visited.put(object, true);
            return object.getName();
        }

        @Override
        public String caseConstantDecl(ConstantDecl object) {
            if (visited.get(object) != null) return object.getName();
            String initial = object.getInitial() != null ? expressionTransformer.doSwitch(object.getInitial()) : null;
            getters.put(object, getter(object.getName(), initial, "constants", constID));
            setters.put(object, setter(object.getName(), "constants", constID));
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
            if (visited.get(object) != null) return className;

            varID = 1;
            constID = 1;
            clockID = 1;

            //TODO if one day we will have shared vars I need to handle the args
            String setterString = "";
            String getterString = "";
            String functionBody = "";
            String selectors = "";

            for (NamedDeclaration cR : object.getDeclarations()) {
                if (cR instanceof ClockDecl) {
                    doSwitch(cR);
                    setterString += annotateMethod(className, setters.get(cR)) + "\n";
                    getterString += annotateMethod(className, getters.get(cR)) + "\n";
                    selectors += "#" + cR.getName();
                    continue;
                }
                if (cR instanceof FunctionDecl) {
                    functionBody += annotateMethod(className, doSwitch(cR));
                    continue;
                }
                doSwitch(cR);

                setterString += annotateMethod(className,setters.get(cR)) + "\n";
                getterString += annotateMethod(className,getters.get(cR)) + "\n";
                selectors += "#" + cR.getName();
            }

            String initializeCode = "initialize\n" +
                    "\tsuper initialize.\n" +
                    "\tclocks := Array new: " + (clockID-1) + ".\n" +
                    "\tvariables := Array new: " + (varID-1) + ".\n" +
                    "\tconstants := Array new: " + (constID-1) + ".\n";

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
                    "\tcategory: '"+packageName+"'!\n\n";

            relationString.put(object, classString + annotateMethod(className, initializeCode) + functionBody + getterString + setterString + transitionMethods + annotateMethod(className, transitionCollection));
            result += relationString.get(object) + "\n\n";
            visited.put(object, true);
            return className;
        }

        @Override
        public String caseCompositeRelationDecl(CompositeRelationDecl object) {
            String className = "Relation"+buildHierarchicalClassName(object);
            if (visited.get(object) != null) return className;
            //TODO if one day we will have shared vars I need to handle the args

            varID = 1;
            constID = 1;
            clockID = 1;

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
                setterString += annotateMethod(className,setters.get(cR)) + "\n";
                getterString += annotateMethod(className,getters.get(cR)) + "\n";
            }

            for (ClockDecl cD : object.getInternalClocks()) {
                doSwitch(cD);
                setterString += annotateMethod(className, setters.get(cD)) + "\n";
                getterString += annotateMethod(className, getters.get(cD)) + "\n";
            }

            String initializeCode = "initialize\n" +
                    "\tsuper initialize.\n" +
                    "\tclocks := Array new: " + (clockID-1) + ".\n" +
                    "\tvariables := Array new: " + (varID-1) + ".\n" +
                    "\tconstants := Array new: " + (constID-1) + ".\n";

            String instanceCode = "instances\n\t|instances|\n\tinstances:=OrderedCollection new.\n\t";
            for (RelationInstanceDecl instanceDecl : object.getInstances()) {
                instanceCode += "instances add: (" + doSwitch(instanceDecl) + ").\n\t";
            }
            instanceCode += "^instances\n";

            String classString = "''!\nRDLCompositeRelation subclass: #"+ className+"\n" +
                    "\tinstanceVariableNames: ''\n" +
                    "\tclassVariableNames: ''\n" +
                    "\tcategory: '"+packageName+"'!\n\n";

            relationString.put(object, classString + annotateMethod(className, initializeCode) + functionBody + getterString + setterString + annotateMethod(className, instanceCode));
            result += relationString.get(object) + "\n\n";
            visited.put(object, true);
            return className;
        }

        int instanceID = 1;
        @Override
        public String caseRelationInstanceDecl(RelationInstanceDecl object) {
            String name = object.getName() != null ? object.getName() : "i"+ instanceID;
            String relationInstanceString = "(" + doSwitch(object.getRelation()) + " instanceNamed: #i"+(instanceID ++ )+")\n\t\t";
            boolean isFirst = true;
            for (Map.Entry<String, Expression> entry : object.getArgumentMap()) {
                if (!isFirst) {
                    relationInstanceString += ";\n\t\t";
                }
                relationInstanceString += entry.getKey() + ": " + expressionTransformer.doSwitch(entry.getValue());
                isFirst = false;
            }
            return relationInstanceString;
        }

        public String annotateMethod(String className, String body) {
            SimpleDateFormat dataFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm");
            return "!"+className+" methodsFor: 'generated' stamp: 'CiprianTeodorov " + dataFormat.format(new Date()) + "'!\n" + body + "! !\n\n";
        }

        @Override
        public String caseLibraryDecl(LibraryDecl object) {
            for (LibraryItemDecl libItem : object.getLibraries()) {
                doSwitch(libItem);
            }
            return result;
        }

        @Override
        public String caseRepositoryDecl(RepositoryDecl object) {
            for (LibraryItemDecl libItem : object.getLibraries()) {
                doSwitch(libItem);
            }
            return result;
        }

        @Override
        public String caseSystemDecl(SystemDecl object) {
            if (object.getRoot()!=null) {
                String instanceString = doSwitch(object.getRoot());

                SimpleDateFormat dataFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm");
                String rootMethod = "''!\nObject subclass: #RDLRelationInstance\n" +
                        "\tinstanceVariableNames: ''\n" +
                        "\tclassVariableNames: ''\n" +
                        "\tcategory: '"+packageName+"'!\n\n!\n";

                rootMethod +="RDLRelationInstance class\n" +
                        "\tinstanceVariableNames: ''!\n" +
                        "\n" +
                        "!RDLRelationInstance class methodsFor: 'generated' stamp: 'CiprianTeodorov " + dataFormat.format(new Date()) + "'!\n";
                rootMethod += "root\n\t|instance|\n\tinstance:=" + instanceString + ".\n\t";
                rootMethod += "^ (ClockSystem named: #rdl) fromRDL: instance! !";


                return result + "\n\n\n" + rootMethod;
            }
            return result;
        }
    };

    String packageName = "RDL-RelationLibrary-Test";
}
