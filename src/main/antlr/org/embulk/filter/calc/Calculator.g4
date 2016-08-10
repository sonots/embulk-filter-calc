grammar Calculator;
@header {
package org.embulk.filter.calc;
}

expr: <assoc=right> expr '^' expr      # Power
    | expr op=('*'|'/'|'%') expr       # SubDivMod
    | expr op=('+'|'-') expr           # AddSub
    | NUM                              # Number
    | ID                               # Identifier
    | func '(' expr ')'                # MathFunc
    | '(' expr ')'                     # Paren
    | multifunc '(' expr COMMA NUM ')' # Multifunca
    ;

func: COS
    | TAN
    | SIN
    | ACOS
    | ATAN
    | ASIN
    | LOG
    | LN
    ;

multifunc: ROUNDUP
         | ROUNDDOWN
         ;

ADD: '+';
SUB: '-';
MUL: '*';
DIV: '/';
MOD: '%';

COS: 'cos';
SIN: 'sin';
TAN: 'tan';
ACOS: 'acos';
ASIN: 'asin';
ATAN: 'atan';
LN: 'ln';
LOG: 'log';

COMMA: ',';

ROUNDUP: 'roundup';
ROUNDDOWN: 'roundown';

NUM: '-'?[0-9]+('.' [0-9]+)?;
//NUM: [0-9]+('.' [0-9]+)?;
ID: [a-zA-Z][0-9A-Za-z_-]*;
WS: [ \t\r\n]+ -> skip;



