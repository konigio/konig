grammar SqlCreateTable;

@header {
	package io.konig.sql.antlr;
} 

sql : sqlElement+ ;

sqlElement : semanticDirective | createTable ;

semanticDirective 
	: prefixDirective 
	| tableShapeIriTemplate 
	| tableTargetShapeIriTemplate
	| tableTargetClassIriTemplate 
	| columnPredicateIriTemplate 
	| columnPathTemplate ;
	
tableShapeIriTemplate : '@tableShapeIriTemplate' iriRef '.' ;
tableTargetClassIriTemplate : '@tableTargetClassIriTemplate' iriRef '.' ;
columnPredicateIriTemplate : '@columnPredicateIriTemplate' iriRef '.' ; 
columnPathTemplate : '@columnPathTemplate' pathValue  '.' ;
tableTargetShapeIriTemplate : '@tableTargetShapeIriTemplate' iriRef '.' ;

createTable : CREATE GLOBAL? TEMPORARY? TABLE tableId tableParts tableSemantics?  ;

tableId : tableRef ;

tableSemantics : SEMANTICS tableSemanticStatement (',' tableSemanticStatement)* '.';

tableSemanticStatement 
	: tableShapeId 
	| tableTargetShapeId
	| tableTargetClass 
	| tableColumnPredicateIriTemplate 
	| tableColumnPathTemplate 
	| tablePathPattern ;
	
tablePathPattern : PathPattern '(' patternPrefix ',' patternClass ',' patternPath ')';

patternPrefix : id ;

patternClass : iri ;

patternPath : pathValue ;
	

tableColumnPathTemplate : 'columnPathTemplate' pathValue ;

tableTargetClass : TargetClass iri ;
tableColumnPredicateIriTemplate : 'columnPredicateIriTemplate' iriRef ;

tableTargetShapeId : TargetShape iri ;

tableShapeId : HasShape iri ;

tableParts : '(' tablePart (',' tablePart)* ')';

tablePart : columnDef | tableConstraint ;

id : (LETTER | '_' | keyword) (LETTER | DIGIT | '_' | keyword)* ;

curiePart : (LETTER | '_' | keyword | '{' ) (LETTER | DIGIT | '_' | keyword | '{' | '}')* ;



iri : iriRef | curie  ;

curie : curiePart ':' curiePart ;

tableRef : (schemaName '.')? tableName ;

schemaName : id ;

tableName : id ;

tableConstraint : (CONSTRAINT constraintName)? tableConstraintPhrase ;

tableConstraintPhrase : tablePrimaryKey | uniqueKeyConstraint | tableForeignKey ;

tableForeignKey : FOREIGN KEY  referencingColumnList referencesClause ;

referencingColumnList : columnList ;

referencesClause : REFERENCES tableRef columnList ;

uniqueKeyConstraint : UNIQUE columnList ;

tablePrimaryKey : PRIMARY KEY columnList ;

columnList : '(' simpleColumnName ( ',' simpleColumnName )* ')' ;

simpleColumnName : id ;

constraintName : id ;

columnDef : columnName columnType columnConstraintDef* columnSemantics?;

columnSemantics : SEMANTICS columnSemanticStatement (';' columnSemanticStatement)* ;

columnSemanticStatement :  columnPredicate | columnPath ;

columnPath : Path pathValue ;

pathValue : step+ ;

step : iri | '^' iri | '/' iri | FILTER ;


columnPredicate : Predicate iri ;

columnConstraintDef : constraintNameDef? columnConstraint ;

constraintNameDef : CONSTRAINT constraintName ;

columnConstraint : nullConstraint | notNull | uniqueSpec ;

uniqueSpec : columnUnique | columnPrimaryKey ;

nullConstraint : NULL ;

notNull : NOT NULL;

columnUnique : UNIQUE ;

columnPrimaryKey : PRIMARY KEY ;

columnName : id ;

columnType : datatype columnSize? ;

columnSize : '(' sizeValue (',' precision)? ')' ;

precision : DIGIT+ ;

sizeValue : DIGIT+ | MAX ;

datatype : BIGINT | BINARY | BIT | CHAR |  DATE | DATETIME | DATETIME2 | DECIMAL | FLOAT |
	IMAGE | INT | NCHAR | NTEXT | NUMERIC | NVARCHAR |REAL | SMALLDATETIME |
	SMALLINT | TEMPORARY | TEXT | TIME | TIMESTAMP | TINYINT |
	UNIQUEIDENTIFIER | VARBINARY | VARCHAR | XML ;

keyword 
	:	datatype | CREATE |TABLE | GLOBAL | MAX | NOT | NULL | PRIMARY | KEY | UNIQUE | FOREIGN | REFERENCES 
	| SEMANTICS | PathPattern | TargetClass | TargetShape | HasShape | Path | Predicate; 

prefixDirective : '@prefix' nsPrefix ':' iriRef '.' ;

nsPrefix : id ;

iriRef : IRIREF ;
Predicate : 'predicate' ;
Path : 'path' ;
HasShape : 'hasShape' ;
TargetShape : 'targetShape' ;
TargetClass : 'targetClass' ;
PathPattern : 'pathPattern' ;

FILTER : '[' ( ~[[\]] | FILTER )+ ']' ;

IRIREF : '<' ~[<]* '>' ;

DIGIT : [0-9] ;

LETTER : ([a-z]|[A-Z]) ;   

BIGINT : B I G I N T ;
BINARY : B I N A R Y ;
BIT : B I T ;
CHAR : C H A R ;
CONSTRAINT : C O N S T R A I N T ;
CREATE : C R E A T E ;
DATE : D A T E ;
DATETIME : D A T E T I M E ;
DATETIME2 : D A T E T I M E '2' ;
DECIMAL : D E C I M A L ;
FLOAT : F L O A T ;
FOREIGN : F O R E I G N ;
GLOBAL : G L O B A L ;
IMAGE : I M A G E ;
INT : I N T ;
KEY : K E Y ;
MAX : M A X ;
NCHAR : N C H A R ;
NOT : N O T ;
NTEXT : N T E X T ;
NULL : N U L L ;
NUMERIC : N U M E R I C ;
NVARCHAR : N V A R C H A R ;
PRIMARY : P R I M A R Y ;
REAL : R E A L ;
REFERENCES : R E F E R E N C E S ;
SMALLDATETIME : S M A L L D A T E T I M E ;
SMALLINT : S M A L L I N T ;
TABLE : T A B L E ;
TEMPORARY : T E M P O R A R Y ;
TEXT : T E X T ;
TIME : T I M E ;
TIMESTAMP : T I M E S T A M P ;
TINYINT : T I N Y I N T ;
UNIQUE : U N I Q U E ;
UNIQUEIDENTIFIER : U N I Q U E I D E N T I F I E R ;
VARBINARY : V A R B I N A R Y ;
VARCHAR : V A R C H A R ;
XML : X M L ;
SEMANTICS : S E M A N T I C S ;

SPECIALCHAR : '!' | '#' | '$' | '%' | '&' | '=' | '?' | '@' | '*' | '+' | ',' | '-' | '[' | ']' | '{' | '}' | '~';

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');

