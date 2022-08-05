package com.tquant.backtester;

import org.junit.Test;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

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


}
