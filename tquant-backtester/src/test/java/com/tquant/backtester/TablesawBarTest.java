package com.tquant.backtester;

import static tech.tablesaw.aggregate.AggregateFunctions.sum;

import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.NumericColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.numbers.IntColumnType;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.HorizontalBarPlot;
import tech.tablesaw.plotly.api.VerticalBarPlot;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.components.Marker;
import tech.tablesaw.plotly.traces.BarTrace;
import tech.tablesaw.plotly.traces.Trace;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/10
 */
public class TablesawBarTest {
  public static void main(String[] args) throws Exception {
    Table table = Table.read().csv("data/tornadoes_1950-2014.csv");
    NumericColumn<?> logNInjuries = table.numberColumn("injuries").add(1).logN();
    logNInjuries.setName("log injuries");
    table.addColumns(logNInjuries);
    IntColumn scale = table.intColumn("scale");
    scale.set(scale.isLessThan(0), IntColumnType.missingValueIndicator());

    Table summaryTable = table.summarize("fatalities", "log injuries", sum).by("Scale");

    Plot.show(
        HorizontalBarPlot.create(
            "Tornado Impact",
            summaryTable,
            "scale",
            Layout.BarMode.STACK,
            "Sum [Fatalities]",
            "Sum [log injuries]"));

    Plot.show(
        VerticalBarPlot.create(
            "Tornado Impact",
            summaryTable,
            "scale",
            Layout.BarMode.GROUP,
            "Sum [Fatalities]",
            "Sum [log injuries]"));

    Layout layout =
        Layout.builder()
            .title("Tornado Impact")
            .barMode(Layout.BarMode.GROUP)
            .showLegend(true)
            .build();

    String[] numberColNames = {"Sum [Fatalities]", "Sum [log injuries]"};
    String[] colors = {"#85144b", "#FF4136"};

    Trace[] traces = new Trace[2];
    for (int i = 0; i < 2; i++) {
      String name = numberColNames[i];
      BarTrace trace =
          BarTrace.builder(summaryTable.categoricalColumn("scale"), summaryTable.numberColumn(name))
              .orientation(BarTrace.Orientation.VERTICAL)
              .marker(Marker.builder().color(colors[i]).build())
              .showLegend(true)
              .name(name)
              .build();
      traces[i] = trace;
    }
    Plot.show(new Figure(layout, traces));
  }
}
