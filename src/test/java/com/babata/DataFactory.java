package com.babata;

import com.babata.concurrent.param.BatchParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataFactory {

    public static Date date = new Date();

    public static List<ExportModel> get(BatchParam batchParam, int count) {
        List<ExportModel> list = new ArrayList<>();
        for (int i = batchParam.getBatchStart(); i < Math.min(batchParam.getBatchStart() + batchParam.getBatchSize(), count); i++) {
            ExportModel model = new ExportModel();
            model.setAge(i);
            model.setName(String.valueOf(i));
            model.setSex(String.valueOf(i));
            model.setDate(date);
            model.setAge2(i);
            model.setAge3(i);
            model.setAge4(i);
            model.setAge5(i);
            model.setAge6(i);
            model.setAge7(i);
            model.setAge8(i);
            model.setAge9(i);
            model.setAge10(i);
            model.setAge11(i);
            model.setAge12(i);
            model.setAge13(i);
            model.setAge14(i);
            model.setAge15(i);
            model.setAge16(i);
            model.setAge17(i);
            model.setAge18(i);
            model.setAge19(i);
            model.setAge20(i);
            model.setAge21(i);
            model.setAge22(i);
            model.setAge23(i);
            list.add(model);
        }
        return list;
    }
}
