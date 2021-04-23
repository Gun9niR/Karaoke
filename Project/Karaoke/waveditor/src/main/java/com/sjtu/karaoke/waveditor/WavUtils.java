package com.sjtu.karaoke.waveditor;
/*
 * @ClassName: WavUtils
 * @Author: guozh
 * @Date: 2021/4/22
 * @Version: 1.3
 * @Description: TODO
 */

public class WavUtils {
    /**
     * Small endian
     */
    public static int byteArrayToInt(byte[] buffer) {
        return ((buffer[0] & 0xFF)) |
                ((buffer[1] & 0xFF) << 8) |
                ((buffer[2] & 0xFF) << 16) |
                ((buffer[3] & 0xFF) << 24);
    }

    /**
     * Small endian
     */
    public static short byteArrayToShort(byte[] buffer) {
        return (short) (((buffer[0] & 0xFF)) |
                ((buffer[1] & 0xFF) << 8));
    }

    /**
     * Small endian
     */
    public static byte[] intToByteArray(int x) {
        return new byte[] {
                (byte) (x & 0xFF),
                (byte) ((x >> 8) & 0xFF),
                (byte) ((x >> 16) & 0xFF),
                (byte) ((x >> 24) & 0xFF)
        };
    }

    /**
     * Small endian
     */
    public static byte[] shortToByteArray(short x) {
        return new byte[] {
                (byte) (x & 0xFF),
                (byte) ((x >> 8) & 0xFF)
        };
    }
}
