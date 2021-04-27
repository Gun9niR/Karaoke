package com.sjtu.karaoke.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import static com.sjtu.karaoke.util.MiscUtil.getRequest;

public class FileUtil {
    public static void deleteOneFile(String fullPath) {
        File file = new File(fullPath);

        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    /**
     * Check if a file at given full path exists
     *
     * @param fullPath Full path to the file to be checked
     * @return True if file exists, false otherwise
     */
    public static boolean isFilePresent(String fullPath) {
        File file = new File(fullPath);
        return file.exists();
    }

    /**
     * Download a bunch of files from urls to destFullPaths, which corresponde with index
     * The function will not return until all files have been downloaded
     *
     * @param urls
     * @param destFullPaths
     */
    public static void downloadFiles(String[] urls, String[] destFullPaths) {
        int numOfFilesToDownload = urls.length;

        CountDownLatch countDownLatch = new CountDownLatch(numOfFilesToDownload);

        for (int i = 0; i < numOfFilesToDownload; ++i) {
            downloadFile(urls[i], destFullPaths[i], countDownLatch);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Download one file from url to destFullPath
     *
     * @param url          Url to send the request to
     * @param destFullPath Destination to save the file
     */
    public static void downloadFile(String url, String destFullPath, CountDownLatch countDownLatch) {
        System.out.println("========== Downloading from " + url + " to " + destFullPath + " ==========");
        getRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Error when downloading file", "Failed to download file " + destFullPath);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // receive and save the file
                saveFileFromResponse(response, destFullPath);
                if (countDownLatch != null) {
                    countDownLatch.countDown();
                }
            }
        });
    }

    public static void saveFileFromResponse(Response response, String destPath) {
        File destFile = new File(destPath);

        try {
            BufferedSink sink = Okio.buffer(Okio.sink(destFile));
            System.out.println("========== Saving file to " + destPath + " ==========");
            sink.writeAll(response.body().source());
            sink.close();
        } catch (IOException e) {
            System.err.println("Failed to download file to " + destPath);
            e.printStackTrace();
        }
    }

    public static void deleteTemporaryFiles() {

    }
}
