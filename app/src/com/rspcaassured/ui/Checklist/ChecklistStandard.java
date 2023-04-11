package com.rspcaassured.ui.Checklist;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.rspcaassured.Utility.AppUtility;

public class ChecklistStandard implements Parcelable {
    public String mChecklistStandardId;
    public String mChecklistStandardSection;
    public int mChecklistStandardSectionOrder;
    public String mChecklistStandardSubSection;
    public int mChecklistStandardOrder;
    public int mChecklistStandardSubSectionOrder;
    public String mChecklistStandardNumber;
    public String mChecklistStandardSummary;
    public String mChecklistStandardCompliance;
    public String addNote;
    public String mChecklistStandardNonCompliance;
    public String mChecklistStandardGuidance;
    public String mChecklistStandardQuestionAnswered;
    public String mChecklistStandardImageUpload;
    public String mEvidence;
    public String mOtherEvidence;
    public String mCorrection;

    public ChecklistStandard(String checklistStandardId, String mChecklistStandardSection, String mChecklistStandardSubSection, String mChecklistStandardNumber, String mChecklistStandardSummary, String mChecklistStandardCompliance, String mChecklistStandardNonCompliance, int mChecklistStandardSectionOrder, int mChecklistStandardSubSectionOrder, int checklistStandardOrder,  String mChecklistStandardGuidance, String mChecklistStandardQuestionAnswered, String imageUpload, String evidence, String otherEvidence, String correction) {
        this.mChecklistStandardId = checklistStandardId;
        this.mChecklistStandardSection = mChecklistStandardSection;
        this.mChecklistStandardSubSection = mChecklistStandardSubSection;
        this.mChecklistStandardNumber = mChecklistStandardNumber;
        this.mChecklistStandardSummary = mChecklistStandardSummary;
        this.mChecklistStandardCompliance = mChecklistStandardCompliance;
        this.mChecklistStandardNonCompliance = mChecklistStandardNonCompliance;
        this.mChecklistStandardSectionOrder = mChecklistStandardSectionOrder;
        this.mChecklistStandardSubSectionOrder = mChecklistStandardSubSectionOrder;
        this.mChecklistStandardOrder = checklistStandardOrder;
        this.mChecklistStandardGuidance = mChecklistStandardGuidance;
        this.mChecklistStandardQuestionAnswered = mChecklistStandardQuestionAnswered;
        this.mChecklistStandardImageUpload = imageUpload;
        this.mEvidence = evidence;
        this.mOtherEvidence = otherEvidence;
        this.mCorrection = correction;
    }


    protected ChecklistStandard(Parcel in) {
        mChecklistStandardId = in.readString();
        mChecklistStandardSection = in.readString();
        mChecklistStandardSectionOrder = in.readInt();
        mChecklistStandardSubSection = in.readString();
        mChecklistStandardSubSectionOrder = in.readInt();
        mChecklistStandardOrder = in.readInt();
        mChecklistStandardNumber = in.readString();
        mChecklistStandardSummary = in.readString();
        mChecklistStandardCompliance = in.readString();
        mChecklistStandardNonCompliance = in.readString();
        mChecklistStandardQuestionAnswered = in.readString();
        mChecklistStandardImageUpload = in.readString();
        mEvidence = in.readString();
        mOtherEvidence = in.readString();
        mCorrection = in.readString();
    }

    public static final Creator<ChecklistStandard> CREATOR = new Creator<ChecklistStandard>() {
        @Override
        public ChecklistStandard createFromParcel(Parcel in) {
            return new ChecklistStandard(in);
        }

        @Override
        public ChecklistStandard[] newArray(int size) {
            return new ChecklistStandard[size];
        }
    };

    public Integer getmChecklistStandardSectionOrder() {
        return mChecklistStandardSectionOrder;
    }

    public void setmChecklistStandardSectionOrder(Integer mChecklistStandardSectionOrder) {
        this.mChecklistStandardSectionOrder = mChecklistStandardSectionOrder;
    }



    public Integer getmChecklistStandardSubSectionOrder() {
        return mChecklistStandardSubSectionOrder;
    }

    public void setmChecklistStandardSubSectionOrder(Integer mChecklistStandardSubSectionOrder) {
        this.mChecklistStandardSubSectionOrder = mChecklistStandardSubSectionOrder;
    }

    public String getmChecklistStandardId() {
        return mChecklistStandardId;
    }

    public String getmChecklistStandardSection() {
        return mChecklistStandardSection;
    }

    public void setmChecklistStandardSection(String mChecklistStandardSection) {
        this.mChecklistStandardSection = mChecklistStandardSection;
    }

    public String getmChecklistStandardSubSection() {
        return mChecklistStandardSubSection;
    }

    public void setmChecklistStandardSubSection(String mChecklistStandardSubSection) {
        this.mChecklistStandardSubSection = mChecklistStandardSubSection;
    }

    public String getmChecklistStandardNumber() {
        return mChecklistStandardNumber;
    }

    public void setmChecklistStandardNumber(String mChecklistStandardNumber) {
        this.mChecklistStandardNumber = mChecklistStandardNumber;
    }

    public String getmChecklistStandardSummary() {
        return mChecklistStandardSummary;
    }

    public void setmChecklistStandardSummary(String mChecklistStandardSummary) {
        this.mChecklistStandardSummary = mChecklistStandardSummary;
    }

    public String getmChecklistStandardCompliance() {
        return mChecklistStandardCompliance;
    }

    public void setmChecklistStandardCompliance(String mChecklistStandardCompliance) {
        this.mChecklistStandardCompliance = mChecklistStandardCompliance;
    }

    public String getAddNote() {
        return this.addNote;
    }

    public void setAddNote(String note) {
        this.addNote = note;
    }

    public String getmChecklistStandardNonCompliance() {
        return mChecklistStandardNonCompliance;
    }

    public void setmChecklistStandardNonCompliance(String mChecklistStandardNonCompliance) {
        this.mChecklistStandardNonCompliance = mChecklistStandardNonCompliance;
    }

    public String getChecklistStandardGuidance(){
        return mChecklistStandardGuidance;
    }

    public Boolean getChecklistStandardQuestionAnswered(){
        return Boolean.valueOf(mChecklistStandardQuestionAnswered);
    }

    public void setChecklistStandardQuestionAnswered(Boolean checklistStandardQuestionAnswered){
        this.mChecklistStandardQuestionAnswered = checklistStandardQuestionAnswered.toString();
    }

    public void setmChecklistStandardImageUploadString(String image){
        this.mChecklistStandardImageUpload = image;
    }
    public void setmChecklistStandardImageUpload(Bitmap image){
        if(image==null){
            this.mChecklistStandardImageUpload = "";
        }else{
            String testImgString = AppUtility.BitMapToString(image);
            Log.d("onCreate", "testImgString : "+testImgString);
            this.mChecklistStandardImageUpload = testImgString;
        }
    }

    public Bitmap getmChecklistStandardImageUpload(){
        if(this.mChecklistStandardImageUpload != null && !this.mChecklistStandardImageUpload.equals("")){
            return AppUtility.StringToBitMap(this.mChecklistStandardImageUpload);
        }else {
            return null;
        }

    }

    public String getmChecklistStandardImageUploadString(){
        return mChecklistStandardImageUpload;
    }

    public Integer getmChecklistStandardOrder() {
        return mChecklistStandardOrder;
    }

    public void setmChecklistStandardOrder(Integer mChecklistStandardOrder) {
        this.mChecklistStandardOrder = mChecklistStandardOrder;
    }

    public String getmEvidence() {
        return mEvidence;
    }

    public void setmEvidence(String mEvidence) {
        this.mEvidence = mEvidence;
    }

    public String getmOtherEvidence() {
        return mOtherEvidence;
    }

    public void setmOtherEvidence(String mOtherEvidence) {
        this.mOtherEvidence = mOtherEvidence;
    }

    public String getmCorrection() {
        return mCorrection;
    }

    public void setmCorrection(String mCorrection) {
        this.mCorrection = mCorrection;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mChecklistStandardId);
        dest.writeString(mChecklistStandardSection);
        dest.writeInt(mChecklistStandardSectionOrder);
        dest.writeString(mChecklistStandardSubSection);
        dest.writeInt(mChecklistStandardSubSectionOrder);
        dest.writeInt(mChecklistStandardOrder);
        dest.writeString(mChecklistStandardNumber);
        dest.writeString(mChecklistStandardSummary);
        dest.writeString(mChecklistStandardCompliance);
        dest.writeString(mChecklistStandardNonCompliance);
        dest.writeString(mChecklistStandardQuestionAnswered);
        dest.writeString(mChecklistStandardImageUpload);
        dest.writeString(mEvidence);
        dest.writeString(mOtherEvidence);
        dest.writeString(mCorrection);
    }
}
