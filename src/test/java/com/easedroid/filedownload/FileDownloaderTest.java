package com.easedroid.filedownload;

import com.easedroid.filedownload.config.SingleThreadConfig;
import com.easedroid.filedownload.log.Logger;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class FileDownloaderTest extends TestCase {

    public static final String TAG = "FileDownloaderTest";

    private DownloadTask createTask(CountDownLatch latch) {
        DownloadTask downloadTask = new DownloadTask("001",
                "https://dl.google.com/dl/android/studio/install/3.0.1.0/android-studio-ide-171.4443003-windows.exe",
                new File("D:/Android Studio 3.0.1.exe"));
        downloadTask.setDownloadListener(new DownloadListener() {
            @Override
            public void onStart() {
                Logger.d(TAG, "onStart()");
            }

            @Override
            public void onStop() {
                Logger.d(TAG, "onStop()");
                latch.countDown();
            }

            @Override
            public void onFinished() {
                Logger.d(TAG, "onFinished()");
                latch.countDown();
            }

            @Override
            public void onError(int errorCode) {
                Logger.d(TAG, "onError errorCode = " + errorCode);
                latch.countDown();
            }
        });
        return downloadTask;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FileDownloader.INSTANCE.launch(new SingleThreadConfig());
    }

    @Test
    public void testDownloadFile() {
        CountDownLatch latch = new CountDownLatch(1);
        DownloadTask task = createTask(latch);
        FileDownloader.INSTANCE.addDownloadTask(task);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testShutdown() {
        CountDownLatch latch = new CountDownLatch(1);
        DownloadTask task = createTask(latch);
        FileDownloader.INSTANCE.addDownloadTask(task);
        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FileDownloader.INSTANCE.shutdown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testStopTask() {
        CountDownLatch latch = new CountDownLatch(1);
        DownloadTask task = createTask(latch);
        FileDownloader.INSTANCE.addDownloadTask(task);
        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        task.stop();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
