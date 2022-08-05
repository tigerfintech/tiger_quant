package com.tquant.storage.mapper;

import com.tquant.core.model.data.Bar;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/09/02
 */
@Mapper
public interface BarMapper {

  String ALL_COLUMN = "symbol,period,open,high,low,close,volume,duration,amount,time,create_time";

  @Insert("insert into bar("+ALL_COLUMN+") value (#{symbol},#{period},#{open},#{high},#{low},#{close},#{volume},#{duration},#{amount},#{time},now())")
  void saveBar(Bar bar);

  @Select("select " + ALL_COLUMN + " from bar where symbol=#{symbol} limit #{limit}")
  List<Bar> queryBars(@Param("symbol") String symbol, @Param("limit") int limit);

  @Select("select "
      + ALL_COLUMN
      + " from bar where symbol=#{symbol} and period=#{period} and time>=#{beginDate} and time<=#{endDate}")
  List<Bar> queryBarsByDate(@Param("symbol") String symbol,@Param("period") String period, @Param("beginDate") LocalDateTime beginDate,
      @Param("endDate") LocalDateTime endDate);
}
