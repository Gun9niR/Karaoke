#include "pianoRater.h"

Beat *beat;
int chordCount, errorCount, beatCount;
string chordTransPath, *chordName;
double *chord;
double *originChord;
int beatN, beatM;
double startTime, endTime, timePerBeat;

//JNI接口：
//对用户的钢琴演奏进行评级。chordTransPath代表.chordTrans的文件路径，
//chordCount代表用户演奏的和弦总数，
//数组chordTime代表用户演奏每个和弦的时刻(以演奏片段开始为计时原点，以ms为单位)
//数组chordName代表用户演奏每个和弦的和弦名称
//返回一个[0,100]的正整数，代表用户的钢琴演奏评级
extern "C"
JNIEXPORT jstring JNICALL
Java_com_sjtu_pianorater_PianoRater_getScore(JNIEnv *env, jclass clazz, jstring _chordTransPath,
                                             jint _chordCount, jdoubleArray _chord, jobjectArray _chordName) {
    srand(time(NULL));
    chordCount = _chordCount;
    errorCount = 0;
    chordTransPath = env->GetStringUTFChars(_chordTransPath,NULL);
    chordName = new string[chordCount];
    chord = new double[chordCount];
    originChord = new double[chordCount];
    for (int i = 0; i < chordCount; i++) {
        jstring str = static_cast<jstring>(env->GetObjectArrayElement(_chordName, i));
        const char* s = env->GetStringUTFChars(str,NULL);
        chordName[i] = s;
        originChord[i] = env->GetDoubleArrayElements(_chord, NULL)[i];
        /*__android_log_print(ANDROID_LOG_INFO, "Rater",
                            "Java_com_sjtu_pianorater_PianoRater_getScore: "
                            "chordName = %s originChord = %.2lf\n",
                            chordName[i].c_str(), originChord[i]);*/
    }
    return env->NewStringUTF(runRater().c_str());
}

//运行钢琴演奏评价系统
string runRater() {
    readFile();
    string res = "0";
    for (int i = 0; i <= RATE_PRECISION; i++) {//枚举音频硬件延迟
        for (int j = 0; j < chordCount; j++) {
            if (i <= RATE_PRECISION * 0.5)
                chord[j] = originChord[j] + MIK_DELAY * (i / (RATE_PRECISION * 0.5));
            else
                chord[j] = originChord[j] - MIK_DELAY * ((i - RATE_PRECISION * 0.5) /
                        (RATE_PRECISION * 0.5));
        }
        classifyBeat();
        string tmp = getScore();
        if (stoi(tmp) > stoi(res))  res = tmp;
    }
    recycleSpace();
    return res;
}

//读取和弦文件
void readFile() {
    ifstream file(chordTransPath);
    string str;
    int curBeat = 0;
    //读入文件头
    file >> timePerBeat >> beatN >> beatM >> startTime >> endTime;
    getline(file, str);getline(file, str);
    beatCount = (endTime - startTime) / timePerBeat + 0.5;
    beat = new Beat[beatCount + 5];
    //读入和弦字典
    while (getline(file, str)) {
        if (str == "" || str == "\r")  break;
    }
    //读入和弦
    while (getline(file, str)) {
        if (str == "" || str == "\r")  break;
        stringstream ss(str);
        string arg1;
        int arg2, lastBeat;
        ss >> arg1 >> arg2;
        lastBeat = arg2 / timePerBeat + 0.5;
        for (int i = 0; i < lastBeat; i++) {
            beat[curBeat].chordName = arg1;
            beat[curBeat].id = i;
            /*__android_log_print(ANDROID_LOG_INFO, "Rater",
                                "readFile: beat[%d].chordName=%s\n",
                                curBeat, beat[curBeat].chordName.c_str());*/
            curBeat++;
        }

    }
}

//回收内存
void recycleSpace() {
    delete[] chordName;
    delete[] chord;
    delete[] originChord;
    delete[] beat;
}

//把用户演奏的一个音符插入到该节拍
void Beat::insert(double time) {
    double minval = INT_MAX;
    int id = 0;
    for (int i = 0; i < SPLIT_NUMBER; i++) {
        double timeNow = timePerBeat * i / SPLIT_NUMBER;
        if (fabs(timeNow - time) < minval) {
            minval = fabs(timeNow - time);
            id = i;
        }
    }
    //if (delay[id] != -1)    errorCount++;
    if (delay[id] != -1)    ;
    else    delay[id] = minval;
}

//检测id是否合法
bool validID(int id) {
    return id >= 0 && id < beatCount;
}

//把用户演奏的音符插入到各个节拍之中
void classifyBeat() {
    errorCount = 0;
    for (int i = 0; i < chordCount; i++) {
        int id = chord[i] / timePerBeat;
        /*__android_log_print(ANDROID_LOG_INFO, "Rater",
                            "classifyBeat: chordName = %s beat[id].chordName = %s\n",
                            chordName[i].c_str(), beat[id].chordName.c_str());
        __android_log_print(ANDROID_LOG_INFO, "Rater",
                            "classifyBeat: info! chord[i] = %.2lf id = %d timePerBeat = %.2lf\n",
                            chord[i], id, timePerBeat);*/
        if (validID(id) && chordName[i] == beat[id].chordName)
            beat[id].insert(chord[i] - id * timePerBeat);
        else
            if (validID(id + 1) && chord[i] > (id + 0.5) * timePerBeat && chordName[i] == beat[id + 1].chordName)
                beat[id + 1].insert(chord[i] - (id + 1) * timePerBeat);
        else
            if (validID(id - 1) && chord[i] < (id + 0.5) * timePerBeat && chordName[i] == beat[id - 1].chordName)
                beat[id - 1].insert(chord[i] - (id - 1) * timePerBeat);
            else
            {
                __android_log_print(ANDROID_LOG_INFO, "Rater",
                                    "classifyBeat: ErrorPlay! chord[i] = %.2lf id = %d timePerBeat = %.2lf\n",
                                    chord[i], id, timePerBeat);
                errorCount++;
            }
    }
}

//利用数学函数模拟的方法获取该次演奏的得分(设计模式为策略模式)
double Beat::calcScoreWithMath(double delay) {
    double y = -40 * pow(delay, 1.5) + 10;
    return y >= 0 ? y : 0;
}

//获取该节拍的得分
double Beat::getScore() {
    int cnt = 0;
    double sum = 0;
    for (int i = 0; i < SPLIT_NUMBER; i++) {
        if (delay[i] != -1) {
            sum += calcScoreWithMath(delay[i] / timePerBeat);
            cnt++;
        }
    }
    return cnt == 0 ? 6 : sum / cnt;
}

//获取得分
string getScore() {
    double score = 0;
    for (int i = 0; i < beatCount; i++) {//逐节拍评分
        score += beat[i].getScore();
    }
    score /= beatCount;
    /*__android_log_print(ANDROID_LOG_INFO, "Rater",
                        "getScore: score before punish = %lf\n", score);*/
    score -= errorCount * PER_ERR_PUNISH;
    /*__android_log_print(ANDROID_LOG_INFO, "Rater",
                        "getScore: score after punish = %lf\n", score);*/
    score *= 10;
    //演奏次数过少则限制得分
    if (chordCount <= 5)    score = min(score, 20.0);
    if (chordCount <= beatCount * 0.25 - 6)    score = min(score, 33.0);
    if (chordCount <= beatCount * 0.5 - 6)    score = min(score, 70.0);
    if (chordCount <= beatCount - 6)    score = min(score, 84.0);
    if (chordCount <= beatCount * 1.5 - 6)    score = min(score, 93.0);

    //标准化得分
    score += rand() % 9 - 4;
    score = max(score, 0.0);
    score = min(score, 100.0);
    /*__android_log_print(ANDROID_LOG_INFO, "Rater",
                        "getScore: score after restrict = %lf\n", score);*/
    return to_string(int(score));
}


