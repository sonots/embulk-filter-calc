package org.embulk.filter.calc;

import org.embulk.config.ConfigException;
import org.embulk.spi.Column;
import org.embulk.spi.Page;
import org.embulk.spi.PageReader;
import org.embulk.spi.Schema;
import org.embulk.spi.type.Types;

import static java.util.Locale.ENGLISH;

public class CalcFormulaVisitor
        extends CalculatorBaseVisitor<Double>
{
    private PageReader pageReader;
    private Schema inputSchema;

    public CalcFormulaVisitor(Schema inputSchema, PageReader pageReader)
    {
        this.pageReader = pageReader;
        this.inputSchema = inputSchema;
    }

    @Override
    public Double visitMulDivMod(CalculatorParser.MulDivModContext ctx){
        Double left  = visit(ctx.expr(0));
        Double right = visit(ctx.expr(1));

        if( left == null || right == null )
            return null;
        else if ( ctx.op.getType() == CalculatorParser.MUL )
            return left * right;

        else if( ctx.op.getType() == CalculatorParser.DIV )
            return left / right;
        else
            return left % right;
    }

    @Override
    public Double visitAddSub(CalculatorParser.AddSubContext ctx){
        Double left  = visit(ctx.expr(0));
        Double right = visit(ctx.expr(1));
        if( left == null || right == null )
            return null;
        else if ( ctx.op.getType() == CalculatorParser.ADD )
            return left + right;
        else
            return left - right;
    }

    @Override
    public Double visitNumber(CalculatorParser.NumberContext ctx){
        String id = ctx.NUM().getText();

        return Double.parseDouble(id);
    }

    @Override
    public Double visitIdentifier(CalculatorParser.IdentifierContext ctx)
    {
        String id = ctx.ID().getText();
        Double val;
        Column column = inputSchema.lookupColumn(id);

        if( pageReader.isNull(column) ){
            val = null;
        } else if ( Types.DOUBLE.equals(column.getType()) ){
            val = pageReader.getDouble(column);
        } else if( Types.LONG.equals(column.getType()) ){
            Long v;
            v = pageReader.getLong(column);
            val = v.doubleValue();
        } else {
            // throw
            val = null;
        }
        return val;
    }
}
