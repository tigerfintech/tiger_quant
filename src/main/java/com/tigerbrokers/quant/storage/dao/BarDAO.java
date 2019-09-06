package com.tigerbrokers.quant.storage.dao;

import com.tigerbrokers.quant.model.data.Bar;
import com.tigerbrokers.quant.storage.mapper.BarMapper;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/09/02
 */
public class BarDAO extends BaseDAO {

  public void saveBar(Bar bar) {
    SqlSession sqlSession = openSession();
    sqlSession.getMapper(BarMapper.class).saveBar(bar);
    sqlSession.commit();
    sqlSession.close();
  }

  public List<Bar> queryBar(String symbol, int limit) {
    SqlSession sqlSession = openSession();
    List<Bar> bars = sqlSession.getMapper(BarMapper.class).queryBars(symbol, limit);
    sqlSession.commit();
    sqlSession.close();
    return bars;
  }

  public List<Bar> queryBar(String symbol, LocalDate beginDate, LocalDate endDate) {
    SqlSession sqlSession = openSession();
    List<Bar> bars = sqlSession.getMapper(BarMapper.class).queryBarsByDate(symbol, beginDate, endDate);
    sqlSession.commit();
    sqlSession.close();
    return bars;
  }
}
