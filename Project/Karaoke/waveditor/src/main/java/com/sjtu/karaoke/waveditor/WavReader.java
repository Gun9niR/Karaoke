package com.sjtu.karaoke.waveditor;
/*
 * @ClassName: WavReader
 * @Author: guozh
 * @Date: 2021/4/22
 * @Version: 1.3
 * @Description: A reader for wav files
 */

import android.util.Log;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.sjtu.karaoke.waveditor.WavUtils.byteArrayToInt;
import static com.sjtu.karaoke.waveditor.WavUtils.byteArrayToShort;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class WavReader {
    private DataInputStream wavInputStream;
    private final String TAG = "WavReader";
    private WavHeader header;
    public static final int BUFFER_LENGTH = 2048;
    /**
     * Initialize wav reader with provided full path to the wav file
     * @param fullPath Absolute path of the file
     */
    public WavReader(String fullPath) {
        try {
            wavInputStream = new DataInputStream(new FileInputStream(fullPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the header of a wav file, 44 bytes
     * @return null if no file is opened, otherwise the header
     */
    public WavHeader getHeader() {
        if (wavInputStream == null) {
            return null;
        }

        WavHeader header = new WavHeader();

        byte[] buffer = new byte[4];
        byte[] shortBuffer = new byte[2];

        try {
            wavInputStream.read(buffer);
            header.ritfWaveChunkID = new String(buffer);
            Log.d(TAG, "Read file chunkID:" + header.ritfWaveChunkID);

            wavInputStream.read(buffer);
            header.ritfWaveChunkSize = byteArrayToInt(buffer);
            Log.d(TAG, "Read file chunkSize:" + header.ritfWaveChunkSize);

            wavInputStream.read(buffer);
            header.waveFormat = new String(buffer);
            Log.d(TAG, "Read file format:" + header.waveFormat);

            wavInputStream.read(buffer);
            header.fmtChunk1ID = new String(buffer);
            Log.d(TAG, "Read fmt chunkID:" + header.fmtChunk1ID);

            wavInputStream.read(buffer);
            header.fmtChunkSize = byteArrayToInt(buffer);
            Log.d(TAG, "Read fmt chunkSize:" + header.fmtChunkSize);

            wavInputStream.read(shortBuffer);
            header.audioFormat = byteArrayToShort(shortBuffer);
            Log.d(TAG, "Read audioFormat:" + header.audioFormat);

            wavInputStream.read(shortBuffer);
            header.numChannel = byteArrayToShort(shortBuffer);
            Log.d(TAG, "Read channel number:" + header.numChannel);

            wavInputStream.read(buffer);
            header.sampleRate = byteArrayToInt(buffer);
            Log.d(TAG, "Read sample rate:" + header.sampleRate);

            wavInputStream.read(buffer);
            header.byteRate = byteArrayToInt(buffer);
            Log.d(TAG, "Read byte rate:" + header.byteRate);

            wavInputStream.read(shortBuffer);
            header.blockAlign = byteArrayToShort(shortBuffer);
            Log.d(TAG, "Read block align:" + header.blockAlign);

            wavInputStream.read(shortBuffer);
            header.bitsPerSample = byteArrayToShort(shortBuffer);
            Log.d(TAG, "Read bits persample:" + header.bitsPerSample);

            wavInputStream.read(buffer);
            header.dataChunkID = new String(buffer);
            Log.d(TAG, "Read data chunkID:" + header.dataChunkID);

            // skip LIST CHUNK of the header
            // header chunk size is 34 bytes, so skip 30 bytes
            if (header.dataChunkID.equals("LIST")) {
                Log.d(TAG, "Skip LIST chunk");
                wavInputStream.read(buffer);
                int listChunkSize = byteArrayToInt(buffer);
                System.out.println("List chunk size: " + listChunkSize);

                wavInputStream.skip(listChunkSize);
                wavInputStream.read(buffer);
                header.dataChunkID = new String(buffer);
                Log.d(TAG, "Read data chunkID:" + header.dataChunkID);
            }

            wavInputStream.read(buffer);
            header.dataChunkSize = byteArrayToInt(buffer);
            Log.d(TAG, "Read data chunkSize:" + header.dataChunkSize);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        this.header = header;
        return header;
    }

    public int readData(byte[] buffer, int offset, int count) {
        if (wavInputStream == null) {
            return 0;
        }
        try {
            return wavInputStream.read(buffer, offset, count);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void closeFile() {
        if (wavInputStream != null) {
            try {
                wavInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Move input stream to the start of the requested clip
     * @param time Starting time in ms
     * @throws IOException
     */
    public void moveToStart(int time) throws IOException {
        int skip = calculateSkip(time);
        if(wavInputStream == null){
            return;
        }
        if(skip >= wavInputStream.available()){
            return;
        }
        wavInputStream.skipBytes(skip);
    }


    /**
     * Calculate the number of bytes to skip after the header is read
     * @param time in ms
     * @return
     */
    private int calculateSkip(int time) {
        double duration = getDuration();
        return (int) ((time * 1.0 / duration) * header.dataChunkSize);
    }


    /**
     * Get the duration of a clip
     * @return Duration in ms
     */
    public double getDuration() {
        // chunkSize = duration * samplerate * bitspersample * channelnumber / 8
        return header.dataChunkSize * 1000.0 / header.sampleRate / header.bitsPerSample / header.numChannel * 8;
    }

    /**
     * Compute the number of bytes given the starting and finishing time of a clip
     * @param interval
     * @return
     */
    public int getIntervalSize(int interval) {
        double duration = getDuration();
        double rate = interval / duration;
        return (int) (header.dataChunkSize * rate);
    }
}
