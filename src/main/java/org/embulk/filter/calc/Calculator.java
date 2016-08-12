package org.embulk.filter.calc;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.embulk.spi.PageReader;
import org.embulk.spi.Schema;

public class Calculator
{
    private String formula;
    private Schema inputSchema;
    private PageReader pageReader;
    private ParseTree tree;
    private CalcFormulaVisitor visitor;

    public Calculator(String formula,Schema inputSchema,PageReader pageReader)
    {
        this.formula = formula;
        this.inputSchema = inputSchema;
        this.pageReader = pageReader;


        ANTLRInputStream input = new ANTLRInputStream(formula);
        CalculatorLexer lexer = new CalculatorLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CalculatorParser parser = new CalculatorParser(tokens);

        this.tree = parser.expr();
        this.visitor = new CalcFormulaVisitor(inputSchema,pageReader);

    }

    public double calc(){
        return visitor.visit(tree);
    }

}

