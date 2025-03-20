package com.liboshuai.slr.server.biz.util.mock;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 将 MySQL 表结构导出为包含多个工作表的单个 Excel 文件
 */
public class MultiSheetTableSchemaExporter {

    // MySQL 连接信息
    private static final String URL = "jdbc:mysql://xxxxx:xxxxx/xxxxx?serverTimezone=UTC&useSSL=false&characterEncoding=utf8";
    private static final String USER = "xxxxx";
    private static final String PASSWORD = "xxxxx";

    // Excel 输出文件路径
    private static final String OUTPUT_FILE = "./南极星系统字典表.xlsx";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            List<String> allTables = getAllTables(conn);
            exportAllTablesToSingleExcel(conn, allTables);
            System.out.println("All tables exported successfully into one Excel file!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> getAllTables(Connection connection) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE()";
        try (PreparedStatement pstmt = connection.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        }
        return tableNames;
    }

    /**
     * 将多个表的结构信息导出到一个 Excel 文件，并为每个表创建一个 Sheet
     */
    private static void exportAllTablesToSingleExcel(Connection conn, List<String> allTables) throws SQLException {
        new File(OUTPUT_FILE).delete(); // 先删除旧文件，避免冲突

        // 遍历所有表，获取表结构信息
        try (com.alibaba.excel.ExcelWriter excelWriter = EasyExcel.write(OUTPUT_FILE).registerWriteHandler(new CustomCellWriteHandler()).build()) {
            for (String tableName : allTables) {
                List<List<String>> data = getTableSchemaWithHeader(conn, tableName); // 获取表结构（包含表名作为首行）
                WriteSheet writeSheet = EasyExcel.writerSheet(tableName).head((List<List<String>>) null).build(); // Sheet 名称
                excelWriter.write(data, writeSheet); // 写入数据
            }
        }

        System.out.println("Excel generated: " + OUTPUT_FILE);
    }

    /**
     * 获取表的列信息，并在第一行添加表名作为表头
     */
    private static List<List<String>> getTableSchemaWithHeader(Connection conn, String tableName) throws SQLException {
        List<List<String>> data = new ArrayList<>();

        // 获取表的注释
        String tableComment = getTableComment(conn, tableName);

        // 第一行是表名（表头）
        List<String> tableHeader = new ArrayList<>();
        tableHeader.add(tableName + "（" + tableComment + "）");
        data.add(tableHeader);

        // 查询表的字段信息
        String sql = "SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                "ORDER BY ORDINAL_POSITION";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tableName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                row.add(rs.getString("COLUMN_NAME"));
                row.add(rs.getString("COLUMN_TYPE"));
                row.add(rs.getString("COLUMN_COMMENT") != null ? rs.getString("COLUMN_COMMENT") : "");
                data.add(row);
            }
        }
        return data;
    }

    /**
     * 获取表的注释（表备注信息）
     */
    private static String getTableComment(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tableName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("TABLE_COMMENT");
            }
        }
        return "";
    }
}

/**
 * 表字段信息实体类（仅用于存储字段信息）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class TableColumn {
    private String columnName;
    private String columnType;
    private String columnComment;
}

class CustomCellWriteHandler implements CellWriteHandler {
    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        Sheet sheet = cell.getSheet();
        Workbook workbook = sheet.getWorkbook();
        if (cell.getRowIndex() == 0) {
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font font = workbook.createFont();
            font.setBold(true);
            cellStyle.setFont(font);
            Cell firstCell = sheet.getRow(0).getCell(0);
            if (firstCell != null) {
                firstCell.setCellStyle(cellStyle);
            }
        }
    }
}