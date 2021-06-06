//
// Created by 66328 on 2021/4/29.
//

#include "getscore.h"
#include "setting.h"
#include "f0analysis.h"
#include "utils.h"

double baseFreq[200] = {16.352, 17.324, 18.354, 19.446, 20.602, 21.827,
                        23.125, 24.500, 25.957, 27.501, 29.136, 30.868};//C1到B1这12个音高对应的频率
char originF0Path[70];//原唱基频分析文件路径
double splittime;//基频分析采样时间间隔
double delayLowerBound;//麦克延迟下界
double delayUpperBound;//麦克延迟上界
vector<F0data> originf0;//原唱基频序列

//从文件读取用户的基频分析数据
vector<F0data> getUserF0(double startTime, double endTime, double delay) {
    vector<string> files;
    int cnt = utils::scanDir(dataDir, files);
    vector<F0data> userf0;
    for (int i = 0; i < cnt; i++) {
        stringstream filenamestream(files[i]);
        int sttimeMs;
        filenamestream >> sttimeMs;
        double sttime = sttimeMs / 1000.0 + delay;
        if (sttime > endTime || sttime + splittime < startTime)   continue;//跳过不对应的区间
        string str;
        ifstream file(dataDir + files[i]);
        while (getline(file, str)) {
            stringstream ss(str);
            double a1, a2;
            ss >> a1 >> a2;
            userf0.push_back(F0data(a1 + sttime, a2));
        }
        file.close();
    }
    sort(userf0.begin(), userf0.end());
    return userf0;
}

//JNI接口：
//以伴奏时钟为参考基准，获取startTimeInMicroMS毫秒至endTimeInMicroMS毫秒的单句演唱打分，
//返回以空格隔开且不超过100的四个非负整数，依次代表单句总分，单句音准与节奏得分，单句感情得分与单句气息得分。
//注意，调用该函数之前，必须先对[startTimeInMicroMS + delayLowerBound, endTimeInMicroMS + delayUpperBound]
//区间内的全部音频调用过f0analysis，且该区间内全部的f0analysis调用已经正常返回。
extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_getScore(JNIEnv *env, jobject thiz, jint jstartTimeInMicroMS, jint jendTimeInMicroMS) {
    int startTimeInMicroMS = jstartTimeInMicroMS;
    int endTimeInMicroMS = jendTimeInMicroMS;
    return env->NewStringUTF(getScoreWithDelay(startTimeInMicroMS / 1000.0, endTimeInMicroMS / 1000.0).c_str());
}

//获取字符串s中的第一个整数
int getFirstInterger(string s) {
    stringstream ss(s);
    int x;
    ss >> x;
    return x;
}

//获取原唱的基频分析数据
vector<F0data> getOriginF0(double startTime, double endTime) {
    vector<F0data> res;
    for (int i = 0; i < originf0.size(); i++) {
        double sttime = originf0[i].time;
        if (sttime > endTime || sttime + splittime < startTime)   continue;
        res.push_back(originf0[i]);
    }
    return res;
}

//对带延迟演唱进行综合评分
string getScoreWithDelay(double startTime, double endTime) {
    string res, tmp;
    double correctDelay = delayLowerBound;
    for (double delay = delayLowerBound; delay <= delayUpperBound; delay += f0Shift) {//枚举麦克延迟
        tmp = getAccuracyScore(startTime, endTime, delay);
        if (res == "" || getFirstInterger(res) < getFirstInterger(tmp)) res = tmp, correctDelay = delay;
    }
    int accuracyScore = getFirstInterger(res);
    int emotionScore = getEmotionScore(startTime, endTime, correctDelay, accuracyScore);
    int breathScore = getBreathScore(startTime, endTime, correctDelay, accuracyScore);
    int totScore = accuracyScore * 0.7 + emotionScore * 0.15 + breathScore * 0.15 + 0.5;
    constraintScore(accuracyScore, totScore);
    /*__android_log_print(ANDROID_LOG_INFO, "Rater",
                        "accuracyScore = %d\n", accuracyScore);
    __android_log_print(ANDROID_LOG_INFO, "Rater",
                        "emotionScore = %d\n", emotionScore);
    __android_log_print(ANDROID_LOG_INFO, "Rater",
                        "breathScore = %d\n", breathScore);
    __android_log_print(ANDROID_LOG_INFO, "Rater",
                        "totScore = %d\n", totScore);*/
    res = to_string(totScore);
    res += " " + to_string(accuracyScore);
    res += " " + to_string(emotionScore);
    res += " " + to_string(breathScore);
    return res;
}

//获取音准得分
string getAccuracyScore(double startTime, double endTime, double delay) {
    string res;
    int CorrectnessScore = getCorrectnessScore(startTime, endTime, delay);
    if (CorrectnessScore > correctnessUpperThreshold) {
        res += to_string(CorrectnessScore);
    }
    else {
        if (CorrectnessScore > correctnessLowerThreshold)
            CorrectnessScore = CorrectnessScore + (correctnessUpperThreshold - CorrectnessScore) / 3;
        res += to_string(CorrectnessScore);
    }
    return res;
}

//JNI接口：
//对歌唱打分系统进行初始化，filePath为原唱人声的基频文件(拓展名为f0a)，
//splitTimeInMicroMS代表以毫秒为单位相邻两次调用f0analysis的间隔时间，
//delayLowerThreshold和delayUpperThreshold分别代表用户录音时钟相对伴奏时钟延迟下界与上界。
//若初始化正常运行，则返回字符串为"Done"，不含引号。
//注意，在初始化结束之前，不可以调用任何歌唱打分系统的函数。filePath必须以/data/data/com.sjtu.karaoke开头。
extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_init(JNIEnv *env, jobject thiz, jstring filePath, jint splitTimeInMicroMS,
                                                jdouble _delayLowerBound, jdouble _delayUpperBound) {
    jboolean icCopy = 0;
    string cppfilePath = env->GetStringUTFChars(filePath, &icCopy);
    delayLowerBound = _delayLowerBound;
    delayUpperBound = _delayUpperBound;
    init(cppfilePath, splitTimeInMicroMS);
    return env->NewStringUTF("Done");
}

//对歌唱打分系统进行初始化
void init(string cppfilePath, int splitTimeInMicroMS) {
    memset(originF0Path, 0 ,sizeof (originF0Path));
    originf0.clear();
    strcpy(originF0Path, cppfilePath.c_str());
    splittime = splitTimeInMicroMS / 1000.0;
    vector<string> files;
    system(("rm -r " + string(dataDir)).c_str());
    system(("mkdir " + string(dataDir)).c_str());
    initbaseFreq();

    //从硬盘读取原唱的基频分析数据
    string str;
    ifstream file(originF0Path);
    while (getline(file, str)) {
        stringstream ss(str);
        double a1, a2;
        ss >> a1 >> a2;
        originf0.push_back(F0data(a1, a2));
    }
    file.close();
}

//根据十二平均律原理计算每个音高对应的频率
void initbaseFreq() {
    for (int i = 1; i <= 9; i++)
        for (int j = 0; j < 12; j++) {
            int id = j + 12 * i;
            baseFreq[id] = baseFreq[id - 12] * 2;
        }
}

//获取音准打分
int getCorrectnessScore(double startTime, double endTime, double delay) {

    vector<F0data> userf0 = getUserF0(startTime, endTime, delay);
    int userNowLowerID = 0, userNowUpperID = 0;
    double rightScore = 0, wrongPenalty = 0;

    //遍历当前乐句原唱的每个音符
    for (int i = 0; i < originf0.size(); i++) {
        double sttime = originf0[i].time;
        if (sttime > endTime || sttime + splittime < startTime)   continue;

        //two-Pointer算法结合模糊偏移找到当前用户的演唱区间
        while (userNowLowerID + 1 < userf0.size() && userf0[userNowLowerID].time + correctnessInterval < sttime) userNowLowerID++;
        while (userNowUpperID + 1 < userf0.size() && userf0[userNowUpperID].time - correctnessInterval < sttime) userNowUpperID++;

        //找出当前用户评分区间的音高最小值minPitch和最大值
        int maxPitch = INT_MIN, minPitch = INT_MAX;
        for (int j = userNowLowerID; j < userf0.size() && j <= userNowUpperID; j++) {
            minPitch = min(minPitch, userf0[j].pitchID);
            maxPitch = max(maxPitch, userf0[j].pitchID);
        }

        //计分
        if (originf0[i].pitchID >= minPitch && originf0[i].pitchID <= maxPitch) rightScore += OnceRightScore;//演唱正确
        else {
            if ((originf0[i].pitchID + 1 >= minPitch && originf0[i].pitchID + 1 <= maxPitch) ||
                (originf0[i].pitchID - 1 >= minPitch && originf0[i].pitchID - 1 <= maxPitch))//存在半音的偏差
                    wrongPenalty += OnceNotCorrectScore;//高音没上去或低音没下来
            else   wrongPenalty += OnceWrongScore;//跑调
        }
    }

    int res = rightScore / (rightScore + wrongPenalty) * 100 + 0.5;
    return res;
}

//获取情感打分
int getEmotionScore(double startTime, double endTime, double delay, int accuracyScore) {
    double userAvg = getEmotionDelta(getUserF0(startTime, endTime, delay));
    double originAvg = getEmotionDelta(getOriginF0(startTime, endTime));
    int emotionScore = accuracyScore +
                       (userAvg / originAvg - STANDARD_EMOTION_RATE) * EMOTION_SCORE_SCALE + 0.5;
    constraintScore(accuracyScore, emotionScore);
    return emotionScore;
}

//获取一段基频分析文件中的EmotionDelta
double getEmotionDelta(const vector<F0data> &F0)
{
    double sum = 0;
    for (int i = 1; i < F0.size(); i++)
        sum += fabs(F0[i].f0 - F0[i - 1].f0);
    return sum / F0.size();
}

//获取一段基频分析文件中的EndBlankCnt
int getEndBlankCnt(const vector<F0data> &F0)
{
    int res = 0;
    for (int i = F0.size() - 1; i >= 0; i--)
    {
        if (F0[i].f0)   break;
        else    res++;
    }
    return res;
}

//获取一段基频分析文件中的ContinuousBlankCnt
int getContinuousBlankCnt(const vector<F0data> &F0)
{
    int res = 0, flag = 0;
    for (int i = 0; i < F0.size(); i++)
    {
        if (F0[i].f0)
        {
            flag = 0;
        }
        else
        {
            if (!flag)  res++;
            flag = 1;
        }
    }
    return res;
}

//获取气息得分
int getBreathScore(double startTime, double endTime, double delay, int accuracyScore) {
    int userEndBlank = getEndBlankCnt(getUserF0(startTime, endTime, delay));
    int originEndBlank = getEndBlankCnt(getOriginF0(startTime, endTime));
    int userContinuousBlank = getContinuousBlankCnt(getUserF0(startTime, endTime, delay));
    int originContinuousBlank = getContinuousBlankCnt(getOriginF0(startTime, endTime));
    int breathScore = accuracyScore;
    breathScore += ((originEndBlank - userEndBlank) + STANDARD_END_BLANK_DELTA) * END_BLANK_SCORE_SCALE;
    breathScore += ((originContinuousBlank - userContinuousBlank) + STANDARD_CONTINUOUS_BLANK_DELTA) *
            CONTINUOUS_BLANK_SCORE_SCALE;
    constraintScore(accuracyScore, breathScore);

    /*__android_log_print(ANDROID_LOG_INFO, "Rater",
                        "###################################\n");
    __android_log_print(ANDROID_LOG_INFO, "Rater",
                        "userEndBlank = %d originEndBlank = %d\n", userEndBlank, originEndBlank);
    __android_log_print(ANDROID_LOG_INFO, "Rater",
                        "userContinuousBlank = %d originContinuousBlank = %d\n",
                        userContinuousBlank, originContinuousBlank);
    __android_log_print(ANDROID_LOG_INFO, "Rater",
                        "###################################\n");*/
    return breathScore;
}

//标准化得分
void constraintScore(int accuracyScore, int &scoreToConstraint)
{
    scoreToConstraint = min(scoreToConstraint, 100);
    scoreToConstraint = max(scoreToConstraint, 0);
    if (accuracyScore < MULTI_SCORE_START_LOWERBOUND)
        scoreToConstraint = min(scoreToConstraint, accuracyScore);
}

//输出某个基频分析文件
void printUserF0(const vector<F0data> &userF0)
{
    __android_log_print(ANDROID_LOG_INFO, "Rater",
                        "###################################\n");
    for (auto &it: userF0)
    {
        __android_log_print(ANDROID_LOG_INFO, "Rater",
                            "time = %.2lf f0 = %.2lf pitchID = %d\n", it.time, it.f0, it.pitchID);
    }
    __android_log_print(ANDROID_LOG_INFO, "Rater",
                        "###################################\n");

}