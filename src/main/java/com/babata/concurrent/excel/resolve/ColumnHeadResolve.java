package com.babata.concurrent.excel.resolve;

import com.babata.concurrent.excel.model.ExcelExportAble;
import com.babata.concurrent.excel.model.ExcelImportAble;
import com.babata.concurrent.excel.resolve.annotation.ExcelColumn;
import com.babata.concurrent.excel.resolve.annotation.TableName;
import com.babata.concurrent.support.util.DateUtil;
import com.babata.concurrent.support.util.NumberUtil;
import com.babata.concurrent.support.util.ReflexUtil;
import com.babata.concurrent.support.util.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.lang.reflect.Field;
import java.util.*;

/**
 * excel列名和格式转换解析
 * @author: zqj
 */
public class ColumnHeadResolve {

    private static final Map<Class<? extends CustomConvertor>, CustomConvertor> customConvertorMap = new HashMap<>();

    /**
     * 解析指定的文件名、列名和格式转换
     * @param excelBean 需要转换的对象
     * @return
     */
    public static ExcelContext buildHeadMap(Class<? extends ExcelExportAble> excelBean) {
        TreeMap<Integer, ColumnContext> headMap = new TreeMap<>();
        Field[] declaredFields = excelBean.getDeclaredFields();
        int maxIndex = 0;
        for (Field field : declaredFields) {
            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            if(excelColumn != null) {
                int index = excelColumn.index();
                if(maxIndex < index) {
                    maxIndex = index;
                }
                //自定义转换器
                CustomConvertor customConvertor = getCustomConvertorInstance(excelColumn.customConvertor());
                //解析日期格式
                DateTimeFormat dateTimeFormat = field.getAnnotation(DateTimeFormat.class);
                //格式化数字类型
                NumberFormat numberFormat = field.getAnnotation(NumberFormat.class);
                field.setAccessible(true);
                headMap.put(index, new ColumnContext(excelColumn.name(), o -> {
                    Object object;
                    try {
                        object = field.get(o);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("get field value error");
                    }
                    if(object != null ) {
                        if(customConvertor != null) {
                            //自定义转换
                            return customConvertor.convert(object);
                        }
                        if (dateTimeFormat != null && Date.class.isAssignableFrom(field.getType())) {
                            //格式化日期格式
                            return DateUtil.getFormatDate((Date) object, dateTimeFormat.pattern());
                        }
                        if (numberFormat != null && Number.class.isAssignableFrom(field.getType())) {
                            NumberUtil.formatNumber((Number) object, numberFormat.pattern());
                        }
                    }
                    return object == null?excelColumn.defaultValue():object.toString();
                }));
            }
        }
        //重新排列index
        ExcelContext excelContext = new ExcelContext();
        ColumnContext[] columns = new ColumnContext[maxIndex + 1];
        TableName tableName = excelBean.getAnnotation(TableName.class);
        if(tableName != null && StringUtils.isNotBlank(tableName.value())) {
            excelContext.setTableName(tableName.value());
        } else {
            excelContext.setTableName("表格" + UUID.randomUUID());
        }
        for (int i = 0; i <= maxIndex; i++) {
            columns[i] = headMap.get(i);
        }
        excelContext.setColumns(columns);
        return excelContext;
    }

    public static Map<Object, ColumnContext> buildImportHeadMap(Class<? extends ExcelImportAble> excelBean) {
        Map<Object, ColumnContext> headMap = new HashMap<>();
        Field[] declaredFields = excelBean.getDeclaredFields();
        for (Field field : declaredFields) {
            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            if(excelColumn != null) {
                //自定义转换器
                CustomConvertor customConvertor = getCustomConvertorInstance(excelColumn.customConvertor());
                //解析日期格式
                DateTimeFormat dateTimeFormat = field.getAnnotation(DateTimeFormat.class);
                field.setAccessible(true);
                headMap.put(excelColumn.index() == -1?excelColumn.name():excelColumn.index(), new ColumnContext(excelColumn.name(), (obj, str) -> {
                    Object o = null;
                    if(customConvertor != null) {
                        //自定义转换
                        o = customConvertor.parse(str);
                    } else if(dateTimeFormat != null && Date.class.isAssignableFrom(field.getType())) {
                        //格式化日期格式
                        o = DateUtil.parseDate(str, dateTimeFormat.pattern());
                    } else {
                        o = ReflexUtil.parseObject(str, field.getType());
                    }
                    try {
                        field.set(obj, o);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
        }
        return headMap;
    }

    /**
     * 获取自定格式转换器实例
     * @param clazz
     * @return
     */
    private static CustomConvertor getCustomConvertorInstance(Class<? extends CustomConvertor> clazz) {
        if(clazz == CustomConvertor.class) {
            return null;
        }
        CustomConvertor customConvertor = customConvertorMap.get(clazz);
        if(customConvertor == null) {
            synchronized (customConvertorMap) {
                customConvertor = customConvertorMap.computeIfAbsent(clazz, clazz0 -> {
                    try {
                        return clazz.newInstance();
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        return customConvertor;
    }
}
