package com.rspcaassured.ui.Checklist;

public class DropdownValue {
    private int totalCount=0;
    private int totalAns = 0;
    private int percent = 0;


    public DropdownValue() {
    }

    public DropdownValue(int totalCount, int totalAns, int percent) {
        this.totalCount = totalCount;
        this.totalAns = totalAns;
        this.percent = percent;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalAns() {
        return totalAns;
    }

    public void setTotalAns(int totalAns) {
        this.totalAns = totalAns;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    @Override
    public String toString() {
        return "DropdownValue{" +
                "totalCount=" + totalCount +
                ", totalAns=" + totalAns +
                ", percent=" + percent +
                '}';
    }
}

