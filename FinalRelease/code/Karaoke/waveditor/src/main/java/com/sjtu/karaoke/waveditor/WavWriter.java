package com.sjtu.karaoke.waveditor;
/*
 * @ClassName: WavWriter
 * @Author: guozh
 * @Date: 2021/4/22
 * @Version: 1.3
 * @Description: A writer for wav files
 */

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.sjtu.karaoke.waveditor.WavUtils.intToByteArray;
import static com.sjtu.karaoke.waveditor.WavUtils.shortToByteArray;

public class WavWriter {
    private String fullPath;
    private DataOutputStream wavOutputStream;
    private int dataSize = 0;

    public WavWriter(String fullPath) {
        this.fullPath = fullPath;

        try {
            wavOutputStream = new DataOutputStream(new FileOutputStream(fullPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void writeHeader(WavHeader header) {
        if (wavOutputStream == null) {
            return;
        }
        if(header == null){
            return;
        }
        try {
            wavOutputStream.writeBytes(header.ritfWaveChunkID);
            wavOutputStream.write(intToByteArray((int) header.ritfWaveChunkSize), 0, 4);
            wavOutputStream.writeBytes(header.waveFormat);
            wavOutputStream.writeBytes(header.fmtChunk1ID);
            wavOutputStream.write(intToByteArray((int) header.fmtChunkSize), 0, 4);
            wavOutputStream.write(shortToByteArray((short) header.audioFormat), 0, 2);
            wavOutputStream.write(shortToByteArray((short) header.numChannel), 0, 2);
            wavOutputStream.write(intToByteArray((int) header.sampleRate), 0, 4);
            wavOutputStream.write(intToByteArray((int) header.byteRate), 0, 4);
            wavOutputStream.write(shortToByteArray((short) header.blockAlign), 0, 2);
            wavOutputStream.write(shortToByteArray((short) header.bitsPerSample), 0, 2);
            wavOutputStream.writeBytes(header.dataChunkID);
            wavOutputStream.write(intToByteArray((int) header.dataChunkSize), 0, 4);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int writeData(byte[] buffer, int offset, int count) {
        if (wavOutputStream == null) {
            return -1;
        }
        try {
            wavOutputStream.write(buffer, offset, count);
            dataSize += count;
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void writeDataSize() {
        if (wavOutputStream == null) {
            return;
        }
        try {
            RandomAccessFile wavFile = new RandomAccessFile(fullPath, "rw");
            //偏移mRitfWaveChunkID 占用的4字节
            wavFile.seek(4);
            //写入mRitfWaveChunkSize（wav文件大小）
            wavFile.write(intToByteArray((int) (dataSize + 44 - 8)), 0, 4);
            //偏移40字节，到mDataChunkSize对应的字节位置
            wavFile.seek(40);
            //写入裁剪音频的实际数据大小
            wavFile.write(intToByteArray((int) (dataSize)), 0, 4);
            wavFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //裁剪文件关闭的时候写入实际音频数据大小
    public void closeFile() throws IOException {
        if (wavOutputStream != null) {
            writeDataSize();
            wavOutputStream.close();
        }
    }
}
