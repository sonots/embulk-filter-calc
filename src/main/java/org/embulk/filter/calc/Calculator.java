package org.embulk.filter.calc;

// import org.antlr.runtime.ANTLRInputStream;
// import org.antlr.runtime.CommonTokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;

public class Calculator
{
    private String name;
    private String formula;

    public Calculator(String name, String formula)
    {
        this.formula = formula;
        this.name = name;
    }

    public Boolean validateFormula(){
        ANTLRInputStream input = new ANTLRInputStream(this.formula);
        CalcExprLexer lexer = new CalcExprLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CalcExprParser parser = new CalcExprParser(tokens);
        ConfigErrorListener errorListener = new ConfigErrorListener();
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        ParseTree tree = parser.expr();
        return true;
    }
}

