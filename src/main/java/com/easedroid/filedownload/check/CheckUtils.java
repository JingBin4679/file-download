package com.easedroid.filedownload.check;

import com.easedroid.filedownload.config.Config;
import com.easedroid.filedownload.exception.DownloaderConfigException;

public class CheckUtils {

    public static void checkConfig(Config config) {
        if (config == null) {
            throw new DownloaderConfigException("config cannot be null");
        }

        if (config.getThreadNum() <= 0) {
            throw new DownloaderConfigException("The number of threads must be greater than 0.");
        }

    }

}
