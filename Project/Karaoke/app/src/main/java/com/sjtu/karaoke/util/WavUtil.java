package com.sjtu.karaoke.util;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.sjtu.karaoke.waveditor.WavHeader;
import com.sjtu.karaoke.waveditor.WavReader;
import com.sjtu.karaoke.waveditor.WavWriter;

import java.io.IOException;
import java.util.List;

public class WavUtil {

    /**
     * 将多个wav文件合并为一个文件
     * @param destPath          输出路径
     * @param voiceFullPath     录音音频的绝对路径
     * @param voiceVolume       录音的音量
     * @param accompanyPaths    伴奏文件列表。伴奏演唱模式有一个文件，自弹自唱模式有3个文件
     * @param accompanyVolumes  伴奏文件的音量列表，与伴奏文件以下标一一对应
     * @param voiceOffset       录音的提前量。在实现上，其实是所有伴奏增加延迟
     */
    public static void mergeWAVs(String destPath,
                                 String voiceFullPath,
                                 Float voiceVolume,
                                 List<String> accompanyPaths,
                                 List<Float> accompanyVolumes,
                                 int voiceOffset) {

        int wavN = accompanyPaths.size() + 1;

        String delayStr = ", adelay=" + voiceOffset + "|" + voiceOffset;
        StringBuilder inputArgs = new StringBuilder(" -i " + voiceFullPath);
        StringBuilder optionArgs = new StringBuilder("[0]volume=" + voiceVolume * wavN + "[a];");
        StringBuilder amixPrefix = new StringBuilder("[a]");

        for (int i = 1; i <= wavN - 1; ++i) {
            inputArgs.append(" -i ").append(accompanyPaths.get(i - 1));
            char codename = (char)((int)'a' + i);   // codename starts from 'b'
            optionArgs.append("[").append(i).append("]volume=").append(accompanyVolumes.get(i - 1) * wavN)
                    .append(delayStr).append("[").append(codename).append("];");
            amixPrefix.append("[").append(codename).append("]");
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append("-y").append(inputArgs).append(" -filter_complex").append(" \"").append(optionArgs).append(amixPrefix)
                .append("amix=inputs=").append(wavN).append(":duration=longest:dropout_transition=1\" ").append(destPath);
        System.out.println(cmd);

        FFmpeg.execute(cmd.toString());
    }

    /**
     * 剪切wav文件
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

    }

    /**
     * 获取wav文件的持续时间
     * @param fullPath wav文件的绝对路径
     * @return 持续时长
     */
    public static double getWAVDuration(String fullPath) {
        WavReader wavReader = new WavReader(fullPath);
        wavReader.getHeader();
        return wavReader.getDuration();
    }
}
