package com.sjtu.karaoke.waveditor;
/*
 * @ClassName: WavHeader
 * @Author: guozh
 * @Date: 2021/4/22
 * @Version: 1.3
 * @Description: Header for a .wav file
 */

public class WavHeader {
    //RITF标志
    public String ritfWaveChunkID;
    //wav文件大小（总大小-8）
    public int ritfWaveChunkSize;
    //wav格式
    public String waveFormat;
    //格式数据块ID：值为"fmt "(注意后面有个空格)
    public String fmtChunk1ID;
    //格式数据块大小，一般为16
    public int fmtChunkSize;
    //数据格式，一般为1，表示音频是pcm编码
    public short audioFormat;
    //声道数
    public short numChannel;
    //采样率
    public int sampleRate;
    //每秒字节数
    public int byteRate;
    //数据块对齐单位
    public short blockAlign;
    //采样位数
    public short bitsPerSample;
    //data块，音频的真正数据块
    public String dataChunkID;
    //音频实际数据大小
    public int dataChunkSize;
}
