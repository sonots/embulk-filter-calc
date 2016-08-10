package org.embulk.filter.calc;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;


public class ConfigErrorListener extends BaseErrorListener
{
    @Override
    public void syntaxError(Recognizer<?,?>recognizer,Object offendingSymbol,int line, int charPositionInLine,String msg,RecognitionException e)
    {
        System.out.println("** line" + line + " " + msg );
    }
}
