package com.easedroid.filedownload.config;

import com.easedroid.filedownload.log.Logger;

public class SingleThreadConfig extends Config {
    private static final String TAG = "SingleThreadConfig";

    public SingleThreadConfig() {

    }

    public static Config getConfig() {
        return new SingleThreadConfig();
    }

    @Override
    public void setThreadNum(int threadNum) {
        Logger.i(TAG, "single thread config not accept thread num");
    }
}
