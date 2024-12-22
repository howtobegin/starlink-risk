package com.liboshuai.starlink;

import com.liboshuai.starlink.slr.engine.utils.JdbcUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JdbcUtilTest {

    /**
     * 在每个测试方法之前执行，初始化测试数据
     */
    @Before
    public void setUp() {
        String sql = "INSERT INTO users (name, age) VALUES (?, ?)";
        JdbcUtil.update(sql, "Alice", 25);
        JdbcUtil.update(sql, "Bob", 30);
        JdbcUtil.update(sql, "Charlie", 35);
        System.out.println("-----测试数据已初始化-----");
    }

    /**
     * 在每个测试方法之后执行，清理测试数据
     */
    @After
    public void tearDown() {
        String sql = "DELETE FROM users";
        JdbcUtil.update(sql);
        System.out.println("-----测试数据已清理-----");
    }

    /**
     * 测试查询单个对象
     */
    @Test
    public void testQueryForListForObject() {
        String sql = "SELECT id, name, age FROM users WHERE name = ?";
        User user = JdbcUtil.queryForObject(sql, new JdbcUtil.BeanPropertyRowMapper<>(User.class), "Alice");
        assertNotNull(user);
        assertEquals("Alice", user.getName());
        System.out.println("testQueryForObject: " + user);
    }

    /**
     * 测试查询列表
     */
    @Test
    public void testQueryForList() {
        String sql = "SELECT id, name, age FROM users WHERE age > ?";
        List<User> users = JdbcUtil.queryForList(sql, new JdbcUtil.BeanPropertyRowMapper<>(User.class), 28);
        assertEquals(2, users.size());
        for (User user : users) {
            System.out.println("testQuery: " + user);
        }
    }

    /**
     * 测试更新操作
     */
    @Test
    public void testUpdate() {
        String sql = "UPDATE users SET age = ? WHERE name = ?";
        int rowsAffected = JdbcUtil.update(sql, 26, "Alice");
        assertEquals(1, rowsAffected);

        // 验证更新结果
        String querySql = "SELECT id, name, age FROM users WHERE name = ?";
        User user = JdbcUtil.queryForObject(querySql, new JdbcUtil.BeanPropertyRowMapper<>(User.class), "Alice");
        assertNotNull(user);
        assertEquals((Integer) 26, user.getAge());
        System.out.println("testUpdate: " + user);
    }

    /**
     * 测试批量更新操作
     */
    @Test
    public void testBatchUpdate() {
        String sql = "INSERT INTO users (name, age) VALUES (?, ?)";
        List<Object[]> batchArgs = Arrays.asList(
                new Object[]{"David", 40},
                new Object[]{"Eve", 45}
        );
        int[] counts = JdbcUtil.batchUpdate(sql, batchArgs);
        assertEquals(2, counts.length);
        System.out.println("testBatchUpdate: Rows affected - " + Arrays.toString(counts));

        // 验证插入结果
        String querySql = "SELECT id, name, age FROM users WHERE name IN (?, ?)";
        List<User> users = JdbcUtil.queryForList(querySql, new JdbcUtil.BeanPropertyRowMapper<>(User.class), "David", "Eve");
        assertEquals(2, users.size());
        for (User user : users) {
            System.out.println("testBatchUpdate: " + user);
        }
    }

    /**
     * 测试查询列表，结果为 Map 的列表
     */
    @Test
    public void testQueryForListForListMap() {
        String sql = "SELECT * FROM users";
        List<Map<String, Object>> users = JdbcUtil.queryForListMap(sql);
        assertEquals(3, users.size());
        for (Map<String, Object> user : users) {
            System.out.println("testQueryForList: " + user);
        }
    }

    /**
     * 用户实体类
     */
    public static class User {
        private Integer id;
        private String name;
        private Integer age;

        // Getters 和 Setters

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        // toString 方法

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
