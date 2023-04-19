package com.babata.concurrent.excel.resolve;

import com.babata.concurrent.excel.model.ExcelExportAble;
import com.babata.concurrent.excel.resolve.annotation.ExcelColumn;
import com.babata.concurrent.excel.resolve.annotation.TableName;
import com.babata.concurrent.util.DateUtil;
import com.babata.concurrent.util.NumberUtil;
import com.babata.concurrent.util.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    public static Map<Integer, ColumnContext> buildHeadMap(Class<? extends ExcelExportAble> excelBean) {
        Map<Integer, ColumnContext> headMap = new HashMap<>(128);
        TableName tableName = excelBean.getAnnotation(TableName.class);
        ColumnContext tableContext = new ColumnContext();
        if(tableName != null && StringUtils.isNotBlank(tableName.value())) {
            tableContext.name = tableName.value();
        } else {
            tableContext.name = "表格" + UUID.randomUUID();
        }
        headMap.put(-1, tableContext);
        Field[] declaredFields = excelBean.getDeclaredFields();
        for (Field field : declaredFields) {
            ExcelColumn excelColumn = field.getAnnotation(ExcelColumn.class);
            if(excelColumn != null) {
                //自定义转换器
                CustomConvertor customConvertor = getCustomConvertorInstance(excelColumn.customConvertor());
                //解析日期格式
                DateTimeFormat dateTimeFormat = field.getAnnotation(DateTimeFormat.class);
                //格式化数字类型
                NumberFormat numberFormat = field.getAnnotation(NumberFormat.class);
                field.setAccessible(true);
                headMap.put(excelColumn.index(), new ColumnContext(excelColumn.name(), o -> {
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
