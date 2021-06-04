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

const char DATA_DIR[] = "/data/data/com.sjtu.karaoke/raterdata/";
const double MIK_DELAY = 0.8;
const int RATE_PRECISION = 100;
const int SPLIT_NUMBER = 4;
const double PER_ERR_PUNISH = 0.6;

#endif //HELLO_LIBS_SETTING_H
