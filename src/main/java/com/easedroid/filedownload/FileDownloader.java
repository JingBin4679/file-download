package com.easedroid.filedownload;

import com.easedroid.filedownload.config.Config;
import com.easedroid.filedownload.config.SingleThreadConfig;
import com.easedroid.filedownload.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.easedroid.filedownload.check.CheckUtils.checkConfig;

public enum FileDownloader {

    INSTANCE;

    private static final String TAG = "FileDownloader";
    AtomicBoolean watchDog = new AtomicBoolean(false);

    private Config config = new SingleThreadConfig();
    private ExecutorService service;
    private List<DownloadTask> record = new ArrayList<>();

    public synchronized void launch(Config config) {
        if (!watchDog.compareAndSet(false, true)) {
            Logger.e(TAG, "下载器已经运行。");
            return;
        }
        checkConfig(config);
        this.config = config;
        start();
    }

    private void start() {
        int mThreadNum = config.getThreadNum();
        String prefixName = config.getPrefixName();
        final String prefix = prefixName == null ? "File download thread" : prefixName;
        service = Executors.newFixedThreadPool(mThreadNum, new ThreadFactory() {
            private int count = 0;

            public Thread newThread(Runnable r) {
                String threadName = prefix + " " + count;
                count++;
                return new Thread(r, threadName);
            }
        });
    }

    public void addDownloadTask(DownloadTask task) {
        record.add(task);
        service.submit(task);
    }

    public void shutdown() {
        service.shutdown();
        stopAllTask();
    }

    private void stopAllTask() {
        for (DownloadTask task : record) {
            stopTask(task);
        }
    }

    private void stopTask(DownloadTask task) {
        if (!task.isDownloading()) {
            return;
        }
        task.stop();
    }

    public void onFinished(DownloadTask task) {
        record.remove(task);
    }
}
