package com.sjtu.pianorater;

//钢琴演奏评价系统
public class PianoRater {
    //加载钢琴演奏评价系统所需动态链接库。
    static {
        System.loadLibrary("PianoRater");
    }

    
    public static native String getScore(String chordTransPath, int chordCount,
                                         double[] chordTime, String[] chordName);

}
