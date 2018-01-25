
grammar CADL ;

base : namespace*  cube+ ;

namespace : 'PREFIX' NAME ':' IRI ;

cube	: 'CUBE' NAME '{' (dimension)* (measure)* '}' ;

dimension	: 'DIMENSION' NAME (level)* (rollup)* ;

level	: 'LEVEL' NAME 'ATTRIBUTES' '{' (attribute)* '}';

measure	: 'MEASURE' NAME '(' datatype ')';

attribute : NAME '(' datatype ')' (UNIQUE)? (',')?;

UNIQUE : 'UNIQUE' ;

rollup :  NAME 'ROLL-UP to' NAME ;

datatype : 'real'|'string'|'date'|'integer' ;

NAME	: [a-zA-Z][a-zA-Z0-9]* ;

// improve IRI regex
IRI : ('http'|'https'|'urn')':'[a-zA-Z0-9/._\-:]+;

COMMENT : '#' ~([\r\n])* -> skip ;

WS : [ \t\r\n]+ -> skip ;
