package org.embulk.filter.calc;


import org.embulk.config.Config;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskSource;
import org.embulk.spi.Exec;
import org.embulk.spi.FilterPlugin;
import org.embulk.spi.Page;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageOutput;
import org.embulk.spi.PageReader;
import org.embulk.spi.Schema;

import java.util.List;

// import com.google.common.base.Optional;
// import org.embulk.config.ConfigDefault;
// import org.embulk.config.ConfigDiff;
// import org.embulk.spi.Column;

public class CalcFilterPlugin
        implements FilterPlugin
{

    // private Object IOException;

    public interface CalcConfig extends Task
    {
        @Config("formula")
        String getFormula();

        @Config("name")
        String getName();
    }


    public interface PluginTask
            extends Task
    {

        @Config("columns")
        public List<CalcConfig> getCalcConfig();
    }

    @Override
    public void transaction(ConfigSource config, Schema inputSchema,
            FilterPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);

        Schema outputSchema = inputSchema;
        for( CalcConfig calcConfig: task.getCalcConfig() ){
            Calculator calc = new Calculator(calcConfig.getName(),calcConfig.getFormula());
            calc.validateFormula();
        }

        control.run(task.dump(), outputSchema);
    }

    @Override
    public PageOutput open(TaskSource taskSource, Schema inputSchema,
            Schema outputSchema, PageOutput output)
    {
        PluginTask task = taskSource.loadTask(PluginTask.class);
        final PageReader pageReader = new PageReader(inputSchema);
        final PageBuilder pageBuilder = new PageBuilder(Exec.getBufferAllocator(), outputSchema, output);


        return new PageOutput() {
            @Override
            public void finish()
            {
                pageBuilder.finish();
            }

            @Override
            public void close()
            {
                pageBuilder.close();
            }

            @Override
            public void add(Page page)
            {
                pageReader.setPage(page);

                while (pageReader.nextRecord()) {
//                    if (CalcVisitor.visitColumns(inputSchema)) {
                        // output.add(page); did not work, double release() error occurred. We need to copy from reader to builder...
//                        outputSchema.visitColumns(builderVisitor);
                        pageBuilder.addRecord();
//                    }
                }
            }
        };


    }
}
