package com.sjtu.karaoke.util;

import android.util.Log;

import com.sjtu.karaoke.component.LoadingDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import static com.sjtu.karaoke.util.MiscUtil.getRequest;

public class FileUtil {
    /**
     * 删除文件
     * @param fullPath 绝对路径
     */
    public static void deleteOneFile(String fullPath) {
        System.out.println("========== deleting " + fullPath);
        File file = new File(fullPath);
        if (file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    /**
     * 检查文件是否存在
     * @param fullPath 绝对路径
     * @return 文件是否存在
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
     * 批量从给定的url下载文件直到所有文件都下载完或下载失败后才返回。如果有文件下载失败，会删除所有其他文件
     * @param urls 下载地址
     * @param destFullPaths 存储路径，和下载地址一一对应
     * @return 当所有文件都成功下载时返回true
     */
    public static boolean downloadFiles(String[] urls, String[] destFullPaths) {
        int numOfFilesToDownload = urls.length;

        CountDownLatch countDownLatch = new CountDownLatch(numOfFilesToDownload);
        AtomicInteger numOfFilesDownloaded = new AtomicInteger(0);

        for (int i = 0; i < numOfFilesToDownload; ++i) {
            downloadFile(urls[i], destFullPaths[i]);
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
     * 和上述下载文件方法作用相同，但还会在下载的同时更新LoadingDialog的进度
     * @param urls 下载地址
     * @param destFullPaths 存储路径，和下载地址一一对应
     * @param loadingDialog 要更新的假爱对话框
     * @param isCanceled 下载是否被取消
     * @return 当所有文件都成功下载时返回true
     */
    public static boolean downloadFiles(
            String[] urls,
            String[] destFullPaths,
            LoadingDialog loadingDialog,
            AtomicBoolean isCanceled) {
        int numOfFilesToDownload = urls.length;

        CountDownLatch countDownLatch = new CountDownLatch(numOfFilesToDownload);
        AtomicInteger numOfFilesDownloaded = new AtomicInteger(0);

        for (int i = 0; i < numOfFilesToDownload; ++i) {
            downloadFile(
                    urls[i],
                    destFullPaths[i],
                    countDownLatch,
                    numOfFilesDownloaded,
                    loadingDialog,
                    100 / numOfFilesToDownload,
                    isCanceled);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (numOfFilesDownloaded.get() == numOfFilesToDownload && !isCanceled.get()) {
            // eliminate inaccuracy due to integer division
            loadingDialog.setProgress(LoadingDialog.MAX_PROGRESS);
            return true;
        } else {
            for (String destFullPath: destFullPaths) {
                deleteOneFile(destFullPath);
            }
            return false;
        }
    }

    /**
     * 从给定url下载单个文件，如果下载失败，会删除被下载的文件
     * @param url 下载地址
     * @param destFullPath 文件保存地址
     */
    public static void downloadFile(String url, String destFullPath) {
        System.out.println("========== Downloading from " + url + " to " + destFullPath + " ==========");
        getRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("downloadFile", "Failed to download file from " + url);
            }

            @Override
            public void onResponse(Call call, Response response) {
                // receive and save the file
                if (!saveFileFromResponse(response, destFullPath)) {
                    deleteOneFile(destFullPath);
                    Log.e("downloadFile", "Failed to save file at " + destFullPath);
                }
            }
        });
    }

    /**
     * 和上述下载文件方法作用相同，但还会在下载的同时更新LoadingDialog的进度
     * @param url 下载地址
     * @param destFullPath 存储路径，和下载地址一一对应
     * @param loadingDialog 要更新的假爱对话框
     * @param isCanceled 下载是否被取消
     */
    public static void downloadFile(String url,
                                    String destFullPath,
                                    CountDownLatch countDownLatch,
                                    AtomicInteger numOfFilesDownloaded,
                                    LoadingDialog loadingDialog,
                                    int increment,
                                    AtomicBoolean isCanceled) {
        System.out.println("========== Downloading from " + url + " to " + destFullPath + " ==========");
        getRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (countDownLatch != null) {
                    while (countDownLatch.getCount() != 0) {
                        countDownLatch.countDown();
                    }
                }
                Log.e("downloadFile", "Failed to download file from " + url);
            }

            @Override
            public void onResponse(Call call, Response response) {
                // receive and save the file
                if (saveFileFromResponse(response, destFullPath, loadingDialog, increment, isCanceled)) {
                    // countDownLatch and numOfFilesDownloaded are absent or present at the same time
                    if (countDownLatch != null) {
                        countDownLatch.countDown();
                        numOfFilesDownloaded.incrementAndGet();
                    }
                } else {
                    deleteOneFile(destFullPath);
                    while (countDownLatch.getCount() != 0) {
                        countDownLatch.countDown();
                    }
                }
            }
        });
    }

    /**
     * 从response body中保存文件
     * @param response HTTP相应
     * @param destPath 文件保存路径（包括文件名）
     * @return 当文件被完整保存时返回true
     */
    public static boolean saveFileFromResponse(Response response, String destPath) {
        if (!response.isSuccessful()) {
            return false;
        }

        File destFile = new File(destPath);
        try {
            System.out.println("========== Saving file to " + destPath + " ==========");
            BufferedSink sink;

            if (destFile.exists()) {
                destFile.delete();
            }
            sink = Okio.buffer(Okio.sink(destFile));
            sink.writeAll(Objects.requireNonNull(response.body()).source());
            sink.close();
            return true;
        } catch (IOException e) {
            System.err.println("Failed to download file to " + destPath);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 和上述保存文件方法效果相同，但是会同时更新加载对话框
     * @param response HTTP相应
     * @param destPath 文件保存路径（包括文件名）
     * @param loadingDialog 需要更新的加载对话框
     * @param increment 本次下载需要更新的进度百分比
     * @param isCanceled 本次下载是否已经被用户取消
     * @return 当文件成功下载时返回true，如果因为网络连接或者用户取消返回false
     */
    public static boolean saveFileFromResponse(Response response, String destPath,
                                               LoadingDialog loadingDialog, int increment,
                                               AtomicBoolean isCanceled) {
        if (!response.isSuccessful()) {
            return false;
        }

        try {
            System.out.println("========== Saving file to " + destPath + " ==========");

            File destFile = new File(destPath);
            String contentLength = response.header("Content-Length", null);

            if (destFile.exists()) {
                destFile.delete();
            }

            byte[] buffer = new byte[4096];

            InputStream is = Objects.requireNonNull(response.body()).byteStream();
            FileOutputStream fos = new FileOutputStream(destFile);
            int n;

            if (contentLength != null) {
                final int totalBytes = Integer.parseInt(contentLength);
                final int bytesPerOnePercent = totalBytes / increment;

                int bytesToCount = 0;
                while ((n = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, n);

                    bytesToCount += n;

                    while (bytesToCount >= bytesPerOnePercent) {
                        bytesToCount -= bytesPerOnePercent;
                        loadingDialog.incrementProgress(1);
                    }
                    if (isCanceled.get()) {
                        is.close();
                        fos.close();
                        return false;
                    }
                }
            }
            // 如果响应头没有Content-Length，无法按照1%的粒度更新进度
            else {
                while ((n = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, n);
                    if (isCanceled.get()) {
                        is.close();
                        fos.close();
                        return false;
                    }
                }
                loadingDialog.incrementProgress(increment);
            }

            is.close();
            fos.close();
            return true;
        } catch (IOException e) {
            System.err.println("Failed to download file to " + destPath);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取一个目录下的所有文件的绝对路径
     * @param dirPath 目录路径
     * @return 所有绝对路径，包括目录和文件
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
