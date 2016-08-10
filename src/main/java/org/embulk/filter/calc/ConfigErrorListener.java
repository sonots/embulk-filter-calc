package org.embulk.filter.calc;

import org.antlr.v4.runtime.*;
import org.embulk.config.ConfigException;

import static java.util.Locale.ENGLISH;

public class ConfigErrorListener
        extends BaseErrorListener
{
    private String column_name;

    public ConfigErrorListener(String column_name){
        this.column_name =  column_name;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
            Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
    {
        String err = String.format(ENGLISH, "The \"%s\" column has invalid formula. line: %d error: %s", column_name,line,msg);
        throw new ConfigException(err);
    }
}
