grammar ClockRDL;
//clock relation definition language

LINE_COMMENT : '//' .*? '\n' -> skip ;
COMMENT : '/*' .*? '*/' -> skip ;
WS : [ \r\t\n]+ -> skip ;

libraryDecl : LIBRARY IDENTIFIER '{' libraryItem+ '}';
libraryItem : relationDecl
    | libraryDecl
    ;

relationDecl: RELATION IDENTIFIER argumentDecl? clockDecl? variableDecl? constantDecl? functionDecl* LCURLY (primitiveRelationBody | compositeRelationBody) RCURLY;

argumentDecl : LPAREN IDENTIFIER+ RPAREN;
clockDecl : CLOCK IDENTIFIER+;
variableDecl : VARIABLE initializedIdentifier+;
constantDecl : CONST initializedIdentifier+;
initializedIdentifier : IDENTIFIER (ASSIGN expression)?;

functionDecl : DEFINE IDENTIFIER (LPAREN IDENTIFIER+ RPAREN)? blockStmt;

primitiveRelationBody: transitionDecl+;
transitionDecl: guard? vector action?;
guard: '[' expression ']';
vector: '{' IDENTIFIER* '}';
action: '[' statement ']';

compositeRelationBody : clockDecl? instanceDecl+;
instanceDecl: (IDENTIFIER COLON)? qualifiedName LPAREN formalToActual* RPAREN;
formalToActual: (IDENTIFIER ':')? expression;
qualifiedName : IDENTIFIER ('.' IDENTIFIER)*;

//LITERALS
literal : booleanLiteral
    | integerLiteral
    | arrayLiteral
    | recordLiteral
    | queueLiteral          //maybe it is better to call this a dynamic array?
;

booleanLiteral : TRUE | FALSE;
integerLiteral : NUMBER;
arrayLiteral : LSQUARE expression* RSQUARE ;
recordLiteral : LCURLY fieldLiteral+ RCURLY;
fieldLiteral : IDENTIFIER EQ expression;
queueLiteral : LCURLYPIPE expression* RCURLYPIPE;

//EXPRESSIONS
expression :
    LPAREN expression RPAREN                                        #ParenExp
  | literal                                                         #LiteralExp
  | expression LSQUARE expression RSQUARE                           #IndexedExp
  | expression DOT IDENTIFIER                                       #SelectedExp
  | expression LPAREN expression* RPAREN                            #FunctionCallExp
  | reference                                                       #ReferenceExp
  | operator=(NOT | PLUS | MINUS) expression        				#UnaryExp
  | expression operator=(MULT | DIV | MOD) expression               #BinaryExp
  | expression operator=(PLUS | MINUS) expression                   #BinaryExp
  | expression operator=(LT | LTE | GT | GTE ) expression           #BinaryExp
  | expression operator=(EQ | NEQ) expression                       #BinaryExp
  | expression operator=(OR | AND | NOR | NAND | XOR) expression    #BinaryExp
  | expression '?' expression ':' expression                        #ConditionalExp
;
reference: IDENTIFIER;

statement : expression      //an expression is a statement, this way I can write function calls
    | assignmentStmt
    | conditionalStmt
    | loopStmt
    | returnStmt
    | blockStmt
;

assignmentStmt : expression operator=(ASSIGN | MINUSASSIGN | PLUSASSIGN | MULTASSIGN | MODASSIGN | DIVASSIGN | ORASSIGN | ANDASSIGN) expression;
conditionalStmt : IF expression blockStmt (ELSE blockStmt)?;
loopStmt : WHILE expression blockStmt;
returnStmt : RETURN expression;

blockStmt : LCURLY blockDecl* statement* RCURLY;

blockDecl : variableDecl
    | constantDecl
    ;

LIBRARY: 'library';
RELATION: 'relation';
PRIMITIVE: 'primitive';
VARIABLE: 'var';
CLOCK: 'clock';
CONST: 'const';
TRUE: 'true';
FALSE: 'false';
IF: 'if';
WHILE: 'while';
ELSE: 'else';
RETURN: 'return';
DEFINE: 'def';

//special characters
LPAREN : '(';
RPAREN : ')';
LSQUARE : '[';
RSQUARE : ']';
LCURLY : '{';
RCURLY : '}';
LCURLYPIPE : '{|';
RCURLYPIPE : '|}';
ASSIGN : ':=';
MINUSASSIGN : '-=';
PLUSASSIGN : '+=';
MULTASSIGN : '*=';
MODASSIGN : '%=';
ANDASSIGN : '&=';
ORASSIGN : '|=';
DIVASSIGN : '/=';
SEMICOLON : ';';
DOT : '.';
NOT: '!';
OR : '|';
AND: '&';
NOR: 'nor';
NAND: 'nand';
XOR: 'xor';
PLUS : '+';
MINUS : '-';
MULT : '*';
DIV: '/';
MOD: '%';
LTE : '<=';
LT : '<';
GTE : '>=';
GT : '>';
EQ : '=';
NEQ : '!=';
COLON: ':';

IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]*;
NUMBER : [0-9]+;



