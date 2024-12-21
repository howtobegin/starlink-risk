package com.liboshuai.starlink.slr.engine.utils.jdbc;

import com.liboshuai.starlink.slr.engine.constants.ParameterConstants;
import com.liboshuai.starlink.slr.engine.utils.parameter.ParameterUtil;
import com.liboshuai.starlink.slr.engine.utils.string.StringUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.utils.ParameterTool;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

@Slf4j
public class JdbcUtil {
    private static HikariDataSource dataSource;

    static {
        try {
            ParameterTool parameterTool = ParameterUtil.getParameters();
            String hostname = parameterTool.get(ParameterConstants.MYSQL_HOSTNAME);
            String port = parameterTool.get(ParameterConstants.MYSQL_PORT);
            String database = parameterTool.get(ParameterConstants.MYSQL_DATABASE);
            String username = parameterTool.get(ParameterConstants.MYSQL_USERNAME);
            String password = parameterTool.get(ParameterConstants.MYSQL_PASSWORD);
            String url = StringUtil.format("jdbc:mysql://{}:{}/{}?serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&useSSL=false",
                    hostname, port, database);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            // 配置连接池的其他参数，例如最大连接数等
            config.setMaximumPoolSize(10);
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            log.error("初始化数据库连接池失败", e);
        }
    }

    /**
     * 从连接池获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 执行更新操作（INSERT、UPDATE、DELETE）
     */
    public static int update(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("执行更新操作失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行批量更新操作
     */
    public static int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Object[] params : batchArgs) {
                setParameters(stmt, params);
                stmt.addBatch();
            }
            return stmt.executeBatch();
        } catch (SQLException e) {
            log.error("执行批量更新操作失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询单个对象
     */
    public static <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> list = queryForList(sql, rowMapper, params);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询列表
     */
    public static <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> result = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                int rowNum = 0;
                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs, rowNum++));
                }
            }
        } catch (SQLException e) {
            log.error("执行查询操作失败", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 查询列表，结果为 List Map 嵌套 的列表
     */
    public static List<Map<String, Object>> queryForListMap(String sql, Object... params) {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            log.error("执行查询操作失败", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 设置 PreparedStatement 的参数
     */
    private static void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        if (params != null)
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
    }

    /**
     * RowMapper 接口，映射行数据到对象
     */
    public interface RowMapper<T> {
        T mapRow(ResultSet rs, int rowNum) throws SQLException;
    }

    /**
     * BeanPropertyRowMapper 实现，自动将列映射到对象属性
     */
    public static class BeanPropertyRowMapper<T> implements RowMapper<T> {
        private final Class<T> mappedClass;
        private final Map<String, Field> fieldMap = new HashMap<>();

        public BeanPropertyRowMapper(Class<T> mappedClass) {
            this.mappedClass = mappedClass;
            initFieldMap();
        }

        private void initFieldMap() {
            Field[] fields = mappedClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                fieldMap.put(field.getName().toLowerCase(), field);
                // 如果需要支持下划线命名，可以在这里转换
                fieldMap.put(convertCamelToUnderscore(field.getName()).toLowerCase(), field);
            }
        }

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T bean;
            try {
                bean = mappedClass.getDeclaredConstructor().newInstance();
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = meta.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    Field field = fieldMap.get(columnName.toLowerCase());
                    if (field != null && value != null) {
                        try {
                            field.set(bean, value);
                        } catch (IllegalAccessException e) {
                            log.error("无法设置属性值: " + field.getName(), e);
                        }
                    }
                }
            } catch (Exception e) {
                throw new SQLException("映射结果集到对象失败", e);
            }
            return bean;
        }

        private String convertCamelToUnderscore(String camelCase) {
            StringBuilder result = new StringBuilder();
            if (camelCase != null && camelCase.length() > 0) {
                result.append(camelCase.charAt(0));
                for (int i = 1; i < camelCase.length(); i++) {
                    char ch = camelCase.charAt(i);
                    if (Character.isUpperCase(ch)) {
                        result.append('_');
                        result.append(Character.toLowerCase(ch));
                    } else {
                        result.append(ch);
                    }
                }
            }
            return result.toString();
        }
    }

    /**
     * 单列行映射器，用于将数据库查询结果中的单个列映射为指定类型对象
     * 此映射器适用于期望从查询结果中提取单一列值并将其转换为特定Java类型的情况
     *
     * @param <T> 指定的类型参数，表示期望转换成的Java类型
     */
    public static class SingleColumnRowMapper<T> implements RowMapper<T> {
        private final Class<T> requiredType;

        public SingleColumnRowMapper(Class<T> requiredType) {
            this.requiredType = requiredType;
        }

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            Object value = rs.getObject(1);
            if (value == null) {
                return null;
            }
            return requiredType.cast(value);
        }
    }

}
