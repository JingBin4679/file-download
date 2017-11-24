package com.easedroid.filedownload.exception;

public class DownloaderConfigException extends RuntimeException {
    public DownloaderConfigException(String message) {
        super(message);
    }

    public DownloaderConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public DownloaderConfigException(Throwable cause) {
        super(cause);
    }

    public DownloaderConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
