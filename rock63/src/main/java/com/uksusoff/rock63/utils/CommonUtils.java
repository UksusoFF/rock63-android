package com.uksusoff.rock63.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import android.content.Context;
import android.util.TypedValue;

public class CommonUtils {
	public static Date getDateFromTimestamp(int ts) {
	    
	    if (ts==-1)
	        return null;
	    
		return new Date( ((long)ts) * 1000 );
	}
	
	public static int getTimestampFromDate(Date d) {
	    
	    if (d==null)
	        return -1;
	    
	    return (int) (d.getTime()/1000);
	}
	
	public static String getCroppedString(String source, int places, boolean addDots) {
	    
	    String postfix = addDots ? "..." : "";
	    
	    if (source.length()<places-postfix.length())
	        return source;
	    else
	        return source.substring(0, places-postfix.length()) + postfix;
	}
	
	public static String getTextFromHtml(String s) {
	    String noHTMLString = s.replaceAll("<(.|\n)*?>", "");
	    noHTMLString = noHTMLString.replaceAll("&.*?;", "");
	    
        return noHTMLString;
	}

    public static String convertStreamToString(InputStream is) throws UnsupportedEncodingException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
}
