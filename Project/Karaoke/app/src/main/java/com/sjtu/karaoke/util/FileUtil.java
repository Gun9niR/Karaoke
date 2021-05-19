package com.sjtu.karaoke.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import static com.sjtu.karaoke.util.MiscUtil.getRequest;

public class FileUtil {
    /**
     * Delete file at given full path.
     * It handles deletion of nonexistent files
     * @param fullPath
     */
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

    public static boolean areFilesPresent(String[] fullPaths) {
        for (String fullPath: fullPaths) {
            if (!isFilePresent(fullPath)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Download a bunch of files from urls to destFullPaths, which corresponde with index
     * The function will not return until all files have been downloaded
     * If one of the files fail to download, all the files will be deleted
     * @param urls
     * @param destFullPaths
     * @return true if all files are downloaded successfully or exist already, false otherwise
     */
    public static boolean downloadFiles(String[] urls, String[] destFullPaths) {
        int numOfFilesToDownload = urls.length;

        CountDownLatch countDownLatch = new CountDownLatch(numOfFilesToDownload);
        AtomicInteger numOfFilesDownloaded = new AtomicInteger(0);

        for (int i = 0; i < numOfFilesToDownload; ++i) {
            downloadFile(urls[i], destFullPaths[i], countDownLatch, numOfFilesDownloaded);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (numOfFilesDownloaded.get() == numOfFilesToDownload) {
            return true;
        } else {
            for (String destFullPath: destFullPaths) {
                deleteOneFile(destFullPath);
            }
            return false;
        }
    }

    /**
     * Download one file from url to destFullPath
     *
     * @param url          Url to send the request to
     * @param destFullPath Destination to save the file
     */
    public static void downloadFile(String url, String destFullPath, CountDownLatch countDownLatch, AtomicInteger numOfFilesDownloaded) {
        System.out.println("========== Downloading from " + url + " to " + destFullPath + " ==========");
        getRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (countDownLatch != null) {
                    while (countDownLatch.getCount() != 0) {
                        countDownLatch.countDown();
                    }
                }
                Log.e("Error when downloading file", "Failed to download file " + destFullPath);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // receive and save the file
                if (saveFileFromResponse(response, destFullPath)) {
                    // countDownLatch and numOfFilesDownloaded are absent or present at the same time
                    if (countDownLatch != null) {
                        countDownLatch.countDown();
                        numOfFilesDownloaded.incrementAndGet();
                    }
                } else {
                    while (countDownLatch.getCount() != 0) {
                        countDownLatch.countDown();
                    }
                }
            }
        });
    }

    public static boolean saveFileFromResponse(Response response, String destPath) {
        File destFile = new File(destPath);
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            BufferedSink sink = Okio.buffer(Okio.sink(destFile));
            System.out.println("========== Saving file to " + destPath + " ==========");
            sink.writeAll(response.body().source());
            sink.close();
            return true;
        } catch (IOException e) {
            System.err.println("Failed to download file to " + destPath);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all file paths in a directory
     * @param dirPath
     * @return The full paths to all files in the directory
     */
    public static List<String> getFullPathsInDirectory(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        List<String> paths = new ArrayList<>();

        for (File file: files) {
            paths.add(file.getPath());
        }
        return paths;
    }
}
