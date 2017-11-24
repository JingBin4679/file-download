package com.easedroid.filedownload.log;

interface ILog {

    void d(String TAG, String msg);

    void i(String TAG, String msg);

    void e(String TAG, String msg);

    void wtf(String TAG, String msg);
}
