#include "setting.h"

//节拍
class Beat {
public:
    Beat(string _chordName = ""): chordName(_chordName) {
        memset(delay, -1, sizeof(delay));
    }
    int id;//节拍编号
    string chordName;//该节拍的和弦名称
    int delay[SPLIT_NUMBER + 1];//演奏延迟(ms)
    void insert(double time);//把用户演奏的一个音符插入到该节拍
    double getScore();//获取该节拍的得分
    double calcScoreWithMath(double delay);//利用数学函数模拟的方法获取该次演奏的得分(设计模式为策略模式)
};

//JNI接口
extern "C"
JNIEXPORT jstring JNICALL
Java_com_sjtu_pianorater_PianoRater_getScore(JNIEnv *env, jclass clazz, jstring _chordTransPath,
                                             jint _chordCount, jdoubleArray _chord, jobjectArray _chordName);

//运行钢琴演奏评价系统
string runRater();

//读取和弦文件
void readFile();

//把用户演奏的音符插入到各个节拍之中
void classifyBeat();

//把用户演奏的音符插入到各个节拍之中
string getScore();

//回收内存
void recycleSpace();