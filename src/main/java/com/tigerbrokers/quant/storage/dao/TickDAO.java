package com.tigerbrokers.quant.storage.dao;

import com.tigerbrokers.quant.model.data.Tick;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Description:
 * todo 实现tick查询
 *
 * @author kevin
 * @date 2022/08/02
 */
public class TickDAO extends BaseDAO  {

  public List<Tick> queryTicks(String symbol, LocalDateTime start, LocalDateTime end) {
    return null;
  }
}
