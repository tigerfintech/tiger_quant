package com.tigerbrokers.quant.storage.mapper;

import com.tigerbrokers.quant.model.data.Bar;
import java.time.LocalDate;
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

  @Insert("insert into bar(symbol,open,high,low,close,volume,duration,amount,create_time) value (#{symbol},#{open},#{high},#{low},#{close},#{volume},#{duration},#{amount},now())")
  void saveBar(Bar bar);

  @Select("select symbol,open,high,low,close,volume,duration,amount from bar where symbol=#{symbol} limit #{limit}")
  List<Bar> queryBars(@Param("symbol") String symbol, @Param("limit") int limit);

  @Select("select symbol,open,high,low,close,volume,duration,amount from bar where symbol=#{symbol} and create_time>=#{beginDate} and create_time<=#{endDate}")
  List<Bar> queryBarsByDate(@Param("symbol") String symbol, @Param("beginDate") LocalDate beginDate,
      @Param("endDate") LocalDate endDate);
}
