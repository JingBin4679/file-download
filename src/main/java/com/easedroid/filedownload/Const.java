package com.easedroid.filedownload;

public class Const {

    public static final int KB = 1024; //1KB = 1024B
    public static final int MB = 1024 * KB; //1MB = 1024KB

    public static class Error {
        public static final int NO_ERROR = 0;

        public static final int INVALID_TASK_ID = 0x0001;

        public static final int INVALID_URL = 0x0002;

        public static final int INVALID_FILE = 0x0003;

        public static final int CONNECTION_EXCEPTION = 0x0004;

        public static final int HTTPS_EXCEPTION = 0x0005;

        public static final int ERROR_HTTP_CODE = 0x0006;

        public static final int ERROR_REMOTE_FILE = 0x0007;

        public static final int ERROR_FILE_SIZE = 0x0008;
    }
}
