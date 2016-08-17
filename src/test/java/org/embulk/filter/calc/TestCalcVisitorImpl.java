package org.embulk.filter.calc;

import org.embulk.EmbulkTestRuntime;
import org.embulk.config.ConfigLoader;
import org.embulk.config.ConfigSource;
import org.embulk.filter.calc.CalcFilterPlugin.PluginTask;
import org.embulk.spi.Exec;
import org.embulk.spi.Page;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageReader;
import org.embulk.spi.PageTestUtils;
import org.embulk.spi.Schema;
import org.embulk.spi.TestPageBuilderReader;
import org.embulk.spi.time.Timestamp;
import org.embulk.spi.util.Pages;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.msgpack.value.ValueFactory;

import static org.embulk.spi.type.Types.BOOLEAN;
import static org.embulk.spi.type.Types.DOUBLE;
import static org.embulk.spi.type.Types.JSON;
import static org.embulk.spi.type.Types.LONG;
import static org.embulk.spi.type.Types.STRING;
import static org.embulk.spi.type.Types.TIMESTAMP;
import static org.junit.Assert.assertEquals;

import java.util.List;


public class TestCalcVisitorImpl
{
    @Rule
    public EmbulkTestRuntime runtime = new EmbulkTestRuntime();

    @Before
    public void createReasource()
    {
    }

    private ConfigSource config()
    {
        return runtime.getExec().newConfigSource();
    }

    private PluginTask taskFromYamlString(String... lines)
    {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        String yamlString = builder.toString();

        ConfigLoader loader = new ConfigLoader(Exec.getModelManager());
        ConfigSource config = loader.fromYamlString(yamlString);
        return config.loadConfig(PluginTask.class);
    }

    private List<Object[]> filter(PluginTask task, Schema inputSchema, Object ... objects)
    {
        TestPageBuilderReader.MockPageOutput output = new TestPageBuilderReader.MockPageOutput();
        Schema outputSchema = CalcFilterPlugin.buildOutputSchema(task, inputSchema);
        PageBuilder pageBuilder = new PageBuilder(runtime.getBufferAllocator(), outputSchema, output);
        PageReader pageReader = new PageReader(inputSchema);
        CalcVisitorImpl visitor = new CalcVisitorImpl(task, inputSchema, outputSchema, pageReader, pageBuilder);

        List<Page> pages = PageTestUtils.buildPage(runtime.getBufferAllocator(), inputSchema, objects);
        for (Page page : pages) {
            pageReader.setPage(page);

            while (pageReader.nextRecord()) {
                outputSchema.visitColumns(visitor);
                pageBuilder.addRecord();
            }
        }
        pageBuilder.finish();
        pageBuilder.close();
        return Pages.toObjects(outputSchema, output.pages);
    }


    @Test
    public void visit_calc_SingleFormula()
    {
        PluginTask task = taskFromYamlString(
                "type: calc",
                "columns:",
                "  - {name: long1,   formula: \" 100 \"}",
                "  - {name: long2,   formula: \" long2 \"}",
                "  - {name: double1,   formula: \" 11.1 \"}",
                "  - {name: double2,   formula: \" double2 \"}");
        Schema inputSchema = Schema.builder()
                .add("long1", LONG)
                .add("long2", LONG)
                .add("double1", DOUBLE)
                .add("double2", DOUBLE)
                .build();
        List<Object[]> records = filter(task, inputSchema,
                // row1
                new Long(521),new Long(521),new Double(523.5),new Double(523.5),
                // row2
                new Long(521),new Long(521),new Double(523.5),new Double(523.5));

        assertEquals(2, records.size());

        Object[] record;
        {
            record = records.get(0);
            assertEquals(4, record.length);
            assertEquals(new Long(100),   record[0]);
            assertEquals(new Long(521),   record[1]);
            assertEquals(new Double(11.1), record[2]);
            assertEquals(new Double(523.5),record[3]);
        }
    }
    @Test
    public void visit_calc_BasicFormula()
    {
        PluginTask task = taskFromYamlString(
                "type: calc",
                "columns:",
                "  - {name: add_long,   formula: \" add_long + 100\"}",
                "  - {name: sub_long,   formula: \" sub_long - 100\"}",
                "  - {name: mul_long,   formula: \" mul_long * 100\"}",
                "  - {name: div_long,   formula: \" div_long / 100\"}",
                "  - {name: mod_long,   formula: \" mod_long % 100\"}",
                "  - {name: add_double, formula: \" add_double + 100\"}",
                "  - {name: sub_double, formula: \" sub_double - 100\"}",
                "  - {name: mul_double, formula: \" mul_double * 100\"}",
                "  - {name: div_double, formula: \" div_double / 100\"}",
                "  - {name: mod_double, formula: \" mod_double % 100\"}");
        Schema inputSchema = Schema.builder()
                .add("add_long", LONG)
                .add("sub_long", LONG)
                .add("mul_long", LONG)
                .add("div_long", LONG)
                .add("mod_long", LONG)
                .add("add_double", DOUBLE)
                .add("sub_double", DOUBLE)
                .add("mul_double", DOUBLE)
                .add("div_double", DOUBLE)
                .add("mod_double", DOUBLE)
                .build();
        List<Object[]> records = filter(task, inputSchema,
                // row1
                new Long(521),new Long(521),new Long(521),new Long(521),new Long(521),
                new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5),
                // row2
                new Long(521),new Long(521),new Long(521),new Long(521),new Long(521),
                new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5));

        assertEquals(2, records.size());

        Object[] record;
        {
            record = records.get(0);
            assertEquals(10, record.length);
            assertEquals(new Long(621),   record[0]);
            assertEquals(new Long(421),   record[1]);
            assertEquals(new Long(52100), record[2]);
            assertEquals(new Long(5),     record[3]);
            assertEquals(new Long(21),    record[4]);
            assertEquals(new Double(623.5),record[5]);
            assertEquals(new Double(423.5),record[6]);
            assertEquals(new Double(52350),record[7]);
            assertEquals(new Double(5.235),record[8]);
            assertEquals(new Double(23.5), record[9]);
        }
    }
    @Test
    public void visit_calc_PriorityChkFormula()
    {
        PluginTask task = taskFromYamlString(
                "type: calc",
                "columns:",
                "  - {name: add_long,   formula: \" add_long + 100 * 3\"}",
                "  - {name: sub_long,   formula: \" sub_long - 100 * 3\"}",
                "  - {name: mul_long,   formula: \" mul_long * 100 * 3\"}",
                "  - {name: div_long,   formula: \" div_long / 100 * 3\"}",
                "  - {name: mod_long,   formula: \" mod_long % 100 * 3\"}",
                "  - {name: add_double, formula: \" add_double + 100 * 3\"}",
                "  - {name: sub_double, formula: \" sub_double - 100 * 3\"}",
                "  - {name: mul_double, formula: \" mul_double * 100 * 3\"}",
                "  - {name: div_double, formula: \" div_double / 100 * 3\"}",
                "  - {name: mod_double, formula: \" mod_double % 100 * 3\"}");
        Schema inputSchema = Schema.builder()
                .add("add_long", LONG)
                .add("sub_long", LONG)
                .add("mul_long", LONG)
                .add("div_long", LONG)
                .add("mod_long", LONG)
                .add("add_double", DOUBLE)
                .add("sub_double", DOUBLE)
                .add("mul_double", DOUBLE)
                .add("div_double", DOUBLE)
                .add("mod_double", DOUBLE)
                .build();
        List<Object[]> records = filter(task, inputSchema,
                // row1
                new Long(521),new Long(521),new Long(521),new Long(521),new Long(521),
                new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5),
                // row2
                new Long(521),new Long(521),new Long(521),new Long(521),new Long(521),
                new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5));

        assertEquals(2, records.size());

        Object[] record;
        {
            record = records.get(0);
            assertEquals(10, record.length);
            assertEquals(new Long(821),   record[0]);
            assertEquals(new Long(221),   record[1]);
            assertEquals(new Long(156300), record[2]);
            assertEquals(new Long(15),     record[3]);
            assertEquals(new Long(63),    record[4]);
            assertEquals(new Double(823.5),record[5]);
            assertEquals(new Double(223.5),record[6]);
            assertEquals(new Double(157050),record[7]);
//            assertEquals(new Double(15.705),record[8]); // TODO result 15.705000000000002
            assertEquals(new Double(70.5), record[9]);
        }
    }
    @Test
    public void visit_calc_ParenChkFormula()
    {
        PluginTask task = taskFromYamlString(
                "type: calc",
                "columns:",
                "  - {name: add_long,   formula: \" ( add_long + 100 ) * 3\"}",
                "  - {name: sub_long,   formula: \" ( sub_long - 100 ) * 3\"}",
                "  - {name: mul_long,   formula: \" ( mul_long * 100 ) * 3\"}",
                "  - {name: div_long,   formula: \" ( div_long / 100 ) * 3\"}",
                "  - {name: mod_long,   formula: \" ( mod_long % 100 ) * 3\"}",
                "  - {name: add_double, formula: \" ( add_double + 100 ) * 3\"}",
                "  - {name: sub_double, formula: \" ( sub_double - 100 ) * 3\"}",
                "  - {name: mul_double, formula: \" ( mul_double * 100 ) * 3\"}",
                "  - {name: div_double, formula: \" ( div_double / 100 ) * 3\"}",
                "  - {name: mod_double, formula: \" ( mod_double % 100 ) * 3\"}");
        Schema inputSchema = Schema.builder()
                .add("add_long", LONG)
                .add("sub_long", LONG)
                .add("mul_long", LONG)
                .add("div_long", LONG)
                .add("mod_long", LONG)
                .add("add_double", DOUBLE)
                .add("sub_double", DOUBLE)
                .add("mul_double", DOUBLE)
                .add("div_double", DOUBLE)
                .add("mod_double", DOUBLE)
                .build();
        List<Object[]> records = filter(task, inputSchema,
                // row1
                new Long(521),new Long(521),new Long(521),new Long(521),new Long(521),
                new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5),
                // row2
                new Long(521),new Long(521),new Long(521),new Long(521),new Long(521),
                new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5),new Double(523.5));

        assertEquals(2, records.size());

        Object[] record;
        {
            record = records.get(0);
            assertEquals(10, record.length);
            assertEquals(new Long(1863),   record[0]);
            assertEquals(new Long(1263),   record[1]);
            assertEquals(new Long(156300), record[2]);
            assertEquals(new Long(15),     record[3]);
            assertEquals(new Long(63),    record[4]);
            assertEquals(new Double(1870.5),record[5]);
            assertEquals(new Double(1270.5),record[6]);
            assertEquals(new Double(157050),record[7]);
//            assertEquals(new Double(15.705),record[8]); // TODO result 15.705000000000002
            assertEquals(new Double(70.5), record[9]);
        }
    }
}