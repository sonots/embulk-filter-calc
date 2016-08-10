package org.embulk.filter.calc;

import org.embulk.config.ConfigException;
import org.embulk.spi.Column;
import org.embulk.spi.Schema;
import org.embulk.spi.type.Types;
import static java.util.Locale.ENGLISH;

public class CalcConfigCheckVisitor
        extends CalculatorBaseVisitor<Double>
{
    private Schema inputSchema;

    public CalcConfigCheckVisitor(Schema inputSchema)
    {
        this.inputSchema = inputSchema;
    }

    @Override
    public Double visitIdentifier(CalculatorParser.IdentifierContext ctx)
            throws ConfigException
    {
        String id = ctx.ID().getText();
        Column column = inputSchema.lookupColumn(id); // throw ConfigException if column not found.
        if (!Types.DOUBLE.equals(column.getType()) && !Types.LONG.equals(column.getType())) {
            String err = String.format(ENGLISH, "\"%s\" is not long and double column", id);
            throw new ConfigException(err);
        }
        return null;
    }
}
