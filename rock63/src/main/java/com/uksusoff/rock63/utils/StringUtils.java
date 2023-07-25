package com.uksusoff.rock63.utils;

import android.text.Spannable;
import android.text.Spanned;

import androidx.core.text.HtmlCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class StringUtils {

    public static String crop(String source, int places, boolean addDots) {
        String postfix = addDots ? "..." : "";

        return source.length() < places - postfix.length()
                ? source
                : source.substring(0, places - postfix.length()) + postfix;
    }

    public static Spanned fromHtml(String s) {
        return HtmlCompat.fromHtml(s, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    public static String cleanHtml(String s) {
        String noHTMLString = s.replaceAll("<(.|\n)*?>", "");

        noHTMLString = noHTMLString.replaceAll("&.*?;", "");

        return noHTMLString;
    }

    public static String fromStream(InputStream is) throws UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
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
