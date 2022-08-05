package com.tigerbrokers.quant.storage.dao;

import com.tigerbrokers.quant.model.data.Tick;
import com.tigerbrokers.quant.storage.mapper.TickMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

/**
 * Description:
 * todo 实现tick查询
 *
 * @author kevin
 * @date 2022/08/02
 */
public class TickDAO extends BaseDAO  {
  private static SqlSession sqlSession = new BaseDAO().openSession();

  public void saveTick(Tick tick) {
    sqlSession.getMapper(TickMapper.class).saveTick(tick);
    sqlSession.commit();
  }

  public List<Tick> queryTicks(String symbol, LocalDateTime start, LocalDateTime end) {
    return sqlSession.getMapper(TickMapper.class).queryTicksByDate(symbol, start, end);
  }
}
