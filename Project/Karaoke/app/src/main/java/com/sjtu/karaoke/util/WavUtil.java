package com.sjtu.karaoke.util;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.sjtu.karaoke.waveditor.WavHeader;
import com.sjtu.karaoke.waveditor.WavReader;
import com.sjtu.karaoke.waveditor.WavWriter;

import java.io.IOException;

import static com.sjtu.karaoke.util.Constants.RECORD_DIRECTORY;

public class WavUtil {
    /**
     * Merge multiple pcm files into one single wav file stored at <destPath>
     * @param destPath
     * @param trimmedAccompanyFullPath
     * @param voiceFullPath
     * @param accompanyVolume
     * @param voiceVolume
     */
    public static void mergeWAVs(String destPath, String trimmedAccompanyFullPath, String voiceFullPath,
                                   float accompanyVolume, float voiceVolume) {
        FFmpeg.execute("-y" +
                " -i " + trimmedAccompanyFullPath +
                " -i " + voiceFullPath +
                " -filter_complex" +
                " \"[0]volume=" + accompanyVolume * 2 + "[a];" +
                "[1]volume=" + voiceVolume * 2 + "[b];" +
                "[a][b]amix=inputs=2:duration=longest:dropout_transition=1\" " + destPath);
    }

    /**
     * @param from  原文件名，不包含路径
     * @param to    目标文件名，不包含路径
     * @param start 开始时间 ms
     * @param end   结束时间 ms
     */
    public static void trimWav(String from, String to, int start, int end) {
        System.out.println("========== Trimming" + from + " ==========");
        try {
            //创建原始音频文件流
            WavReader reader = new WavReader(from);
            //读取header
            WavHeader header = reader.getHeader();
            //创建裁剪文件输出文件流
            WavWriter writer = new WavWriter(to);
            writer.writeHeader(header);
            //BYTE_PER_READ 指的是每次读取的字节数，可以自定义
            byte[] buffer = new byte[WavReader.BUFFER_LENGTH];
            int size = -1;
            //移动至裁剪起点
            reader.moveToStart(start);
            //获取裁剪时间段对应的字节大小
            int dataSize = reader.getIntervalSize(end - start);
            int sizeCount = 0;
            while (true) {
                size = reader.readData(buffer, 0, buffer.length);
                //当到达裁剪时间段大小时候结束读取
                if (size < 0 || sizeCount >= dataSize) {
                    //在close时候写入实际音频数据大小
                    writer.closeFile();
                    reader.closeFile();
                    return;
                }
                //写入音频数据到裁剪文件
                writer.writeData(buffer, 0, size);
                //计算读取的字节数，注意，因为BYTE_PER_READ的原因，读取的字节数和实际的音频大小未必相同，
                //不能把它直接当作实际音频数据大小
                sizeCount += size;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("========== Trimming " + from + " finished ==========");
    }

    public static double getWAVDuration(String fullPath) {
        WavReader wavReader = new WavReader(fullPath);
        wavReader.getHeader();
        return wavReader.getDuration();
    }
}
