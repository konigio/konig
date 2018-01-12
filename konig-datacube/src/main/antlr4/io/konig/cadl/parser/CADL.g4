
grammar CADL ;

cube	: 'CUBE' NAME '{' (dimension)* (measure)* '}' ;

dimension	: 'DIMENSION' NAME (level)* (rollup)* ;

level	: 'LEVEL' NAME 'ATTRIBUTES' '{' (attribute)* '}';

measure	: 'MEASURE' NAME '(' datatype ')';

attribute : NAME '(' datatype ')' (UNIQUE)? (',')?;

UNIQUE : 'UNIQUE' ;

rollup :  NAME 'ROLL-UP to' NAME ;

datatype : 'real'|'string'|'date'|'integer' ;

NAME	: [a-zA-Z][a-zA-Z0-9]* ;

COMMENT : '#' ~([\r\n])* -> skip ;

WS : [ \t\r\n]+ -> skip ;
