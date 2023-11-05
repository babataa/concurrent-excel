package com.babata;

import com.babata.concurrent.support.BatchParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataFactory {

    public static Date date = new Date();

    public static List<ExportModel> get(BatchParam batchParam, int count) {
        List<ExportModel> list = new ArrayList<>();
        for (int i = batchParam.getBatchStart(); i < Math.min(batchParam.getBatchStart() + batchParam.getBatchSize(), count); i++) {
            /*if(i == 8737) {
                int a = 1/0;
            }*/
            ExportModel model = new ExportModel();
            model.setAge(i);
            model.setName(i + "$Name");
            model.setSex(i + "$Sex");
            model.setDate(date);
            model.setAge2(i + "$Age2");
            model.setAge3(i + "$Age3");
            model.setAge4(i + "$Age4");
            model.setAge5(i + "$Age5");
            model.setAge6(i + "$Age6");
            model.setAge7(i + "$Age7");
            model.setAge8(i + "$Age8");
            model.setAge9(i + "$Age9");
            model.setAge10(i + "$Age10");
            model.setAge11(i + "$Age12");
            model.setAge12(i + "$Age13");
            model.setAge13(i + "$Age14");
            model.setAge14(i + "$Age15");
            model.setAge15(i + "$Age16");
            model.setAge16(i + "$Age18");
            model.setAge17(i + "$Age19");
            model.setAge18(i + "$Age20");
            model.setAge19(i + "$Age21");
            model.setAge20(i + "$Age22");
            model.setAge21(i + "$Age23");
            model.setAge22(i + "$Age24");
            model.setAge23(i + "$Age25");
            model.setAge24(i + "$Age26");
            model.setAge25(i + "$Age27");
            model.setAge26(i + "$Age28");
            model.setAge27(i + "$Age29");
            list.add(model);
        }
        return list;
    }
}
