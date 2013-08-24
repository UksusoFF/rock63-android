package com.uksusoff.rock63.utils;

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
	
	public static int getThemedResource(Context context, int attr) {
	
        TypedValue typedvalueattr = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedvalueattr, true);
        return typedvalueattr.resourceId;
	
	}
}
