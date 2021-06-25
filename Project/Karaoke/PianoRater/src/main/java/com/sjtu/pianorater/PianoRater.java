package com.sjtu.pianorater;

//钢琴演奏评价系统
public class PianoRater {
    //加载钢琴演奏评价系统所需动态链接库。
    static {
        System.loadLibrary("PianoRater");
    }

    //对用户的钢琴演奏进行评级。chordTransPath代表.chordTrans的文件路径，
    //chordCount代表用户演奏的和弦总数，
    //数组chordTime代表用户演奏每个和弦的时刻(以演奏片段开始为计时原点，以ms为单位)
    //数组chordName代表用户演奏每个和弦的和弦名称
    //返回一个[0,100]的正整数，代表用户的钢琴演奏评级
    public static native String getScore(String chordTransPath, int chordCount,
                                         double[] chordTime, String[] chordName);

}
