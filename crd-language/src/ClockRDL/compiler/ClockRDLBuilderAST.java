package ClockRDL.compiler;


import ClockRDL.grammar.ClockRDLBaseListener;
import ClockRDL.grammar.ClockRDLLexer;
import ClockRDL.grammar.ClockRDLParser;
import ClockRDL.model.declarations.*;
import ClockRDL.model.expressions.*;
import ClockRDL.model.expressions.literals.*;
import ClockRDL.model.kernel.Declaration;
import ClockRDL.model.kernel.Expression;
import ClockRDL.model.kernel.NamedDeclaration;
import ClockRDL.model.kernel.Statement;
import ClockRDL.model.statements.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


public class ClockRDLBuilderAST extends ClockRDLBaseListener {
    public ParseTreeProperty<Object> values = new ParseTreeProperty<>();

    Scope<NamedDeclaration> currentScope;
    List<URI> libraryPaths;

    public ClockRDLBuilderAST(Scope<NamedDeclaration> globalScope, List<URI> libraryPaths) {
        this.currentScope = globalScope;
        this.libraryPaths = libraryPaths;
    }

    //this is needed only for binding relation instances to their library definitions
    Map<Declaration, Scope> decl2scope = new IdentityHashMap<>();
    public void saveScope(Declaration node, Scope scope) {
        decl2scope.put(node, scope);
    }

    //helper methods
    public void setValue(ParseTree node, Object value) {
        values.put(node, value);
    }

    public Object getValue(ParseTree node) {
        return values.get(node);
    }

    public <T extends Object> T getValue(ParseTree node, Class<T> type) {
        if (node == null) return null;
        Object e = values.get(node);
        if (type.isAssignableFrom(e.getClass())) {
            return type.cast(e);
        }
        return null;
    }

    LiteralsFactory litFact = LiteralsFactory.eINSTANCE;

    @Override
    public void exitIntegerLiteral(ClockRDLParser.IntegerLiteralContext ctx) {
        String numberString = ctx.NUMBER().getText();
        int number = Integer.parseInt(numberString);
        IntegerLiteral literal = litFact.createIntegerLiteral();
        literal.setValue(number);
        setValue(ctx, literal);
    }

    @Override
    public void exitBooleanLiteral(ClockRDLParser.BooleanLiteralContext ctx) {
        boolean value;
        switch (ctx.start.getType()) {
            case ClockRDLParser.TRUE:
                value = true;
                break;
            case ClockRDLParser.FALSE:
                value = false;
                break;
            default:
                throw new RuntimeException("unexpected boolean literal (line: "+ ctx.getStart().getLine() +")\n");
        }
        BooleanLiteral literal = litFact.createBooleanLiteral();
        literal.setValue(value);
        setValue(ctx, literal);
    }

    @Override
    public void exitArrayLiteral(ClockRDLParser.ArrayLiteralContext ctx) {
        ArrayLiteral literal = litFact.createArrayLiteral();
        List<Expression> items = literal.getValue();
        for (ClockRDLParser.ExpressionContext expCtx : ctx.expression()) {
            Expression exp = getValue(expCtx, Expression.class);
            items.add(exp);
        }
        setValue(ctx, literal);
    }

    @Override
    public void exitQueueLiteral(ClockRDLParser.QueueLiteralContext ctx) {
        QueueLiteral literal = litFact.createQueueLiteral();
        List<Expression> items = literal.getValue();
        for (ClockRDLParser.ExpressionContext expCtx : ctx.expression()) {
            Expression exp = getValue(expCtx, Expression.class);
            items.add(exp);
        }
        setValue(ctx, literal);
    }

    @Override
    public void exitRecordLiteral(ClockRDLParser.RecordLiteralContext ctx) {
        RecordLiteral literal = litFact.createRecordLiteral();
        List<FieldLiteral> items = literal.getValue();
        for (ClockRDLParser.FieldLiteralContext flCtx : ctx.fieldLiteral()) {
            FieldLiteral fieldLiteral = getValue(flCtx, FieldLiteral.class);
            items.add(fieldLiteral);
        }
        setValue(ctx, literal);
    }

    @Override
    public void exitFieldLiteral(ClockRDLParser.FieldLiteralContext ctx) {
        FieldLiteral fieldLiteral = litFact.createFieldLiteral();
        String identifier = ctx.IDENTIFIER().getText();
        Expression exp = getValue(ctx.expression(), Expression.class);

        fieldLiteral.setName(identifier);
        fieldLiteral.setValue(exp);

        setValue(ctx, fieldLiteral);
    }

    @Override
    public void exitClockLiteral(ClockRDLParser.ClockLiteralContext ctx) {
        ClockLiteral clockLiteral = litFact.createClockLiteral();
        String identifier = ctx.IDENTIFIER().getText();
        clockLiteral.setName(identifier);
        if (ctx.INTERNAL() != null) clockLiteral.setIsInternal(true);
        else clockLiteral.setIsInternal(false);
        setValue(ctx, clockLiteral);
    }

    //Expressions
    ExpressionsFactory expFact = ExpressionsFactory.eINSTANCE;

    @Override
    public void exitParenExp(ClockRDLParser.ParenExpContext ctx) {
        ParenExp parenExp = expFact.createParenExp();
        Expression exp = getValue(ctx.expression(), Expression.class);
        parenExp.setExp(exp);
        setValue(ctx, parenExp);
    }

    @Override
    public void exitLiteralExp(ClockRDLParser.LiteralExpContext ctx) {
        setValue(ctx, getValue(ctx.literal()));
    }

    @Override
    public void exitLiteral(ClockRDLParser.LiteralContext ctx) {
        setValue(ctx, getValue(ctx.getChild(0)));
    }

    @Override
    public void exitIndexedExp(ClockRDLParser.IndexedExpContext ctx) {
        IndexedExp exp = expFact.createIndexedExp();
        Expression prefix = getValue(ctx.expression(0), Expression.class);
        Expression index = getValue(ctx.expression(1), Expression.class);

        exp.setPrefix(prefix);
        exp.setIndex(index);
        setValue(ctx, exp);
    }

    @Override
    public void exitSelectedExp(ClockRDLParser.SelectedExpContext ctx) {
        SelectedExp exp = expFact.createSelectedExp();
        Expression prefix = getValue(ctx.expression(), Expression.class);
        String selector = ctx.IDENTIFIER().getText();

        exp.setPrefix(prefix);
        exp.setSelector(selector);

        setValue(ctx, exp);
    }

    @Override
    public void exitFunctionCallExp(ClockRDLParser.FunctionCallExpContext ctx) {
        FunctionCallExp exp = expFact.createFunctionCallExp();
        Expression function = getValue(ctx.expression(0), Expression.class);

        exp.setPrefix(function);
        List<Expression> arguments = exp.getArguments();
        for (int i = 1; i < ctx.expression().size(); i++) {
            Expression arg = getValue(ctx.expression(i), Expression.class);
            arguments.add(arg);
        }


        setValue(ctx, exp);
    }

    @Override
    public void exitReferenceExp(ClockRDLParser.ReferenceExpContext ctx) {
        ReferenceExp exp = expFact.createReferenceExp();
        String name = ctx.reference().IDENTIFIER().getText();

        NamedDeclaration referenced = currentScope.resolve(name);
        if (referenced == null) {
            throw new RuntimeException("The identifier " + name + " is not defined in the current scope (line: " + ctx.getStart().getLine() + ")");
        }
        exp.setRef(referenced);

        setValue(ctx, exp);
    }

    @Override
    public void exitUnaryExp(ClockRDLParser.UnaryExpContext ctx) {
        UnaryExp exp = expFact.createUnaryExp();

        Expression operand = getValue(ctx.expression(), Expression.class);
        exp.setOperand(operand);

        switch (ctx.operator.getType()) {
            case ClockRDLParser.NOT:
                exp.setOperator(UnaryOperator.UNOT);
                break;
            case ClockRDLParser.PLUS:
                exp.setOperator(UnaryOperator.UPLUS);
                break;
            case ClockRDLParser.MINUS:
                exp.setOperator(UnaryOperator.UMINUS);
                break;
            default:
                throw new RuntimeException("unexpected unary operator: " + ctx.operator.getText() + " (line: "+ ctx.getStart().getLine() +")\n");
        }

        setValue(ctx, exp);
    }

    @Override
    public void exitBinaryExp(ClockRDLParser.BinaryExpContext ctx) {
        BinaryExp exp = expFact.createBinaryExp();
        Expression lhs = getValue(ctx.expression(0), Expression.class);
        exp.setLhs(lhs);

        Expression rhs = getValue(ctx.expression(1), Expression.class);
        exp.setRhs(rhs);

        switch (ctx.operator.getType()) {
            case ClockRDLParser.MULT:
                exp.setOperator(BinaryOperator.BMUL);
                break;
            case ClockRDLParser.DIV:
                exp.setOperator(BinaryOperator.BDIV);
                break;
            case ClockRDLParser.MOD:
                exp.setOperator(BinaryOperator.BMOD);
                break;
            case ClockRDLParser.PLUS:
                exp.setOperator(BinaryOperator.BPLUS);
                break;
            case ClockRDLParser.MINUS:
                exp.setOperator(BinaryOperator.BMINUS);
                break;
            case ClockRDLParser.LT:
                exp.setOperator(BinaryOperator.BLT);
                break;
            case ClockRDLParser.LTE:
                exp.setOperator(BinaryOperator.BLE);
                break;
            case ClockRDLParser.GT:
                exp.setOperator(BinaryOperator.BGT);
                break;
            case ClockRDLParser.GTE:
                exp.setOperator(BinaryOperator.BGE);
                break;
            case ClockRDLParser.EQ:
                exp.setOperator(BinaryOperator.BEQ);
                break;
            case ClockRDLParser.NEQ:
                exp.setOperator(BinaryOperator.BNE);
                break;
            case ClockRDLParser.OR:
                exp.setOperator(BinaryOperator.BOR);
                break;
            case ClockRDLParser.AND:
                exp.setOperator(BinaryOperator.BAND);
                break;
            case ClockRDLParser.NOR:
                exp.setOperator(BinaryOperator.BNOR);
                break;
            case ClockRDLParser.NAND:
                exp.setOperator(BinaryOperator.BNAND);
                break;
            case ClockRDLParser.XOR:
                exp.setOperator(BinaryOperator.BXOR);
                break;
            default:
                throw new RuntimeException("unexpected binary operator: " + ctx.operator.getText() + " (line: "+ ctx.getStart().getLine() +")\n");
        }
        setValue(ctx, exp);
    }

    @Override
    public void exitConditionalExp(ClockRDLParser.ConditionalExpContext ctx) {
        ConditionalExp exp = expFact.createConditionalExp();
        Expression cond = getValue(ctx.expression(0), Expression.class);
        exp.setCondition(cond);
        Expression trueE = getValue(ctx.expression(1), Expression.class);
        exp.setTrueBranch(trueE);
        Expression falseE = getValue(ctx.expression(2), Expression.class);
        exp.setFalseBranch(falseE);

        setValue(ctx, exp);
    }

    //statements
    StatementsFactory stmtFact = StatementsFactory.eINSTANCE;


    @Override
    public void exitStatement(ClockRDLParser.StatementContext ctx) {
        setValue(ctx, getValue(ctx.getChild(0)));
    }

    @Override
    public void exitAssignmentStmt(ClockRDLParser.AssignmentStmtContext ctx) {
        AssignmentStmt stmt = stmtFact.createAssignmentStmt();

        Expression lhs = getValue(ctx.expression(0), Expression.class);
        stmt.setLhs(lhs);
        Expression rhs = getValue(ctx.expression(1), Expression.class);
        stmt.setRhs(rhs);

        switch (ctx.operator.getType()) {
            case ClockRDLParser.ASSIGN:
                stmt.setOperator(AssignmentOperator.ASSIGN);
                break;
            case ClockRDLParser.MINUSASSIGN:
                stmt.setOperator(AssignmentOperator.MINUSASSIGN);
                break;
            case ClockRDLParser.PLUSASSIGN:
                stmt.setOperator(AssignmentOperator.PLUSASSIGN);
                break;
            case ClockRDLParser.MULTASSIGN:
                stmt.setOperator(AssignmentOperator.MULTASSIGN);
                break;
            case ClockRDLParser.MODASSIGN:
                stmt.setOperator(AssignmentOperator.MODASSIGN);
                break;
            case ClockRDLParser.DIVASSIGN:
                stmt.setOperator(AssignmentOperator.DIVASSIGN);
                break;
            case ClockRDLParser.ORASSIGN:
                stmt.setOperator(AssignmentOperator.ORASSIGN);
                break;
            case ClockRDLParser.ANDASSIGN:
                stmt.setOperator(AssignmentOperator.ANDASSIGN);
                break;
            default:
                throw new RuntimeException("unexpected assignment operator: " + ctx.operator.getText() + " (line: "+ ctx.getStart().getLine() +")\n");
        }
        setValue(ctx, stmt);
    }

    @Override
    public void exitConditionalStmt(ClockRDLParser.ConditionalStmtContext ctx) {
        ConditionalStmt stmt = stmtFact.createConditionalStmt();
        Expression condition = getValue(ctx.expression(), Expression.class);
        stmt.setCondition(condition);
        BlockStmt trueB = getValue(ctx.blockStmt(0), BlockStmt.class);
        stmt.setTrueBranch(trueB);
        BlockStmt falseB = getValue(ctx.blockStmt(1), BlockStmt.class);
        stmt.setFalseBranch(falseB);

        setValue(ctx, stmt);
    }

    @Override
    public void exitLoopStmt(ClockRDLParser.LoopStmtContext ctx) {
        LoopStmt stmt = stmtFact.createLoopStmt();

        Expression condition = getValue(ctx.expression(), Expression.class);
        stmt.setCondition(condition);
        BlockStmt body = getValue(ctx.blockStmt(), BlockStmt.class);
        stmt.setBody(body);

        setValue(ctx, stmt);
    }

    @Override
    public void exitReturnStmt(ClockRDLParser.ReturnStmtContext ctx) {
        ReturnStmt stmt = stmtFact.createReturnStmt();

        Expression value = getValue(ctx.expression(), Expression.class);
        stmt.setExp(value);

        setValue(ctx, stmt);
    }

    @Override
    public void enterBlockStmt(ClockRDLParser.BlockStmtContext ctx) {
        //create a local scope and set it as current for the children
        Scope<NamedDeclaration> scope = new Scope<>("local", currentScope);
        currentScope = scope;
    }

    @Override
    public void exitBlockStmt(ClockRDLParser.BlockStmtContext ctx) {
        BlockStmt stmt = stmtFact.createBlockStmt();

        //reset the current scope
        currentScope = currentScope.getEnclosingScope();

        List<NamedDeclaration> items = stmt.getDeclarations();
        for (ClockRDLParser.BlockDeclContext itemCtx : ctx.blockDecl()) {
            List<NamedDeclaration> bItems = getValue(itemCtx, List.class);
            items.addAll(bItems);
        }

        List<Statement> stmts = stmt.getStatements();
        for (ClockRDLParser.StatementContext stmtItem : ctx.statement()) {
            Statement sItem = getValue(stmtItem, Statement.class);
            stmts.add(sItem);
        }

        setValue(ctx, stmt);
    }

    @Override
    public void exitBlockDecl(ClockRDLParser.BlockDeclContext ctx) {
        setValue(ctx, getValue(ctx.getChild(0)));
    }

    //Declarations
    DeclarationsFactory declFact = DeclarationsFactory.eINSTANCE;

    @Override
    public void exitArgumentDecl(ClockRDLParser.ArgumentDeclContext ctx) {
        List<ArgumentDecl> arguments = new ArrayList<>(ctx.IDENTIFIER().size());
        for (TerminalNode id : ctx.IDENTIFIER()) {
            ArgumentDecl argDecl = declFact.createArgumentDecl();

            String name = id.getText();
            //add this argument to the current scope
            currentScope.define(name, argDecl);

            argDecl.setName(name);

            arguments.add(argDecl);
        }

        setValue(ctx, arguments);
    }

    @Override
    public void exitClockDecl(ClockRDLParser.ClockDeclContext ctx) {

        List<ClockDecl> clocks = new ArrayList<>(ctx.initializedIdentifier().size());
        for (ClockRDLParser.InitializedIdentifierContext id : ctx.initializedIdentifier()) {
            ClockDecl clockDecl = declFact.createClockDecl();

            String name = id.IDENTIFIER().getText();
            //add this clock to the current scope
            currentScope.define(name, clockDecl);

            clockDecl.setName(name);
            if (id.expression() != null) clockDecl.setInitial(getValue(id.expression(), ClockLiteral.class));

            clocks.add(clockDecl);
        }

        setValue(ctx, clocks);
    }

    @Override
    public void exitVariableDecl(ClockRDLParser.VariableDeclContext ctx) {
        List<VariableDecl> vars = new ArrayList<>(ctx.initializedIdentifier().size());
        for (ClockRDLParser.InitializedIdentifierContext id : ctx.initializedIdentifier()) {
            VariableDecl varDecl = declFact.createVariableDecl();

            String name = id.IDENTIFIER().getText();
            //add this variable to the current scope
            currentScope.define(name, varDecl);

            varDecl.setName(name);
            if (id.expression() != null) varDecl.setInitial(getValue(id.expression(), Expression.class));

            vars.add(varDecl);
        }
        setValue(ctx, vars);
    }

    @Override
    public void exitConstantDecl(ClockRDLParser.ConstantDeclContext ctx) {
        List<VariableDecl> vars = new ArrayList<>(ctx.initializedIdentifier().size());
        for (ClockRDLParser.InitializedIdentifierContext id : ctx.initializedIdentifier()) {
            ConstantDecl constDecl = declFact.createConstantDecl();

            String name = id.IDENTIFIER().getText();
            //add this constant to the current scope
            currentScope.define(name, constDecl);

            constDecl.setName(name);
            constDecl.setInitial(getValue(id.expression(), Expression.class));

            vars.add(constDecl);
        }
        setValue(ctx, vars);
    }

    @Override
    public void enterFunctionDecl(ClockRDLParser.FunctionDeclContext ctx) {
        //create a function scope and set it as current for the children
        Scope<NamedDeclaration> scope = new Scope<>("fun " + ctx.IDENTIFIER().getText(), currentScope);
        currentScope = scope;
    }

    @Override
    public void exitFunctionDecl(ClockRDLParser.FunctionDeclContext ctx) {
        FunctionDecl decl = declFact.createFunctionDecl();

        String name = ctx.IDENTIFIER().getText();

        //add this function to the enclosing scope
        currentScope = currentScope.getEnclosingScope();
        currentScope.define(name, decl);

        decl.setName(name);

        List<ArgumentDecl> arguments = getValue(ctx.argumentDecl(), List.class);
        if (arguments != null) decl.getArguments().addAll(arguments);

        BlockStmt body = getValue(ctx.blockStmt(), BlockStmt.class);
        decl.setBody(body);

        setValue(ctx, decl);
    }

    public BooleanLiteral trueGuard() {
        BooleanLiteral trueGuard = litFact.createBooleanLiteral();
        trueGuard.setValue(true);
        return trueGuard;
    }

    @Override
    public void exitTransitionDecl(ClockRDLParser.TransitionDeclContext ctx) {
        TransitionDecl decl = declFact.createTransitionDecl();

        Expression guard = ctx.guard() != null ? getValue(ctx.guard().expression(), Expression.class) : trueGuard();
        decl.setGuard(guard);

        List<ClockReference> clocks = decl.getVector();
        for (TerminalNode id : ctx.vector().IDENTIFIER()) {
            ClockReference cRef = expFact.createClockReference();

            NamedDeclaration referenced = currentScope.resolve(id.getText());
            if (!(referenced instanceof ClockDecl)) {
                throw new RuntimeException("invalid clock name: " + id.getText() + "(line: "+ ctx.getStart().getLine() +")\n");
            }
            cRef.setRef(referenced);

            clocks.add(cRef);
        }

        Statement stmt = ctx.action() != null ? getValue(ctx.action().statement(), Statement.class) : null;
        decl.setAction(stmt);

        setValue(ctx, decl);
    }

    @Override
    public void exitFormalToActual(ClockRDLParser.FormalToActualContext ctx) {
        String formalName = ctx.IDENTIFIER() != null ? ctx.IDENTIFIER().getText() : null;
        Expression actualExp = getValue(ctx.expression(), Expression.class);

        AbstractMap.SimpleEntry<String, Expression> formalToActual = new AbstractMap.SimpleEntry<>(formalName, actualExp);

        setValue(ctx, formalToActual);
    }

    @Override
    public void exitQualifiedName(ClockRDLParser.QualifiedNameContext ctx) {
        List<String> qualifiedPath = new ArrayList<>(ctx.IDENTIFIER().size());
        for (TerminalNode id : ctx.IDENTIFIER()) {
            qualifiedPath.add(id.getText());
        }
        setValue(ctx, qualifiedPath);
    }

    public <T extends Declaration> T lookup(List<String> qualifiedName, Class<T> type) {
        Scope theScope = currentScope;

        Declaration decl = null;

        for (String selector : qualifiedName) {
            decl = (Declaration)theScope.resolve(selector);
            theScope = decl2scope.get(decl);
        }

        if (type.isAssignableFrom(decl.getClass())) {
            return type.cast(decl);
        }

        if (decl==null)
            throw new RuntimeException("Cannot resolve qualified name: " + qualifiedName);

        throw new RuntimeException("Cannot resolve qualified name: " + qualifiedName + " (expected: " + type.getSimpleName() + " found: " + decl.getClass().getSimpleName() + ")");
    }

    @Override
    public void exitInstanceDecl(ClockRDLParser.InstanceDeclContext ctx) {
        RelationInstanceDecl decl = declFact.createRelationInstanceDecl();

        if (ctx.IDENTIFIER() != null) {
            String instanceName = ctx.IDENTIFIER().getText();
            decl.setName(instanceName);
        }

        List<String> qualifiedName = getValue(ctx.qualifiedName(), List.class);
        decl.getQualifiedName().addAll(qualifiedName);
        decl.setRelation(lookup(qualifiedName, AbstractRelationDecl.class));

        Map<String, Expression> formalToActualMap = decl.getArgumentMap().map();
        int id = 0;
        for (ClockRDLParser.FormalToActualContext entryCtx : ctx.formalToActual()) {
            Map.Entry<String, Expression> entry = getValue(entryCtx, Map.Entry.class);
            String formalName = entry.getKey();
            Expression actualExp = entry.getValue();

            formalToActualMap.put(formalName == null ? Integer.toString(id++) : formalName, actualExp);
        }
        setValue(ctx, decl);
    }

    @Override
    public void enterRelationDecl(ClockRDLParser.RelationDeclContext ctx) {
        //create a relation scope and set it as current for the children
        Scope<NamedDeclaration> relationScope = new Scope<>("rel " + ctx.IDENTIFIER().getText(), currentScope);
        currentScope = relationScope;
    }

    @Override
    public void exitPrimitiveRelationBody(ClockRDLParser.PrimitiveRelationBodyContext ctx) {
        List<TransitionDecl> transitions = new ArrayList(ctx.transitionDecl().size());
        for (ClockRDLParser.TransitionDeclContext transitionCtx : ctx.transitionDecl()) {
            transitions.add(getValue(transitionCtx, TransitionDecl.class));
        }
        setValue(ctx, transitions);
    }

    @Override
    public void enterCompositeRelationBody(ClockRDLParser.CompositeRelationBodyContext ctx) {
        //create a scope for internal clocks and set it as current for the children
        Scope<NamedDeclaration> internalClocksScope = new Scope<>("internal clocks scope", currentScope);
        currentScope = internalClocksScope;

        for (ClockRDLParser.ClockLiteralContext clockLiteralContext : ctx.clockLiteral()) {
            ClockDecl clockDecl = declFact.createClockDecl();

            String name = clockLiteralContext.IDENTIFIER().getText();
            clockDecl.setName(name);
            //add this clock to the current scope
            currentScope.define(name, clockDecl);
        }
    }

    @Override
    public void exitCompositeRelationBody(ClockRDLParser.CompositeRelationBodyContext ctx) {
        List<ClockDecl> internalClocks = new ArrayList<>();
        for (ClockRDLParser.ClockLiteralContext clockLiteralCtx : ctx.clockLiteral()) {
            ClockLiteral cL = getValue(clockLiteralCtx, ClockLiteral.class);
            ClockDecl clockDecl = (ClockDecl)currentScope.resolve(cL.getName());

            clockDecl.setInitial(cL);

            internalClocks.add(clockDecl);
        }
        //reset the scope
        currentScope = currentScope.getEnclosingScope();

        List<RelationInstanceDecl> instances = new ArrayList(ctx.instanceDecl().size());
        for (ClockRDLParser.InstanceDeclContext instCtx : ctx.instanceDecl()) {
            RelationInstanceDecl instance = getValue(instCtx, RelationInstanceDecl.class);
            instances.add(instance);
        }
        Map<String, List> result = new HashMap<>();
        result.put("internalClocks", internalClocks);
        result.put("instances", instances);
        setValue(ctx, result);
    }

    @Override
    public void exitRelationDecl(ClockRDLParser.RelationDeclContext ctx) {
        boolean isPrimitive = ctx.primitiveRelationBody() != null;
        AbstractRelationDecl decl = isPrimitive ? declFact.createPrimitiveRelationDecl() : declFact.createCompositeRelationDecl();

        String name = ctx.IDENTIFIER().getText();

        //add this relation to the enclosing scope
        saveScope(decl, currentScope);
        currentScope = currentScope.getEnclosingScope();
        currentScope.define(name, decl);

        decl.setName(name);

        List<ArgumentDecl> arguments = getValue(ctx.argumentDecl(), List.class);
        if (arguments != null) {
            decl.getArguments().addAll(arguments);
            throw new RuntimeException("Relation with argument '"+name+"' not supported yet! ( variable sharing is not yet implemented )");
        }

        List<ClockDecl> clocks = getValue(ctx.clockDecl(), List.class);
        if (clocks != null) decl.getDeclarations().addAll(clocks);

        List<VariableDecl> variables = getValue(ctx.variableDecl(), List.class);
        if (variables != null) {
            decl.getDeclarations().addAll(variables);
            if (decl instanceof CompositeRelationDecl) {
                throw new RuntimeException("Composite relation '"+name+"' with variables do not make sense without variable sharing which is not supported yet!");
            }
        }

        List<ConstantDecl> constants = getValue(ctx.constantDecl(), List.class);
        if (constants != null) decl.getDeclarations().addAll(constants);

        List<NamedDeclaration> decls = decl.getDeclarations();
        for (ClockRDLParser.FunctionDeclContext fctCtx : ctx.functionDecl()) {
            FunctionDecl f = getValue(fctCtx, FunctionDecl.class);
            decls.add(f);
        }

        if (isPrimitive) {
            List<TransitionDecl> transitions = ((PrimitiveRelationDecl)decl).getTransitions();
            transitions.addAll(getValue(ctx.primitiveRelationBody(), List.class));
        }
        else {
            Map<String, List> body = getValue(ctx.compositeRelationBody(), Map.class);
            List<ClockDecl> internalClocks = body.get("internalClocks");
            if (internalClocks != null) ((CompositeRelationDecl)decl).getInternalClocks().addAll(internalClocks);
            ((CompositeRelationDecl)decl).getInstances().addAll(body.get("instances"));
        }
        setValue(ctx, decl);
    }

    LibraryDecl library;

    @Override
    public void enterLibraryDecl(ClockRDLParser.LibraryDeclContext ctx) {
        //create a library scope and set it as the current scope for the children
        Scope<NamedDeclaration> libScope = new Scope<>("lib " + ctx.IDENTIFIER().getText(), currentScope);
        currentScope = libScope;
    }

    @Override
    public void exitLibraryItem(ClockRDLParser.LibraryItemContext ctx) {
        setValue(ctx, getValue(ctx.relationDecl() == null ? ctx.libraryDecl() : ctx.relationDecl()));
    }

    @Override
    public void exitLibraryDecl(ClockRDLParser.LibraryDeclContext ctx) {
        LibraryDecl decl = declFact.createLibraryDecl();

        String name = ctx.IDENTIFIER().getText();

        //add this library to the global scope
        saveScope(decl, currentScope);
        currentScope = currentScope.getEnclosingScope();
        currentScope.define(name, decl);

        decl.setName(name);

        for (ClockRDLParser.LibraryItemContext relationCtx : ctx.libraryItem()) {
            LibraryItemDecl relation = getValue(relationCtx, LibraryItemDecl.class);
            relation.setLibrary(decl);
        }

        setValue(ctx, decl);
        library = decl;
    }

    @Override
    public void enterImportDecl(ClockRDLParser.ImportDeclContext ctx) {
        try {
            String uriString = ctx.STRING().getText().replaceAll("\"", "");
            URI uri = new URI(uriString);

            InputStream inputStream = null;
            if (uri.isAbsolute()) {
                inputStream = uri.toURL().openStream();
            }
            else {
                for (URI baseURI : libraryPaths) {
                    URI resolved = baseURI.resolve(uriString);
                    File file = new File(resolved);
                    if (file.exists()) {
                        inputStream = resolved.toURL().openStream();
                        break;
                    }
                }
            }

            if (inputStream == null) {
                System.err.println("Library named " + ctx.STRING().getText() + " could not be found in Path" + libraryPaths.toString());
                throw new RuntimeException("Library named " + ctx.STRING().getText() + " could not be found in Path" + libraryPaths.toString());
            }

            ANTLRInputStream is = new ANTLRInputStream(inputStream);
            ClockRDLLexer lexer = new ClockRDLLexer(is);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ClockRDLParser parser = new ClockRDLParser(tokens);
            ParseTree tree = parser.systemDecl();
            ParseTreeWalker walker = new ParseTreeWalker();

            walker.walk(this, tree);
            RepositoryDecl repositoryDecl = getValue(tree, RepositoryDecl.class);
            setValue(ctx, repositoryDecl.getLibraries());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    //TODO the library support is very shitty
    //I should reify the importDecl in the model,
    //stop doing relation name resolution here but in an subsequent pass over the instantiated model
    //and use the symbol table to resolve stuff instead of doing this shitty library support

    @Override
    public void exitSystemDecl(ClockRDLParser.SystemDeclContext ctx) {
        RepositoryDecl decl;
        if (ctx.instanceDecl() == null) {
            decl = declFact.createRepositoryDecl();
            decl.setName("top");
        }
        else {
            decl = declFact.createSystemDecl();
            decl.setName("top");
            RelationInstanceDecl instanceDecl = getValue(ctx.instanceDecl(), RelationInstanceDecl.class);
            ((SystemDecl)decl).setRoot(instanceDecl);
        }

        List<LibraryItemDecl> libs = decl.getLibraries();
        for (ClockRDLParser.ImportDeclContext importDeclContext : ctx.importDecl()) {
            List<LibraryItemDecl> importedLibraries = getValue(importDeclContext, List.class);
            libs.addAll(importedLibraries);
        }

        for (ClockRDLParser.LibraryDeclContext libraryDeclContext : ctx.libraryDecl()) {
            LibraryDecl libraryDecl = getValue(libraryDeclContext, LibraryDecl.class);
            libraryDecl.setLibrary(decl);
        }

        setValue(ctx, decl);
    }
}