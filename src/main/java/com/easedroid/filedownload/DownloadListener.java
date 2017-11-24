package com.easedroid.filedownload;

public interface DownloadListener {

    void onStart();

    void onStop();

    void onFinished();

    void onError(int errorCode);
}
