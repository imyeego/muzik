package com.liuzhao.muzik.model.bean;

import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;

@SmartTable(name = "试卷详情")
public class Exam {
    @SmartColumn(id = 1, name = "考试科目")
    private String km;
    @SmartColumn(id = 2, name = "考场数")
    private int kcCount;
    @SmartColumn(id = 3, name = "试卷袋数")
    private int bagCount;
    @SmartColumn(id = 4, name = "备用试卷袋数")
    private int sparedPaperCount;
    @SmartColumn(id = 5, name = "备用答题卡袋数")
    private int sparedCardCount;
    @SmartColumn(id = 6, name = "合计")
    private int total;

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public int getKcCount() {
        return kcCount;
    }

    public void setKcCount(int kcCount) {
        this.kcCount = kcCount;
    }

    public int getBagCount() {
        return bagCount;
    }

    public void setBagCount(int bagCount) {
        this.bagCount = bagCount;
    }

    public int getSparedPaperCount() {
        return sparedPaperCount;
    }

    public void setSparedPaperCount(int sparedPaperCount) {
        this.sparedPaperCount = sparedPaperCount;
    }

    public int getSparedCardCount() {
        return sparedCardCount;
    }

    public void setSparedCardCount(int sparedCardCount) {
        this.sparedCardCount = sparedCardCount;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
