package com.liboshuai.starlink.slr.engine.utils.jdbc;

import com.liboshuai.starlink.slr.engine.constants.ParameterConstants;
import com.liboshuai.starlink.slr.engine.utils.parameter.ParameterUtil;
import com.liboshuai.starlink.slr.engine.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author liboshuai
 * @Date 2023/10/29 23:31
 */
@Slf4j
public class JdbcUtil {

    private static String url;
    private static String username;
    private static String password;


    static {
        try {
            ParameterTool parameterTool = ParameterUtil.getParameters();
            String hostname = parameterTool.get(ParameterConstants.MYSQL_HOSTNAME);
            String port = parameterTool.get(ParameterConstants.MYSQL_PORT);
            String database = parameterTool.get(ParameterConstants.MYSQL_DATABASE);
            username = parameterTool.get(ParameterConstants.MYSQL_USERNAME);
//            password = CryptoUtils.decrypt(parameterTool.get(ParameterConstants.MYSQL_PASSWORD));
            password = parameterTool.get(ParameterConstants.MYSQL_PASSWORD);
            url = StringUtil.format("jdbc:mysql://{}:{}/{}?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=false",
                    hostname, port, database);
        } catch (Exception e) {
            log.error("MySQL JDBC 初始化数据连接配置失败", e);
        }

    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void close(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            // Log exception
        }
    }

    public static <T> T queryOne(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = queryList(sql, rowMapper, params);
        if (results != null) {
            return results.isEmpty() ? null : results.get(0);
        } else {
            return null;
        }
    }

    public static <T> List<T> queryList(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setParameters(statement, params);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        results.add(rowMapper.mapRow(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            log.error("MySQL JDBC 查询失败", e);
            return null;
        }
        return results;
    }

    public static int update(String sql, Object... params) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                setParameters(statement, params);
                return statement.executeUpdate();
            }
        } catch (SQLException e) {
            // Log exception
            return 0;
        }
    }

    private static void setParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    public interface RowMapper<T> {
        T mapRow(ResultSet resultSet) throws SQLException;
    }

    public static class BeanPropertyRowMapper<T> implements RowMapper<T> {

        private final Class<T> mappedClass;

        public BeanPropertyRowMapper(Class<T> mappedClass) {
            this.mappedClass = mappedClass;
        }

        @Override
        public T mapRow(ResultSet resultSet) throws SQLException {
            T bean;
            try {
                bean = mappedClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new SQLException("Failed to create new instance of " + mappedClass.getName(), e);
            }

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object columnValue = resultSet.getObject(i);

                // Convert column value to property value
                columnValue = convertColumnValue(columnValue);

                // Convert column name from underscore to camel case
                String propertyName = convertUnderscoreToCamel(columnName);

                // Use Java reflection to set the property value
                try {
                    java.lang.reflect.Field field = bean.getClass().getDeclaredField(propertyName);
                    field.setAccessible(true);
                    field.set(bean, columnValue);
                } catch (Exception e) {
                    throw new SQLException("Failed to set property " + propertyName + " on " + mappedClass.getName(), e);
                }
            }

            return bean;
        }

        private String convertUnderscoreToCamel(String name) {
            StringBuilder result = new StringBuilder();
            boolean nextIsUpper = false;
            for (int i = 0; i < name.length(); i++) {
                char ch = name.charAt(i);
                if (ch == '_') {
                    nextIsUpper = true;
                } else if (nextIsUpper) {
                    result.append(Character.toUpperCase(ch));
                    nextIsUpper = false;
                } else {
                    result.append(Character.toLowerCase(ch));
                }
            }
            return result.toString();
        }

        private Object convertColumnValue(Object columnValue) {
            if (columnValue == null) {
                return null;
            } else if (columnValue instanceof BigInteger) {
                // Convert BigInteger to Long
                return ((BigInteger) columnValue).longValue();
            } else if (columnValue instanceof LocalDateTime) {
                // Convert LocalDateTime to java.util.Date
                return Date.from(((LocalDateTime) columnValue).atZone(ZoneId.systemDefault()).toInstant());
            } else if (columnValue instanceof BigDecimal) {
                // Convert BigDecimal to Double
                return ((BigDecimal) columnValue).doubleValue();
            } else if (columnValue instanceof Integer) {
                // MySQL TINYINT, SMALLINT, MEDIUMINT, INT or INTEGER can be java.lang.Integer
                return columnValue;
            } else if (columnValue instanceof Double) {
                // MySQL FLOAT, DOUBLE can be java.lang.Double
                return columnValue;
            } else if (columnValue instanceof Byte[]) {
                // MySQL BINARY, VARBINARY, TINYBLOB, BLOB, MEDIUMBLOB, LONGBLOB can be byte[]
                return columnValue;
            } else if (columnValue instanceof Boolean) {
                // MySQL BIT, BOOL, BOOLEAN can be java.lang.Boolean
                return columnValue;
            } else if (columnValue instanceof String) {
                // MySQL CHAR, VARCHAR, TINYTEXT, TEXT, MEDIUMTEXT, LONGTEXT, ENUM, SET can be java.lang.String
                return columnValue;
            } else if (columnValue instanceof Date) {
                // MySQL YEAR, DATE can be java.sql.Date
                return columnValue;
            } else if (columnValue instanceof Time) {
                // MySQL TIME can be java.sql.Time
                return columnValue;
            } else if (columnValue instanceof Timestamp) {
                // MySQL DATETIME, TIMESTAMP can be java.sql.Timestamp
                return columnValue;
            }
            // Add more conversions as needed

            // If no conversion is needed, return the value as is
            return columnValue;
        }
    }
}
