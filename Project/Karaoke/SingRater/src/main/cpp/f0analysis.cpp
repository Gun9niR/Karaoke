#include "setting.h"
#include "tools/audioio.h"
#include "tools/parameterio.h"
#include "world/harvest.h"
#include "world/constantnumbers.h"
#include "f0analysis.h"
#include "utils.h"

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "hello-libs::", __VA_ARGS__))



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

