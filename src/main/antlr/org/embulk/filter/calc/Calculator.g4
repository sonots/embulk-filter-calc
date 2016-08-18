grammar Calculator;
@header {
package org.embulk.filter.calc;
}

expr: <assoc=right> expr '^' expr      # Power
    | expr op=('*'|'/'|'%') expr       # MulDivMod
    | expr op=('+'|'-') expr           # AddSub
    | NUM                              # Number
    | ID                               # Identifier
    | func                             # MathFunc
    | '(' expr ')'                     # Paren
    ;

func: COS '(' expr ')' # FuncCos
    | TAN '(' expr ')' # FuncTan
    | SIN '(' expr ')' # FuncSin
//    | ACOS
//    | ATAN
//    | ASIN
//    | LOG
//    | LN
//    | ROUDUP
//    | ROUDDOWN
    ;

ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';
MOD: '%';

COS: 'cos';
SIN: 'sin';
TAN: 'tan';
// ACOS: 'acos';
// ASIN: 'asin';
// ATAN: 'atan';
// LN: 'ln';
// LOG: 'log';

COMMA: ',';

ROUNDUP: 'roundup';
ROUNDDOWN: 'roundown';

NUM: '-'?[0-9]+('.' [0-9]+)?;
ID: [a-zA-Z][0-9A-Za-z_-]*;
WS: [ \t\r\n]+ -> skip;



