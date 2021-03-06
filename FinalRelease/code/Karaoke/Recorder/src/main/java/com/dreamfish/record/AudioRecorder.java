package com.dreamfish.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.dreamfish.record.FileUtil.deleteOneFile;
import static com.dreamfish.record.FileUtil.getPcmFullPath;
import static com.dreamfish.record.FileUtil.getTrimmedWavFullPath;
import static com.sjtu.karaoke.singrater.RatingUtil.f0analysis;

/**
 * 实现录音
 *
 * @author chenmy0709
 * @version V001R001C01B001
 */
public class AudioRecorder {
    //音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;

    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    //采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    public final static int AUDIO_SAMPLE_RATE = 44100;

    // 切分pcm的间隔时间
    public static final int PCM_SPLIT_INTERVAL = 500;
    //声道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    public final static int NUM_OF_CHANNEL = 1;
    //编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    // 上一个pcm文件的开始时间（已结束录音）
    private int lastPcmStartTime;
    // 当前正在写的pcm文件的开始时间
    private int currentPcmStartTime;
    // pcm切分时间和interval整数倍的插，例如pcm从1010ms开始切，间隔是500ms，offset就是10ms
    private int offset;
    //录音对象
    private AudioRecord audioRecord;
    // 记录每个pcm的f0analysis是否完成
    private HashSet<Integer> f0Complete;
    //录音状态
    private Status status = Status.STATUS_NO_READY;
    //文件名，包含后缀.wav，不包含路径
    private String fileName;
    //录音所有pcm文件
    private List<String> filesName = new ArrayList<>();

    // 当前正在输出到的pcm文件
    FileOutputStream fos = null;
    File fo = null;
    // 当前正在写的pcm文件
    String currentFileName= null;
    // 互斥锁，用来保证在停止录音时最后一个FileOutputStream一定能被close
    Semaphore mutex = new Semaphore(1);
    // 是否需要将录音写到新的pcm文件
    private boolean shouldStartNewPcm = false;
    /**
     * 类级的内部类，也就是静态类的成员式内部类，该内部类的实例与外部类的实例
     * 没有绑定关系，而且只有被调用时才会装载，从而实现了延迟加载
     */
    private static class AudioRecorderHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static AudioRecorder instance = new AudioRecorder();
    }

    private AudioRecorder() {
    }

    public static AudioRecorder getInstance() {
        return AudioRecorderHolder.instance;
    }

    /**
     * 创建默认的录音对象
     *
     * @param fileName 文件名
     */
    public void createDefaultAudio(String fileName, int offset) {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL, AUDIO_ENCODING);
        audioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes);
        this.fileName = fileName;

        filesName.clear();
        this.lastPcmStartTime = 0;
        this.offset = offset;
        f0Complete = new HashSet<>();
        // initialize file name (no extension)
        currentFileName = fileName + filesName.size();
        setFileOutputStream();

        status = Status.STATUS_READY;
    }

    /**
     * Needs to be called AFTER setting currentFileName
     */
    private void setFileOutputStream() {
        try {
            fo = new File(FileUtil.getPcmFileAbsolutePath(currentFileName));
            if (fo.exists()) {
                fo.delete();
            }
            fos = new FileOutputStream(fo);
            filesName.add(currentFileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 开始录音
     *
     * @param listener 音频流的监听
     */
    public void startRecord(final RecordStreamListener listener) {

        if (status == Status.STATUS_NO_READY || TextUtils.isEmpty(fileName)) {
            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
        }
        if (status == Status.STATUS_START) {
            throw new IllegalStateException("正在录音");
        }
        Log.d("AudioRecorder", "===startRecord===" + audioRecord.getState());
        audioRecord.startRecording();
        new Thread(() -> writeDataTOFile(listener)).start();
    }

    /**
     * 暂停录音
     */
    public void pauseRecord() {
        Log.d("AudioRecorder", "===pauseRecord===");
        if (status != Status.STATUS_START) {
            throw new IllegalStateException("没有在录音");
        } else {
            audioRecord.stop();
            status = Status.STATUS_PAUSE;
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord(boolean shouldMergePcm) {
        Log.d("AudioRecorder", "===stopRecord===");
        if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY) {
            throw new IllegalStateException("录音尚未开始");
        } else {
            audioRecord.stop();
            status = Status.STATUS_STOP;

            fos = null;
            fo = null;
            currentFileName = null;

            release(shouldMergePcm);
        }
    }

    /**
     * 释放资源，包括删除所有pcm和打分用的wav
     */
    public void release(boolean shouldMergePcm) {
        Log.d("AudioRecorder", "===release===");
        //假如有暂停录音
        try {
            if (filesName.size() > 0) {
                // 用互斥锁保护，确保最后一个FileOutputStream会被关闭
                mutex.acquireUninterruptibly();
                if (fos != null) {
                    fos.close();
                }
                mutex.release();

                //将多个pcm文件转化为wav文件
                if (shouldMergePcm) {
                    List<String> filePaths = new ArrayList<>();
                    for (String fileName : filesName) {
                        filePaths.add(FileUtil.getPcmFileAbsolutePath(fileName));
                    }
                    mergePCMFilesToWAVFile(filePaths);
                }

                filesName.clear();
            }
        } catch (IllegalStateException | IOException e) {
            throw new IllegalStateException(e.getMessage());
        }

        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        status = Status.STATUS_NO_READY;
    }

    /**
     * 取消录音
     */
    public void cancel() {
        for (String fileName: filesName) {
            deleteOneFile(fileName);
        }
        filesName.clear();
        fileName = null;
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        status = Status.STATUS_NO_READY;
    }

    public void setShouldStartNewPcm(boolean shouldStartNewPcm) {
        this.shouldStartNewPcm = shouldStartNewPcm;
    }

    /**
     * 如果最后一句需要打分，且录音在最后一个需要打分的pcm录完之前结束，需要调用这个方法，强制最后一个pcm直接结束
     */
    private void endLastPcm(int startTime) {
        try {
            fos.close();

            // 如果这句需要打分，就将这句话转换成.wav开始打分
            final String fileName = currentFileName;
            new Thread(() -> {
                String pcmFullPath = getPcmFullPath(fileName);
                String wavFullPath = getTrimmedWavFullPath(fileName);
                PcmToWav.makePCMFileToWAVFile(pcmFullPath, wavFullPath, false);

                f0analysis(wavFullPath, lastPcmStartTime);
                f0Complete.add(startTime);
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeDataTOFile(RecordStreamListener listener) {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[bufferSizeInBytes];

        status = Status.STATUS_START;
        while (status == Status.STATUS_START || status == Status.STATUS_STOP) {
            // 如果录音已经停止，将最后一句话打完分
            if (shouldStartNewPcm || status == Status.STATUS_STOP) {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    // 如果这句需要打分，就将这句话转换成.wav开始打分
                    final int startTime = status == Status.STATUS_STOP ?
                            currentPcmStartTime : lastPcmStartTime;
                    final String fileName = currentFileName;
                    new Thread(() -> {
                        String pcmFullPath = getPcmFullPath(fileName);
                        String wavFullPath = getTrimmedWavFullPath(fileName);
                        PcmToWav.makePCMFileToWAVFile(pcmFullPath, wavFullPath, false);

                        f0analysis(wavFullPath, lastPcmStartTime);
                        f0Complete.add(startTime);
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (status == Status.STATUS_STOP) {
                    break;
                }
                currentFileName = fileName + filesName.size();

                mutex.acquireUninterruptibly();
                if (status != Status.STATUS_STOP) {
                    setFileOutputStream();
                }
                mutex.release();

                shouldStartNewPcm = false;
            } else {
                int readSize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
                if (AudioRecord.ERROR_INVALID_OPERATION != readSize && fos != null) {
                    try {
                        // writes very fucking fastf
                        fos.write(audiodata);
                        if (listener != null) {
                            //用于拓展业务
                            listener.recordOfByte(audiodata, 0, audiodata.length);
                        }
                    } catch (IOException e) {
                        Log.e("AudioRecorder", e.getMessage());
                    }
                }
            }
        }

    }

    /**
     * 将pcm合并成wav
     *
     * @param filePaths
     */
    private void mergePCMFilesToWAVFile(final List<String> filePaths) {
        // synchronous task
        if (PcmToWav.mergePCMFilesToWAVFile(filePaths, FileUtil.getWavFileAbsolutePath(fileName))) {
            //操作成功
        } else {
            //操作失败
            Log.e("AudioRecorder", "mergePCMFilesToWAVFile fail");
            throw new IllegalStateException("mergePCMFilesToWAVFile fail");
        }
        fileName = null;
    }

    /**
     * 将单个pcm文件转化为wav文件
     */
    private void makePCMFileToWAVFile() {
        new Thread(() -> {
            if (PcmToWav.makePCMFileToWAVFile(FileUtil.getPcmFileAbsolutePath(fileName), FileUtil.getWavFileAbsolutePath(fileName), true)) {
                //操作成功
            } else {
                //操作失败
                Log.e("AudioRecorder", "makePCMFileToWAVFile fail");
                throw new IllegalStateException("makePCMFileToWAVFile fail");
            }
            fileName = null;
        }).start();
    }

    /**
     * 获取录音对象的状态
     *
     * @return
     */
    public Status getStatus() {
        return status;
    }

    public void setLastPcmStartTime(int lastPcmStartTime) {
        this.lastPcmStartTime = lastPcmStartTime;
        this.currentPcmStartTime = lastPcmStartTime + PCM_SPLIT_INTERVAL;
    }

    public boolean isf0AnalysisComplete(int startTime, int endTime) {
        // 开始时间和结束时间都往前取，例如500取0
        int s = (startTime - offset) / PCM_SPLIT_INTERVAL * PCM_SPLIT_INTERVAL + offset;
        int e = (endTime - offset) / PCM_SPLIT_INTERVAL * PCM_SPLIT_INTERVAL + offset;
        System.out.println("check f0analysis: " + s + " " + e);
        for (int i = s; i <= e; i += PCM_SPLIT_INTERVAL) {
            if (!f0Complete.contains(i)) {
                return false;
            }
        }
        return true;
    }
    /**
     * 录音对象的状态
     */
    public enum Status {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //录音
        STATUS_START,
        //暂停
        STATUS_PAUSE,
        //停止
        STATUS_STOP
    }

}