package com.tigerbrokers.quant.storage.dao;

import com.tigerbrokers.quant.TigerQuantException;
import com.tigerbrokers.quant.config.ConfigLoader;
import java.io.IOException;
import java.io.InputStream;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/09/02
 */
public class BaseDAO {

  private static SqlSessionFactory sqlSessionFactory;
  private static Boolean storageEnabled = (Boolean) ConfigLoader.GLOBAL_SETTINGS.get("storage.enable");

  static {
    if (storageEnabled != null && storageEnabled) {
      String resource = "datasource/mybatis-config.xml";
      InputStream inputStream;
      try {
        inputStream = Resources.getResourceAsStream(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
      } catch (IOException e) {
        throw new TigerQuantException("load mybatis config error:" + e.getMessage());
      }
    }
  }

  private static SqlSessionFactory getSqlSessionFactory() {
    if (sqlSessionFactory == null) {
      throw new TigerQuantException("[storage.enable] switch is not configured in global_setting.json");
    }
    return sqlSessionFactory;
  }

  public SqlSession openSession() {
    return getSqlSessionFactory().openSession();
  }

  public void closeSession(SqlSession sqlSession) {
    if (sqlSession != null) {
      try {
        sqlSession.close();
      } catch (Exception e) {
        System.err.println("failed to close sqlSession:" + e.getMessage());
        throw e;
      }
    }
  }
}
