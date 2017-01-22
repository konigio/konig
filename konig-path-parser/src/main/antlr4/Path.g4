/**
 * Define a grammar called Path
 */
grammar Path;

@header {
	package io.konig.antlr.path;
} 


path : step+ ;
step : start | in | out | filter ;
start : iri ;
in : '^' iri ;
out : '/' iri ;
resource : iri | bnode ;
filter : '[' WS? hasStep WS? (';' WS? hasStep)* ']' ;

hasStep : hasPredicate hasStepSpace hasValue  ;

hasStepSpace : WS+ ;

hasPredicate : iri ;
hasValue : iri | literal ;

literal :  numericLiteral  | stringLiteral | booleanLiteral ;

booleanLiteral : TRUE | FALSE ;

stringLiteral : '"' stringContent '"' stringModifier?;

stringModifier : '^' stringType | '@' languageCode ;

stringType : iri ;

languageCode : LETTER+ ;

stringContent : stringChar* ;

stringChar : asciiChar | UNICODE ;

asciiChar :  LETTER | DIGIT | WS | '!' | '#' | '$' | '%' | '&' | '\u0027' | '(' |
	 ')' | '*' | '+' | ',' | '-' | '.' | '/' | ':' | ';' | '<' | '=' | '>' | '?' | '@' | '[' | ']' |
	 '^' | '_' | '`' | '{' | '|' | '}' | '~'  ;
	 
	 
numericLiteral : DIGIT+ ('.' DIGIT+)? ;

iri : fullIri | qname  | bareLocalName ;

fullIri : '<' rawIri '>' ;
	 
rawIri : 	(LETTER | DIGIT | '!' | '"' | '#' | '$' | '%' | '&' | '\u0027' | '(' |
	 ')' | '*' | '+' | ',' | '-' | '.' | '/' | ':' | ';' | '=' | '?' | '@' | '[' | ']' |
	 '^' | '_' | '`' | '{' | '|' | '}' | '~' )+ ;

qname : prefix ':' localName ;

prefix : LETTER (LETTER | DIGIT | '_' | '-')+ ;

localName : (LETTER | DIGIT | '_' | '-')+ ;

bareLocalName : (LETTER | '_') localName ;

bnode : '_:' localName ;

TRUE : 'true' ;
FALSE : 'false' ;
UNICODE : [\u0080-\uFFFF] ;
LETTER : [A-Z] | [a-z];
DIGIT : [0-9];


WS : [ \t\r\n]+ ; // skip spaces, tabs, newlines

