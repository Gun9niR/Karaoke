//
// Created by 66328 on 2021/4/29.
//

#ifndef HELLO_LIBS_SETTING_H
#define HELLO_LIBS_SETTING_H
#include <cstring>

#include <string>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include <vector>
#include <fstream>
#include <sstream>
#include <iostream>
#include <algorithm>

#include <jni.h>
#include <cinttypes>
#include <android/log.h>
#include <sys/time.h>
using namespace std;

const char dataDir[] = "/data/data/com.sjtu.karaoke/raterdata/";//歌唱打分系统数据存储路径
const float correctnessInterval = 0.25;//音准打分的模糊偏移
const int correctnessUpperThreshold = 68;//强制向上提分的上界区间
const int correctnessLowerThreshold = 50;//强制向上提分的下界区间
const float OnceRightScore = 1.25;//演唱正确的加分
const float OnceNotCorrectScore = 0.3;//音高不精确的扣分
const float OnceWrongScore = 0.9;//跑掉的扣分
const float f0Shift = 0.1;//基频分析采样时间间隔
const float STANDARD_EMOTION_RATE = 2.35;//标准用户原唱情感差比值：standard越高，得分越低
const float EMOTION_SCORE_SCALE = 3.5;//用户原唱情感比值的得分放缩
const float STANDARD_END_BLANK_DELTA = 2.8;//标准用户原唱结尾空白比值：standard越高，得分越高
const float END_BLANK_SCORE_SCALE = 1.5;//用户原唱结尾空白比值的得分放缩
const float STANDARD_CONTINUOUS_BLANK_DELTA = 2.15;//标准用户原唱中间连续空白比值：standard越高，得分越高
const float CONTINUOUS_BLANK_SCORE_SCALE = 2.5;//用户原唱中间连续空白比值的得分放缩
const float MULTI_SCORE_START_LOWERBOUND = 50;//综合评分的用户音准分起始阈值

#endif //HELLO_LIBS_SETTING_H
