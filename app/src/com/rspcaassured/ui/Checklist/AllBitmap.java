package com.rspcaassured.ui.Checklist;

import java.util.List;

public class AllBitmap {
    private List<ChecklistStandardBitmap> bitmapList;

    public AllBitmap() {
    }

    public AllBitmap(List<ChecklistStandardBitmap> bitmapList) {
        this.bitmapList = bitmapList;
    }

    public List<ChecklistStandardBitmap> getBitmapList() {
        return bitmapList;
    }

    public void setBitmapList(List<ChecklistStandardBitmap> bitmapList) {
        this.bitmapList = bitmapList;
    }
}
