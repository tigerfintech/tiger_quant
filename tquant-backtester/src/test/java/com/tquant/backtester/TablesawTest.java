package com.tquant.backtester;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.DataFrameReader;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/05
 */
public class TablesawTest {

  @Test
  public void testColumn() {
    double[] numbers = {1, 2, 3, 4};
    DoubleColumn nc = DoubleColumn.create("nc", numbers);
    System.out.println(nc.print());
    double three = nc.get(2);
    System.out.println(three);
    DoubleColumn nc2 = nc.multiply(4);
    System.out.println(nc2.print());

    System.out.println(nc.isLessThan(3));
  }

  @Test
  public void testCreateTable() {
    String[] animals = {"bear", "cat", "giraffe"};
    double[] cuteness = {90.1, 84.3, 99.7};

    Table cuteAnimals =
        Table.create("Cute Animals")
            .addColumns(
                StringColumn.create("Animal types", animals),
                DoubleColumn.create("rating", cuteness));
    System.out.println(cuteAnimals);

    System.out.println(cuteAnimals.structure());

    System.out.println(cuteAnimals.first(1));
    System.out.println(cuteAnimals.last(1));
  }

  @Test
  public void testDataFrame() {
    Map<LocalDate, DailyResult> dailyResults = new HashMap<>();
    dailyResults.values();
    //Column
  }

  @Test
  public void testFields() {
    Field[] fields = DailyResult.class.getDeclaredFields();

    System.out.println("Number of fields = " + fields.length);

    for (Field field : fields) {
      System.out.println("Field name = " + field.getName()+", type = " + field.getType());
      if (field.getType()==LocalDate.class) {
        System.out.println("date");
      } else if (field.getType()==double.class) {
        System.out.println("double");
      } else if (field.getType() == List.class) {
        System.out.println("list");
      } else if (field.getType() == int.class) {
        System.out.println("int");
      }
    }
  }

  @Test
  public void testCumSum() {
    DoubleColumn doubleColumn = DoubleColumn.create("balance", 6).fillWith(0).set(0, 100D);

    DoubleColumn netPnl = DoubleColumn.create("netPnl", 0,1, 2, 3, 4, 5);
    DoubleColumn new1=doubleColumn.add(netPnl).cumSum();

      Iterator<Double> iterator = new1.iterator();
      while (iterator.hasNext()) {
        System.out.println(iterator.next());
      }
    }

}
