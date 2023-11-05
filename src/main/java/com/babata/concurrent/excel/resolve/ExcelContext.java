package com.babata.concurrent.excel.resolve;

/**
 * excel上下文
 * @author zqj
 */
public class ExcelContext {
    /**
     * 表格名称
     */
    String tableName;
    /**
     * 列
     */
    ColumnContext[] columns;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ColumnContext[] getColumns() {
        return columns;
    }

    public void setColumns(ColumnContext[] columns) {
        this.columns = columns;
    }
}
