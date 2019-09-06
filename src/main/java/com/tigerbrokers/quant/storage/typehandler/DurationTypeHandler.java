package com.tigerbrokers.quant.storage.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/09/03
 */
@MappedJdbcTypes(JdbcType.BIGINT)
public class DurationTypeHandler extends BaseTypeHandler<Duration> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Duration parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setLong(i, parameter.getSeconds());
  }

  @Override
  public Duration getNullableResult(ResultSet rs, String columnName) throws SQLException {
    long seconds = rs.getLong(columnName);
    if (rs.wasNull()) {
      return null;
    } else {
      return Duration.ofSeconds(seconds);
    }
  }

  @Override
  public Duration getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    long seconds = rs.getLong(columnIndex);
    if (rs.wasNull()) {
      return null;
    } else {
      return Duration.ofSeconds(seconds);
    }
  }

  @Override
  public Duration getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    long seconds = cs.getLong(columnIndex);
    if (cs.wasNull()) {
      return null;
    } else {
      return Duration.ofSeconds(seconds);
    }
  }
}
