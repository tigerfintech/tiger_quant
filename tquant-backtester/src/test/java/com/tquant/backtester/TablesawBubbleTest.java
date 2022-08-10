package com.tquant.backtester;

import java.io.IOException;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.BubblePlot;
import tech.tablesaw.plotly.components.Figure;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/10
 */
public class TablesawBubbleTest {

  public static void main(String[] args) throws IOException {

    Table wines = Table.read().csv("data/test_wines.csv");

    Table champagne =
        wines.where(
            wines
                .stringColumn("wine type")
                .isEqualTo("Champagne & Sparkling")
                .and(wines.stringColumn("region").isEqualTo("California")));

    Figure figure =
        BubblePlot.create(
            "Average retail price for champagnes by year and rating",
            champagne, // table name
            "highest pro score", // x variable column name
            "year", // y variable column name
            "Mean Retail" // bubble size
        );

    Plot.show(figure);
  }

}
