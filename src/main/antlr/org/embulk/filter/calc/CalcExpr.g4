grammar CalcExpr;
@header {
    package org.embulk.filter.calc;
}


expr:   expr op=('*'|'/') expr      # MulDiv
    |   expr op=('+'|'-') expr      # AddSub
    |   NUM                         # num
    |   ID                          # id
    |   '(' expr ')'                # parens
    ;

MUL :   '*' ;
DIV :   '/' ;
ADD :   '+' ;
SUB :   '-' ;
ID  :   [a-zA-Z][0-9a-zA-Z_]+ ;  // match identifiers
NUM :   '-'?[0-9]+('.'[0-9]*)? ;   // match number
WS  :   [ \t\r\n]+ -> skip ;     // toss out whitespace
