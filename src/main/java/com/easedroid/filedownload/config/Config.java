package com.easedroid.filedownload.config;

import static com.easedroid.filedownload.check.CheckUtils.checkConfig;

public class Config {

    //下载器工作线程数。
    private int threadNum = 1;

    //线程名称前缀。
    private String prefixName;

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        checkConfig(this);
        this.threadNum = threadNum;
    }

    public void setThreadPrefixName(String prefixName) {
        this.prefixName = prefixName;
    }

    public String getPrefixName() {
        return prefixName;
    }
}
