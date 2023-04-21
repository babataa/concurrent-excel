package com.babata;

import com.babata.concurrent.excel.model.ExcelExportAble;
import com.babata.concurrent.excel.resolve.annotation.ExcelColumn;
import com.babata.concurrent.excel.resolve.annotation.TableName;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import java.util.Date;

/**
 * @author zqj
 */
@TableName("测试表格")
public class ExportModel implements ExcelExportAble {

    @ExcelColumn(name = "姓名", index = 0)
    private String name;
    @ExcelColumn(name = "性别", index = 1)
    private String sex;
    @NumberFormat(pattern = "0.00")
    @ExcelColumn(name = "年龄", index = 2)
    private int age;
    @ExcelColumn(name = "生日", index = 3)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;
    @ExcelColumn(name = "年龄2", index = 4, customConvertor = TestCustomConvertor.class)
    private String age2;
    @ExcelColumn(name = "年龄3", index = 5)
    private String age3;
    @ExcelColumn(name = "年龄4", index = 6, customConvertor = TestCustomConvertor.class)
    private String age4;
    @ExcelColumn(name = "年龄5", index = 7)
    private String age5;
    @ExcelColumn(name = "年龄6", index = 8)
    private String age6;
    @ExcelColumn(name = "年龄7", index = 9)
    private String age7;
    @ExcelColumn(name = "年龄8", index = 10)
    private String age8;
    @ExcelColumn(name = "年龄9", index = 11)
    private String age9;
    @ExcelColumn(name = "年龄10", index = 12)
    private String age10;
    @ExcelColumn(name = "年龄11", index = 13)
    private String age11;
    @ExcelColumn(name = "年龄12", index = 14)
    private String age12;
    @ExcelColumn(name = "年龄13", index = 15)
    private String age13;
    @ExcelColumn(name = "年龄14", index = 16)
    private String age14;
    @ExcelColumn(name = "年龄15", index = 17)
    private String age15;
    @ExcelColumn(name = "年龄16", index = 18)
    private String age16;
    @ExcelColumn(name = "年龄17", index = 19)
    private String age17;
    @ExcelColumn(name = "年龄18", index = 20)
    private String age18;
    @ExcelColumn(name = "年龄19", index = 21)
    private String age19;
    @ExcelColumn(name = "年龄20", index = 22)
    private String age20;
    @ExcelColumn(name = "年龄21", index = 23)
    private String age21;
    @ExcelColumn(name = "年龄22", index = 24)
    private String age22;
    @ExcelColumn(name = "年龄23", index = 25)
    private String age23;
    @ExcelColumn(name = "年龄24", index = 26)
    private String age24;
    @ExcelColumn(name = "年龄25", index = 27)
    private String age25;
    @ExcelColumn(name = "年龄26", index = 28)
    private String age26;
    @ExcelColumn(name = "年龄27", index = 29)
    private String age27;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAge2() {
        return age2;
    }

    public void setAge2(String age2) {
        this.age2 = age2;
    }

    public String getAge3() {
        return age3;
    }

    public void setAge3(String age3) {
        this.age3 = age3;
    }

    public String getAge4() {
        return age4;
    }

    public void setAge4(String age4) {
        this.age4 = age4;
    }

    public String getAge5() {
        return age5;
    }

    public void setAge5(String age5) {
        this.age5 = age5;
    }

    public String getAge6() {
        return age6;
    }

    public void setAge6(String age6) {
        this.age6 = age6;
    }

    public String getAge7() {
        return age7;
    }

    public void setAge7(String age7) {
        this.age7 = age7;
    }

    public String getAge8() {
        return age8;
    }

    public void setAge8(String age8) {
        this.age8 = age8;
    }

    public String getAge9() {
        return age9;
    }

    public void setAge9(String age9) {
        this.age9 = age9;
    }

    public String getAge10() {
        return age10;
    }

    public void setAge10(String age10) {
        this.age10 = age10;
    }

    public String getAge11() {
        return age11;
    }

    public void setAge11(String age11) {
        this.age11 = age11;
    }

    public String getAge12() {
        return age12;
    }

    public void setAge12(String age12) {
        this.age12 = age12;
    }

    public String getAge13() {
        return age13;
    }

    public void setAge13(String age13) {
        this.age13 = age13;
    }

    public String getAge14() {
        return age14;
    }

    public void setAge14(String age14) {
        this.age14 = age14;
    }

    public String getAge15() {
        return age15;
    }

    public void setAge15(String age15) {
        this.age15 = age15;
    }

    public String getAge16() {
        return age16;
    }

    public void setAge16(String age16) {
        this.age16 = age16;
    }

    public String getAge17() {
        return age17;
    }

    public void setAge17(String age17) {
        this.age17 = age17;
    }

    public String getAge18() {
        return age18;
    }

    public void setAge18(String age18) {
        this.age18 = age18;
    }

    public String getAge19() {
        return age19;
    }

    public void setAge19(String age19) {
        this.age19 = age19;
    }

    public String getAge20() {
        return age20;
    }

    public void setAge20(String age20) {
        this.age20 = age20;
    }

    public String getAge21() {
        return age21;
    }

    public void setAge21(String age21) {
        this.age21 = age21;
    }

    public String getAge22() {
        return age22;
    }

    public void setAge22(String age22) {
        this.age22 = age22;
    }

    public String getAge23() {
        return age23;
    }

    public void setAge23(String age23) {
        this.age23 = age23;
    }

    public String getAge24() {
        return age24;
    }

    public void setAge24(String age24) {
        this.age24 = age24;
    }

    public String getAge25() {
        return age25;
    }

    public void setAge25(String age25) {
        this.age25 = age25;
    }

    public String getAge26() {
        return age26;
    }

    public void setAge26(String age26) {
        this.age26 = age26;
    }

    public String getAge27() {
        return age27;
    }

    public void setAge27(String age27) {
        this.age27 = age27;
    }
}
