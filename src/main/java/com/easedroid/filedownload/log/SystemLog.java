package com.easedroid.filedownload.log;

class SystemLog implements ILog {

    public void d(String TAG, String msg) {
        System.out.println(TAG + ":\t" + msg);
    }

    public void i(String TAG, String msg) {
        System.out.println(TAG + ":\t" + msg);

    }

    public void e(String TAG, String msg) {
        System.err.println(TAG + ":\t" + msg);

    }

    public void wtf(String TAG, String msg) {
        System.err.println(TAG + ":\t" + msg);

    }
}
