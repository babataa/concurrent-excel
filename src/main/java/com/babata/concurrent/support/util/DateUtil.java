package com.babata.concurrent.support.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期工具类
 * @author: zqj
 */
public class DateUtil {

    private static final Map<String, DateTimeFormatter> DATE_FORMAT = new HashMap<>();

    /**
     * 格式化日期
     * @param date
     * @param pattern
     * @return
     */
    public static String getFormatDate(Date date, String pattern) {
        if(null == date) {
            return null;
        }
        DateTimeFormatter dateTimeFormatter = DATE_FORMAT.get(pattern);
        if(dateTimeFormatter == null) {
            synchronized (DATE_FORMAT) {
                dateTimeFormatter = DATE_FORMAT.computeIfAbsent(pattern, key -> DateTimeFormatter.ofPattern(key));
            }
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(dateTimeFormatter);
    }

    public static Date parseDate(String dateStr, String pattern) {
        if(StringUtils.isBlank(dateStr)) {
            return null;
        }
        DateTimeFormatter dateTimeFormatter = DATE_FORMAT.get(pattern);
        if(dateTimeFormatter == null) {
            synchronized (DATE_FORMAT) {
                dateTimeFormatter = DATE_FORMAT.computeIfAbsent(pattern, key -> DateTimeFormatter.ofPattern(key));
            }
        }
        return Date.from(LocalDateTime.parse(dateStr, dateTimeFormatter).atZone(ZoneId.systemDefault()).toInstant());
    }
}
