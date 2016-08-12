package org.embulk.filter.calc;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.embulk.spi.Schema;

public class CalcConfigChecker
{
    private String name;
    private String formula;
    private Schema inputSchema;

    public CalcConfigChecker(String name, String formula,Schema inputSchema)
    {
        this.formula = formula;
        this.name = name;
        this.inputSchema = inputSchema;
    }

    public Boolean validateFormula(){
        ANTLRInputStream input = new ANTLRInputStream(this.formula);
        CalculatorLexer lexer = new CalculatorLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CalculatorParser parser = new CalculatorParser(tokens);
        ConfigErrorListener errorListener = new ConfigErrorListener(name);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        ParseTree tree = parser.expr();
        CalcConfigCheckVisitor eval = new CalcConfigCheckVisitor(inputSchema);
        eval.visit(tree);
        return true;
    }
}

