//
// Created by 66328 on 2021/4/29.
//

#include "getscore.h"
#include "setting.h"
#include "f0analysis.h"
#include "utils.h"

double baseFreq[200] = {16.352, 17.324, 18.354, 19.446, 20.602, 21.827,
                        23.125, 24.500, 25.957, 27.501, 29.136, 30.868};
char originF0Path[70];
double splittime;
double delayLowerBound;
double delayUpperBound;
vector<F0data> originf0;


vector<F0data> getUserF0(double startTime, double endTime, double delay) {
    vector<string> files;
    int cnt = utils::scanDir(dataDir, files);
    vector<F0data> userf0;
    for (int i = 0; i < cnt; i++) {
        stringstream filenamestream(files[i]);
        int sttimeMs;
        filenamestream >> sttimeMs;
        double sttime = sttimeMs / 1000.0 + delay;
        if (sttime > endTime || sttime + splittime < startTime)   continue;
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

extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_getScore(JNIEnv *env, jobject thiz, jint jstartTimeInMicroMS, jint jendTimeInMicroMS) {
    int startTimeInMicroMS = jstartTimeInMicroMS;
    int endTimeInMicroMS = jendTimeInMicroMS;
    return env->NewStringUTF(getScoreWithDelay(startTimeInMicroMS / 1000.0, endTimeInMicroMS / 1000.0).c_str());
}

int getFirstInterger(string s) {
    stringstream ss(s);
    int x;
    ss >> x;
    return x;
}

vector<F0data> getOriginF0(double startTime, double endTime) {
    vector<F0data> res;
    for (int i = 0; i < originf0.size(); i++) {
        double sttime = originf0[i].time;
        if (sttime > endTime || sttime + splittime < startTime)   continue;
        res.push_back(originf0[i]);
    }
    return res;
}

string getScoreWithDelay(double startTime, double endTime) {
    string res, tmp;
    double correctDelay = delayLowerBound;
    for (double delay = delayLowerBound; delay <= delayUpperBound; delay += f0Shift) {
        tmp = getScore(startTime, endTime, delay);
        if (res == "" || getFirstInterger(res) < getFirstInterger(tmp)) res = tmp, correctDelay = delay;
    }
    int accuracyScore = getFirstInterger(res);
    int emotionScore = getEmotionScore(startTime, endTime, correctDelay, accuracyScore);
    int breathScore = getBreathScore(startTime, endTime, correctDelay, accuracyScore);
    int totScore = accuracyScore * 0.75 + emotionScore * 0.15 + breathScore * 0.15 + 0.5;
    constraintScore(accuracyScore, totScore);
    /*__android_log_print(ANDROID_LOG_INFO, "Rater",
                        "accuracyScore = %d\n", accuracyScore);
    __android_log_print(ANDROID_LOG_INFO, "Rater",
                        "emotionScore = %d\n", emotionScore);
    __android_log_print(ANDROID_LOG_INFO, "Rater",
                        "breathScore = %d\n", breathScore);*/
    res = to_string(totScore);
    res += " " + to_string(accuracyScore);
    res += " " + to_string(emotionScore);
    res += " " + to_string(breathScore);
    return res;
}

string getScore(double startTime, double endTime, double delay) {
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

void init(string cppfilePath, int splitTimeInMicroMS) {
    memset(originF0Path, 0 ,sizeof (originF0Path));
    originf0.clear();
    strcpy(originF0Path, cppfilePath.c_str());
    splittime = splitTimeInMicroMS / 1000.0;
    vector<string> files;
    system(("rm -r " + string(dataDir)).c_str());
    system(("mkdir " + string(dataDir)).c_str());
    initbaseFreq();
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

void initbaseFreq() {
    for (int i = 1; i <= 9; i++)
        for (int j = 0; j < 12; j++) {
            int id = j + 12 * i;
            baseFreq[id] = baseFreq[id - 12] * 2;
        }
}


int getCorrectnessScore(double startTime, double endTime, double delay) {
    vector<F0data> userf0 = getUserF0(startTime, endTime, delay);

    int userNowLowerID = 0, userNowUpperID = 0;
    double rightScore = 0, wrongPenalty = 0;
    for (int i = 0; i < originf0.size(); i++) {
        double sttime = originf0[i].time;
        if (sttime > endTime || sttime + splittime < startTime)   continue;
        while (userNowLowerID + 1 < userf0.size() && userf0[userNowLowerID].time + correctnessInterval < sttime) userNowLowerID++;
        while (userNowUpperID + 1 < userf0.size() && userf0[userNowUpperID].time - correctnessInterval < sttime) userNowUpperID++;
        int maxPitch = INT_MIN, minPitch = INT_MAX;
        for (int j = userNowLowerID; j < userf0.size() && j <= userNowUpperID; j++) {
            minPitch = min(minPitch, userf0[j].pitchID);
            maxPitch = max(maxPitch, userf0[j].pitchID);
        }
        if (originf0[i].pitchID >= minPitch && originf0[i].pitchID <= maxPitch) rightScore += OnceRightScore;
        else {
            if ((originf0[i].pitchID + 1 >= minPitch && originf0[i].pitchID + 1 <= maxPitch) ||
                (originf0[i].pitchID - 1 >= minPitch && originf0[i].pitchID - 1 <= maxPitch))   wrongPenalty += OnceNotCorrectScore;
            else   wrongPenalty += OnceWrongScore;
        }
    }

    int res = rightScore / (rightScore + wrongPenalty) * 100 + 0.5;
    return res;
}

int getEmotionScore(double startTime, double endTime, double delay, int accuracyScore) {
    double userAvg = getEmotionDelta(getUserF0(startTime, endTime, delay));
    double originAvg = getEmotionDelta(getOriginF0(startTime, endTime));
    int emotionScore = accuracyScore +
                       (userAvg / originAvg - STANDARD_EMOTION_RATE) * EMOTION_SCORE_SCALE + 0.5;
    constraintScore(accuracyScore, emotionScore);
    return emotionScore;
}

double getEmotionDelta(const vector<F0data> &F0)
{
    double sum = 0;
    for (int i = 1; i < F0.size(); i++)
        sum += fabs(F0[i].f0 - F0[i - 1].f0);
    return sum / F0.size();
}

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

void constraintScore(int accuracyScore, int &scoreToConstraint)
{
    scoreToConstraint = min(scoreToConstraint, 100);
    scoreToConstraint = max(scoreToConstraint, 0);
    if (accuracyScore < MULTI_SCORE_START_LOWERBOUND)
        scoreToConstraint = min(scoreToConstraint, accuracyScore);
}

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