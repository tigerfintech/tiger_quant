package com.tigerbrokers.quant.storage.dao;

import com.tigerbrokers.quant.model.data.Bar;
import com.tigerbrokers.quant.storage.mapper.BarMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

/**
 * Description:
 * todo 优化SqlSession，保证线程安全
 *
 * @author kevin
 * @date 2019/09/02
 */
public class BarDAO extends BaseDAO {

  private static SqlSession sqlSession = new BaseDAO().openSession();

  public void saveBar(Bar bar) {
    sqlSession.getMapper(BarMapper.class).saveBar(bar);
    sqlSession.commit();
  }

  public List<Bar> queryBar(String symbol, int limit) {
    return sqlSession.getMapper(BarMapper.class).queryBars(symbol, limit);
  }

  public List<Bar> queryBar(String symbol, String period, LocalDateTime beginDate, LocalDateTime endDate) {
    return sqlSession.getMapper(BarMapper.class).queryBarsByDate(symbol, period, beginDate, endDate);
  }
}
