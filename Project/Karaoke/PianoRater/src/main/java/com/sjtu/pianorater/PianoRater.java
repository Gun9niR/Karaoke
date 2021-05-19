package com.sjtu.pianorater;

//钢琴演奏评价系统
public class PianoRater {
    //加载钢琴演奏评价系统所需动态链接库。
    static {
        System.loadLibrary("PianoRater");
    }
    //以伴奏时钟为参考基准，获取startTimeInMicroMS毫秒至endTimeInMicroMS毫秒的单句演唱打分，
    //返回以空格隔开且不超过100的四个非负整数，依次代表单句总分，单句音准与节奏得分，单句感情得分与单句气息得分。
    //注意，调用该函数之前，必须先对[startTimeInMicroMS + delayLowerBound, endTimeInMicroMS + delayUpperBound]
    //区间内的全部音频调用过f0analysis，且该区间内全部的f0analysis调用已经正常返回。
    public static native String getScore(String chordTransPath, int chordCount, double[] chord);

}
