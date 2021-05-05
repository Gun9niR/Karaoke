#include "setting.h"
#include "tools/audioio.h"
#include "tools/parameterio.h"
#include "world/harvest.h"
#include "world/constantnumbers.h"
#include "getscore.h"
#include "f0analysis.h"
#include "utils.h"
#include <jni.h>
#include <cinttypes>
#include <android/log.h>
#include <sys/time.h>
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "hello-libs::", __VA_ARGS__))

char originF0Path[70];
double splittime;
double delayLowerBound;
double delayUpperBound;
vector<F0data> originf0;


namespace {

//-----------------------------------------------------------------------------
// Display how to use this program
//-----------------------------------------------------------------------------
    void usage(char *argv) {
        printf("\n");
        printf(" %s - F0 estimation by Harvest\n", argv);
        printf("\n");
        printf("  usage:\n");
        printf("   %s input.wav [options]\n", argv);
        printf("  options:\n");
        printf("   -f f    : floor of frequency range (Hz) [40]\n");
        printf("   -c c    : ceil of frequency range (Hz)  [800]\n");
        printf("   -s s    : shift length (ms)             [5]\n");
        printf("   -o name : filename used for output      [output.f0]\n");
        printf("   -t      : text file is given            [binary]\n");
        printf("\n");
    }

//-----------------------------------------------------------------------------
// Set parameters from command line options
//-----------------------------------------------------------------------------
    int SetOption(int argc, char **argv, double *f0_floor, double *f0_ceil,
                  double *frame_period, char *filename, int *text_flag) {
        while (--argc) {
            if (strcmp(argv[argc], "-f") == 0) *f0_floor = atof(argv[argc + 1]);
            if (strcmp(argv[argc], "-c") == 0) *f0_ceil = atof(argv[argc + 1]);
            if (strcmp(argv[argc], "-s") == 0) *frame_period = atof(argv[argc + 1]);
            if (strcmp(argv[argc], "-o") == 0)
                snprintf(filename, 200, argv[argc + 1]);
            if (strcmp(argv[argc], "-t") == 0) *text_flag = 1;
            if (strcmp(argv[argc], "-h") == 0) {
                usage(argv[0]);
                return 0;
            }
        }
        return 1;
    }

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_f0analysis(JNIEnv *env, jobject thiz, jstring filePath, jint jstartTimeInMicroMS) {
    jboolean icCopy = 0;
    string cppfilePath = env->GetStringUTFChars(filePath, &icCopy);
    timeval t1, t2;
    gettimeofday(&t1, NULL);
    int argc = 7;
    char **argv = new char *[15];
    char a0[] = "f0analysis";
    char a1[70];
    strcpy(a1, cppfilePath.c_str());
    char a2[] = "-t";
    char a3[] = "-s";
    char a4[] = "100";
    char a5[] = "-o";
    char a6[70];
    strcpy(a6, (dataDir + to_string(jstartTimeInMicroMS) + ".f0a").c_str());
    int lena6 = strlen(a6);

    /*char a7[] = "-f";
    char a8[] = "30";
    char a9[] = "-c";
    char a10[] = "280";*/

    argv[0] = a0;
    argv[1] = a1;
    argv[2] = a2;
    argv[3] = a3;
    argv[4] = a4;
    argv[5] = a5;
    argv[6] = a6;

    /*argv[7] = a7;
    argv[8] = a8;
    argv[9] = a9;
    argv[10] = a10;*/

    // Default parameters
    HarvestOption option = { 0 };
    InitializeHarvestOption(&option);
    option.frame_period = 5.0;
    option.f0_floor = world::kFloorF0;
    option.f0_ceil = world::kCeilF0;
    char filename[200] = "output.f0";
    int text_flag = 0;

    // Options from command line
    if (SetOption(argc, argv, &option.f0_floor, &option.f0_ceil,
                  &option.frame_period, filename, &text_flag) == 0) return env->NewStringUTF("-1");

    // Read an audio file
    int x_length = GetAudioLength(argv[1]);
    if (x_length <= 0) {
        if (x_length == 0) {
            printf("error: File not found.\n");
        } else {
            printf("error: File is not .wav format.\n");
        }
        return env->NewStringUTF("-2");
    }
    double *x = new double[x_length];
    int fs, nbit;
    wavread(argv[1], &fs, &nbit, x);

    // F0 analysis
    int number_of_frames =
            GetSamplesForHarvest(fs, x_length, option.frame_period);
    double *f0 = new double[number_of_frames];
    double *temporal_positions = new double[number_of_frames];
    Harvest(x, x_length, fs, &option, temporal_positions, f0);

    // File output
    WriteF0(filename, number_of_frames, option.frame_period, temporal_positions,
            f0, text_flag);

    // Memory deallocation
    delete[] f0;
    delete[] temporal_positions;
    delete[] x;
    delete[] argv;
    gettimeofday(&t2, NULL);
    string res = to_string(t2.tv_sec - t1.tv_sec + (t2.tv_usec - t1.tv_usec) / 1e6);
    return env->NewStringUTF(res.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_getScore(JNIEnv *env, jobject thiz, jint jstartTimeInMicroMS, jint jendTimeInMicroMS) {
    int startTimeInMicroMS = jstartTimeInMicroMS;
    int endTimeInMicroMS = jendTimeInMicroMS;
    return env->NewStringUTF(getScoreWithDelay(startTimeInMicroMS / 1000.0, endTimeInMicroMS / 1000.0).c_str());
}

int getFirstScore(string s) {
    stringstream ss(s);
    int x;
    ss >> x;
    return x;
}

string getScoreWithDelay(double startTime, double endTime) {
    string res, tmp;
    for (double delay = delayLowerBound; delay <= delayLowerBound; delay += f0Shift) {
        tmp = getScore(startTime, endTime, delay);
        if (res == "" || getFirstScore(res) < getFirstScore(tmp)) res = tmp;
    }
    return res;
}

string getScore(double startTime, double endTime, double delay) {
    string res;
    int CorrectnessScore = getCorrectnessScore(startTime, endTime, delay);
    if (CorrectnessScore > correctnessThreshold) {
        res += to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
    }
    else {
        res += to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
        res += " " + to_string(CorrectnessScore);
    }
    return res;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_sjtu_karaoke_singrater_RatingUtil_init(JNIEnv *env, jobject thiz, jstring filePath, jint splitTimeInMicroMS,
        jdouble delayLowerBound, jdouble delayUpperBound) {
    jboolean icCopy = 0;
    string cppfilePath = env->GetStringUTFChars(filePath, &icCopy);
    init(cppfilePath, splitTimeInMicroMS);
    return env->NewStringUTF("Done");
}

void init(string cppfilePath, int splitTimeInMicroMS) {
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
