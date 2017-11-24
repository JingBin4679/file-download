package com.easedroid.filedownload;

import com.easedroid.filedownload.log.Logger;
import com.easedroid.filedownload.util.AcceptAllHostnameVerifier;
import com.easedroid.filedownload.util.AcceptAllTrustManager;
import com.easedroid.filedownload.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class DownloadTask implements Runnable {

    public static final int STATE_READY = 0;  //准备
    public static final int STATE_DOWNLOADING = 1; //下载中
    public static final int STATE_FINISHED = 2; //完成
    public static final int STATE_ERROR = 3; //错误
    public static final int STATE_STOP = 4; //停止
    public static final int STATE_PAUSE = 5; //暂停
    public static final String TAG = "DownloadTask";


    private String taskId;
    private String path;
    private File file;
    private int state = STATE_READY;
    private int error = 0;
    private long readTimeout = TimeUnit.SECONDS.toMillis(5);
    private long connectTimeout = TimeUnit.SECONDS.toMillis(5);
    private DownloadListener listener;
    private long startTime;
    private long endTime;

    public DownloadTask(String taskId, String url, File file) {
        this.taskId = taskId;
        this.path = url;
        this.file = file;
    }

    public void setDownloadListener(DownloadListener listener) {
        this.listener = listener;
    }

    public boolean isDownloading() {
        return STATE_DOWNLOADING == state;
    }

    public void stop() {
        setState(STATE_STOP);
    }

    private void setState(int state) {
        if (state < STATE_READY || state > STATE_PAUSE) {
            Logger.e(TAG, "错误的状态值。");
            return;
        }
        this.state = state;
    }

    private void setErrorCode(int errorCode) {
        error = errorCode;
        setState(STATE_ERROR);
        onError();
    }

    private boolean checkTaskValid() {
        if (StringUtils.isEmpty(taskId)) {
            setErrorCode(Const.Error.INVALID_TASK_ID);
            return false;
        }

        if (StringUtils.isEmpty(path)) {
            setErrorCode(Const.Error.INVALID_URL);
            return false;
        }

        if (file == null) {
            setErrorCode(Const.Error.INVALID_FILE);
            return false;
        }
        return true;
    }

    private void printResponseHeader(URLConnection connection) {
        Map<String, List<String>> map = connection.getHeaderFields();
        for (String key : map.keySet()) {
            List<String> values = map.get(key);
            Logger.d(TAG, key + ": " + values);
        }
    }

    public void run() {
        startTime = System.currentTimeMillis();
        if (state != STATE_READY && state != STATE_PAUSE) {
            Logger.d(TAG, "状态错误，不下载。");
            return;
        }
        setState(STATE_DOWNLOADING);
        onStart();
        if (!checkTaskValid()) {
            return;
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                setErrorCode(Const.Error.INVALID_FILE);
                return;
            }
        }

        RandomAccessFile rsf = null;

        try {
            rsf = new RandomAccessFile(file, "rw");
            rsf.setLength(0);
        } catch (IOException e) {
            e.printStackTrace();
            setErrorCode(Const.Error.INVALID_FILE);
            return;
        }

        HttpURLConnection urlConnection;
        try {
            URL url = new URL(path);
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            setErrorCode(Const.Error.INVALID_URL);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            setErrorCode(Const.Error.CONNECTION_EXCEPTION);
            return;
        }

        configConnection(urlConnection);

        if (checkHttpsConnection(urlConnection)) {
            setErrorCode(Const.Error.HTTPS_EXCEPTION);
            return;
        }

        long contentLength = StringUtils.tryParseLong(urlConnection.getHeaderField("Content-Length"), 0L);
        if (contentLength == 0L) { // 文件长度检查
            setErrorCode(Const.Error.ERROR_REMOTE_FILE);
            return;
        }

        printResponseHeader(urlConnection);

        InputStream is = null;
        try {
            int code = urlConnection.getResponseCode();
            if (code != 200 && code != 206 && code != 302) { //http code 检查
                setErrorCode(Const.Error.ERROR_HTTP_CODE);
                return;
            }

            is = urlConnection.getInputStream();
            String encoding = urlConnection.getHeaderField("Content-Encoding");
            if ("gzip".equalsIgnoreCase(encoding)) {
                is = new GZIPInputStream(is);
            }

            int bufferLength = 32 * Const.KB;
            byte[] readBuffer = new byte[bufferLength];
            int len = 0;
            long readSize = 0;
            while ((len = is.read(readBuffer, 0, bufferLength)) > 0) {
                if (checkStop()) {
                    onStop();
                    return;
                }
                printProcess(contentLength, readSize);
                readSize += len;
                rsf.write(readBuffer, 0, len);
            }
            if (readSize != contentLength) {
                setErrorCode(Const.Error.ERROR_FILE_SIZE);
                return;
            }
            setState(STATE_FINISHED);
            endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            Logger.d(TAG, taskId + "  下载完成 "
                    + "\n文件大小：" + (contentLength * 1F / Const.MB) + "MB"
                    + "\n下载用时：" + (duration / TimeUnit.SECONDS.toMillis(1)) + " S"
                    + "\n平均速度：" + (contentLength * 1F / Const.KB / duration * 1000) + "KB/S");
            onFinished();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (rsf != null) {
                try {
                    rsf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    int lastPercent = 0;

    private void printProcess(long contentLength, long readSize) {
        int percent = (int) (readSize * 1f / contentLength * 100);
        if (lastPercent != percent) {
            lastPercent = percent;
            System.out.append(String.format("%02d|", percent));
            if (percent % 30 == 0) {
                System.out.println();
            }
        }
    }

    private boolean checkStop() {
        return state == STATE_STOP;
    }

    private boolean checkHttpsConnection(HttpURLConnection urlConnection) {
        //信任https站点
        if (urlConnection instanceof HttpsURLConnection) {
            try {
                SSLContext _sslCtx = SSLContext.getInstance("SSL");
                _sslCtx.init(null, new TrustManager[]{new AcceptAllTrustManager()}, new SecureRandom());
                AcceptAllHostnameVerifier _hostVerifier = new AcceptAllHostnameVerifier();
                HttpsURLConnection conns = (HttpsURLConnection) urlConnection;
                conns.setHostnameVerifier(_hostVerifier);
                conns.setSSLSocketFactory(_sslCtx.getSocketFactory());
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }
        return false;
    }

    private void configConnection(HttpURLConnection conn) {
        conn.setReadTimeout((int) readTimeout);
        conn.setConnectTimeout((int) connectTimeout);
        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setInstanceFollowRedirects(true);
    }


    /* call back */
    private void onStart() {
        if (listener == null) {
            return;
        }
        listener.onStart();
    }

    private void onError() {
        if (listener == null) {
            return;
        }
        listener.onError(error);
    }

    private void onStop() {
        if (listener == null) {
            return;
        }
        listener.onStop();
    }

    private void onFinished() {
        if (listener == null) {
            return;
        }
        listener.onFinished();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }



}
