package com.tquant.storage.mapper;

import com.tquant.core.model.data.Tick;
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
 * @date 2022/08/02
 */
@Mapper
public interface TickMapper {

  String ALL_COLUMN = "symbol,volume,amount,latest_price,latest_volume,latest_time,time,type,create_time";

  @Insert("insert into tick("
      + ALL_COLUMN
      + ") value (#{symbol},#{volume},#{amount},#{latestPrice},#{latestVolume},#{latestTime},#{time},#{type},now())")
  void saveTick(Tick tick);

  @Select("select "
      + ALL_COLUMN
      + " from tick where symbol=#{symbol} and latest_time>=#{beginDate} and time<=#{endDate}")
  List<Tick> queryTicksByDate(@Param("symbol") String symbol, @Param("beginDate") LocalDateTime beginDate,
      @Param("endDate") LocalDateTime endDate);
}
