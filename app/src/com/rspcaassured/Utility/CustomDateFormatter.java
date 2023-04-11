package com.rspcaassured.Utility;

public class CustomDateFormatter {
    public static String formattedDate(String date){
        if(date!=null && !date.trim().isEmpty()){
            String[] sep = date.split("-");
            if(sep.length==3){
                return sep[2] +" / "+ sep[1] + " / "+sep[0];
            }else{
                return "";
            }
        }else{
            return "";
        }
    }
}
