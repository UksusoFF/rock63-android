package com.koushikdutta.urlimageviewhelper;

import java.io.InputStream;

import android.content.Context;

public interface UrlDownloader {
    interface UrlDownloaderCallback {
        void onDownloadComplete(UrlDownloader downloader, InputStream in, String filename);
    }
    
    void download(Context context, String url, String filename, UrlDownloaderCallback callback, Runnable completion);
    boolean allowCache();
    boolean canDownloadUrl(String url);
}