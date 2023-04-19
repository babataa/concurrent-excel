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
    private int age2;
    @ExcelColumn(name = "年龄3", index = 5)
    private int age3;
    @ExcelColumn(name = "年龄4", index = 6, customConvertor = TestCustomConvertor.class)
    private int age4;
    @ExcelColumn(name = "年龄5", index = 7)
    private int age5;
    @ExcelColumn(name = "年龄6", index = 8)
    private int age6;
    @ExcelColumn(name = "年龄7", index = 9)
    private int age7;
    @ExcelColumn(name = "年龄8", index = 10)
    private int age8;
    @ExcelColumn(name = "年龄9", index = 11)
    private int age9;
    @ExcelColumn(name = "年龄10", index = 12)
    private int age10;
    @ExcelColumn(name = "年龄11", index = 13)
    private int age11;
    @ExcelColumn(name = "年龄12", index = 14)
    private int age12;
    @ExcelColumn(name = "年龄13", index = 15)
    private int age13;
    @ExcelColumn(name = "年龄14", index = 16)
    private int age14;
    @ExcelColumn(name = "年龄15", index = 17)
    private int age15;
    @ExcelColumn(name = "年龄16", index = 18)
    private int age16;
    @ExcelColumn(name = "年龄17", index = 19)
    private int age17;
    @ExcelColumn(name = "年龄18", index = 20)
    private int age18;
    @ExcelColumn(name = "年龄19", index = 21)
    private int age19;
    @ExcelColumn(name = "年龄20", index = 22)
    private int age20;
    @ExcelColumn(name = "年龄21", index = 23)
    private int age21;
    @ExcelColumn(name = "年龄22", index = 24)
    private int age22;
    @ExcelColumn(name = "年龄23", index = 25)
    private int age23;

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

    public int getAge2() {
        return age2;
    }

    public void setAge2(int age2) {
        this.age2 = age2;
    }

    public int getAge3() {
        return age3;
    }

    public void setAge3(int age3) {
        this.age3 = age3;
    }

    public int getAge4() {
        return age4;
    }

    public void setAge4(int age4) {
        this.age4 = age4;
    }

    public int getAge5() {
        return age5;
    }

    public void setAge5(int age5) {
        this.age5 = age5;
    }

    public int getAge6() {
        return age6;
    }

    public void setAge6(int age6) {
        this.age6 = age6;
    }

    public int getAge7() {
        return age7;
    }

    public void setAge7(int age7) {
        this.age7 = age7;
    }

    public int getAge8() {
        return age8;
    }

    public void setAge8(int age8) {
        this.age8 = age8;
    }

    public int getAge9() {
        return age9;
    }

    public void setAge9(int age9) {
        this.age9 = age9;
    }

    public int getAge10() {
        return age10;
    }

    public void setAge10(int age10) {
        this.age10 = age10;
    }

    public int getAge11() {
        return age11;
    }

    public void setAge11(int age11) {
        this.age11 = age11;
    }

    public int getAge12() {
        return age12;
    }

    public void setAge12(int age12) {
        this.age12 = age12;
    }

    public int getAge13() {
        return age13;
    }

    public void setAge13(int age13) {
        this.age13 = age13;
    }

    public int getAge14() {
        return age14;
    }

    public void setAge14(int age14) {
        this.age14 = age14;
    }

    public int getAge15() {
        return age15;
    }

    public void setAge15(int age15) {
        this.age15 = age15;
    }

    public int getAge16() {
        return age16;
    }

    public void setAge16(int age16) {
        this.age16 = age16;
    }

    public int getAge17() {
        return age17;
    }

    public void setAge17(int age17) {
        this.age17 = age17;
    }

    public int getAge18() {
        return age18;
    }

    public void setAge18(int age18) {
        this.age18 = age18;
    }

    public int getAge19() {
        return age19;
    }

    public void setAge19(int age19) {
        this.age19 = age19;
    }

    public int getAge20() {
        return age20;
    }

    public void setAge20(int age20) {
        this.age20 = age20;
    }

    public int getAge21() {
        return age21;
    }

    public void setAge21(int age21) {
        this.age21 = age21;
    }

    public int getAge22() {
        return age22;
    }

    public void setAge22(int age22) {
        this.age22 = age22;
    }

    public int getAge23() {
        return age23;
    }

    public void setAge23(int age23) {
        this.age23 = age23;
    }
}
