package com.rspcaassured.objects;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.rspcaassured.Utility.CustomDateFormatter;
import com.salesforce.androidsdk.mobilesync.model.SalesforceObject;
import com.salesforce.androidsdk.mobilesync.target.SyncTarget;
import com.salesforce.androidsdk.mobilesync.util.Constants;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple representation of a Application object.
 *
 * @author shahedmiah
 */
public class ApplicationObject extends SalesforceObject{
    public static final String NAME = "Name";
    public static final String SITE_NAME = "Site_Name__c";
    public static final String MEMBERSHIP_NUMBER = "Membership_Number__c";
    public static final String ADDRESS = "Address__c";
    public static final String ASSESSMENT_DATE = "Assessment_Date__c";
    public static final String ASSESSMENT_DEADLINE= "Assessment_Deadline__c";
    public static final String PREV_ASSESSMENT_DATE__C = "Previous_Assessment_Date__c";
    public static final String WHAT3WORDS = "What3Words__c";
    public static final String SITE_PHONE = "Site_Phone__c";
    public static final String APPLICANT_PHONE = "Applicant_Phone__c";
    public static final String LOCATION_NOTES = "Location_Notes__c";
    public static final String APPLICATION_TYPE = "Application_Type__c";
    public static final String CHECKLIST_COMPLETION = "Checklist_Completion__c";
    public static final String SITE_NOTE = "Site_Notes__c";
    public static final String SITE_NOTE_EDIT = "Site_Notes_Update__c";


    private final boolean isLocallyCreated;
    private final boolean isLocallyDeleted;
    private final boolean isLocallyModified;

    /**
     * Parameterized constructor.
     *
     * @param data Raw data.
     */
    public ApplicationObject(JSONObject data) {
        super(data);
        objectType = "Application__c";
        objectId = data.optString(Constants.ID);
        name = data.optString(NAME);
        isLocallyCreated = data.optBoolean(SyncTarget.LOCALLY_CREATED);
        isLocallyDeleted = data.optBoolean(SyncTarget.LOCALLY_DELETED);
        final boolean isLocallyUpdated = data.optBoolean(SyncTarget.LOCALLY_UPDATED);
        isLocallyModified = isLocallyCreated || isLocallyUpdated || isLocallyDeleted;
    }

    /**
     * Returns name of the application.
     *
     * @return name of the application.
     */
    public String getName() {
        return sanitizeText(rawData.optString(NAME));
    }

    /**
     * Returns site name of the application.
     *
     * @return site name of the application.
     */
    public String getSiteName() {
        return sanitizeText(rawData.optString(SITE_NAME));
    }


    /**
     * Returns membership number of the site.
     *
     * @return membership number on the site.
     */
    public String getMembershipNumber() {
        return sanitizeText(rawData.optString(MEMBERSHIP_NUMBER));
    }

    /**
     * Returns site address of the application.
     *
     * @return site address of the application.
     */
    public String getAddress() {
        return sanitizeText(rawData.optString(ADDRESS));
    }

    /**
     * Returns assessment date of the application.
     *
     * @return assessment date of the application.
     */
    public String getAssessmentDate() {
        return sanitizeText(rawData.optString(ASSESSMENT_DATE));
    }

    /**
     * Returns assessment deadline date of the application
     *
     * @return assessment deadline
     * */
    public String getAssessmentDeadline(){
        return sanitizeText(rawData.optString(ASSESSMENT_DEADLINE));
    }

    /**
     * Returns previous assessment date of the site.
     *
     * @return previous assessment of the site.
     */
    public String getPrevAssessmentDate() {
        return sanitizeText(rawData.optString(PREV_ASSESSMENT_DATE__C));
    }

    /**
     * Returns what3words of the site.
     *
     * @return what3words of the site.
     */
    public String getWhat3words() {
        return sanitizeText(rawData.optString(WHAT3WORDS));
    }

    /**
     * Returns phone of the site.
     *
     * @return phone of the site.
     */
    public String getSitePhone() {
        return sanitizeText(rawData.optString(SITE_PHONE));
    }

    /**
     * Returns applicant phone.
     *
     * @return applicant phone.
     */
    public String getApplicantPhone() {
        return sanitizeText(rawData.optString(APPLICANT_PHONE));
    }

    /**
     * Returns location notes of the site.
     *
     * @return location notes.
     */
    public String getLocationNotes() {
        String l_note = sanitizeText(rawData.optString(LOCATION_NOTES));
        Log.d("onCreate", "location note: "+l_note);
        return l_note;
    }

    /**
     * Returns site notes of the site.
     *
     * @return site notes.
     */
    public String getSiteNotes() {
        return sanitizeText(rawData.optString(SITE_NOTE));
    }

    /**
     * Returns edit site notes of the site.
     *
     * @return edit site notes.
     */
    public String getSiteNotesEdit() {
        return sanitizeText(rawData.optString(SITE_NOTE_EDIT));
    }

    /**
     * Returns checklist completion progress for the application.
     *
     * @return checklist completion progress.
     */
    public String getChecklistCompletion() {
        return sanitizeText(rawData.optString(CHECKLIST_COMPLETION));
    }

    /**
     * Returns application type.
     *
     * @return application type.
     */
    public String getApplicationType() {
        return sanitizeText(rawData.optString(APPLICATION_TYPE));
    }

    /**
     * Returns a concatenated assessment name which is the farm name + application type + assessment month + assessment year.
     *
     * @return farm assessment name.
     */
    public String getFarmAssessmentName() {
        String appNo = getName();
        Log.d("onCreate", "appNo: "+appNo);
        String appNumber = getMembershipNumber();
        Log.d("onCreate", "appNumber: "+appNumber);
        String contactNumber = getSitePhone();
        Log.d("onCreate", "contactNumber: "+contactNumber);


        String siteName = getSiteName();
        String applicationType = getApplicationType();
        String assessmentDate = getAssessmentDate();
        String assessmentDateFormatted = CustomDateFormatter.formattedDate(assessmentDate);


        Date assessmentDateValue= null;
        try {
            assessmentDateValue = new SimpleDateFormat("yyyy-MM-dd").parse(assessmentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(assessmentDateValue);
        int monthNumber = cal.get(Calendar.MONTH);

        String monthTxt = getMonthText(monthNumber);
        String yearTxt = String.valueOf(cal.get(Calendar.YEAR));

        //String farmAssessmentName = siteName + " " +  applicationType + " " + monthTxt + " " +  yearTxt;
        String farmAssessmentName = siteName + " , "+assessmentDateFormatted;

        if(appNo.isEmpty()){
            return farmAssessmentName;
        }else{
            return appNo+" , "+farmAssessmentName;
        }
    }

    public String getRenewalDate(){
        String appNo = getName();
        Log.d("onCreate", "appNo: "+appNo);
        String appNumber = getMembershipNumber();
        Log.d("onCreate", "appNumber: "+appNumber);
        String contactNumber = getSitePhone();
        Log.d("onCreate", "contactNumber: "+contactNumber);


        String siteName = getSiteName();
        String applicationType = getApplicationType();
        String assessmentDate = getAssessmentDate();
        String assessmentDateFormatted = CustomDateFormatter.formattedDate(assessmentDate);


        Date assessmentDateValue= null;
        try {
            assessmentDateValue = new SimpleDateFormat("yyyy-MM-dd").parse(assessmentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(assessmentDateValue);
        int monthNumber = cal.get(Calendar.MONTH);

        String monthTxt = getMonthText(monthNumber);
        String yearTxt = String.valueOf(cal.get(Calendar.YEAR));

        //String farmAssessmentName = applicationType + " " + monthTxt + ", " +  yearTxt;
        String farmAssessmentName =  monthTxt + ", " +  yearTxt;

        return farmAssessmentName;
        /*if(appNo.isEmpty()){
            return farmAssessmentName;
        }else{
            return appNo+" , "+farmAssessmentName;
        }*/
    }

    public String getMonthText(int monthNumber){
        String monthText = "";
        switch (monthNumber){
            case 0:
                monthText = "January";
                break;
            case 1:
                monthText = "February";
                break;
            case 2:
                monthText = "March";
                break;
            case 3:
                monthText = "April";
                break;
            case 4:
                monthText = "May";
                break;
            case 5:
                monthText = "June";
            break;
            case 6:
                monthText = "July";
                break;
            case 7:
                monthText = "August";
                break;
            case 8:
                monthText = "September";
                break;
            case 9:
                monthText = "October";
                break;
            case 10:
                monthText = "November";
                break;
            case 11:
                monthText = "December";
                break;
            default: monthText = "Invalid month";
                break;

        }
        return monthText;
    }


    /**
     * Returns whether the application has been locally modified or not.
     *
     * @return True - if the application has been locally modified, False - otherwise.
     */
    public boolean isLocallyModified() {
        return isLocallyModified;
    }

    /**
     * Returns whether the application has been locally deleted or not.
     *
     * @return True - if the application has been locally deleted, False - otherwise.
     */
    public boolean isLocallyDeleted() {
        return isLocallyDeleted;
    }

    /**
     * Returns whether the application has been locally created or not.
     *
     * @return True - if the application has been locally created, False - otherwise.
     */
    public boolean isLocallyCreated() {
        return isLocallyCreated;
    }

    /**
     * Checks if the text is empty or null. If it is, return "", otherwise return the text.
     *
     * @param text
     * @return Returns "" if the text is null or empty, otherwise same text is returned.
     */
    private String sanitizeText(String text) {
        if (TextUtils.isEmpty(text) || text.equals(Constants.NULL_STRING)) {
            return Constants.EMPTY_STRING;
        }
        return text;
    }
}
