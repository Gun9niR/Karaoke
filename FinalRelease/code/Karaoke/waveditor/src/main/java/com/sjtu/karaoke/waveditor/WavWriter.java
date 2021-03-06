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
            //??????mRitfWaveChunkID ?????????4??????
            wavFile.seek(4);
            //??????mRitfWaveChunkSize???wav???????????????
            wavFile.write(intToByteArray((int) (dataSize + 44 - 8)), 0, 4);
            //??????40????????????mDataChunkSize?????????????????????
            wavFile.seek(40);
            //???????????????????????????????????????
            wavFile.write(intToByteArray((int) (dataSize)), 0, 4);
            wavFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //?????????????????????????????????????????????????????????
    public void closeFile() throws IOException {
        if (wavOutputStream != null) {
            writeDataSize();
            wavOutputStream.close();
        }
    }
}
