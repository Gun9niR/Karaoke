#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <map>
#include <set>
#include <vector>
using namespace std;

//显示使用帮助
void showTutorial()
{
	puts("欢迎使用lrc文件转换器，本程序下面是使用说明:");
	puts("请使用命令行调用该程序，并传入3个参数，");
	puts("分别代表经翻译的和弦文件文件名，需要转换的lrc文件文件名以及输出文件名");
	puts("示例：chordTranslater.exe 时间煮雨和弦.chordTrans 时间煮雨instrument.lrc 时间煮雨instrument2.lrc");
	puts("屏幕上提示\"lrc文件转换成功\"字样说明程序运行正常");
}

//抛出异常并终止程序
void convertorException(string s)
{
	puts(s.c_str());
	exit(-1);
}

int main(int argc, char *argv[])
{
	//检查传参是否正确 
	if (argc != 4)
	{
		showTutorial();
		return 0;
	}

	//初始化解释器
	string a2 = argv[1], a3 = argv[2], a4 = argv[3];

	//读取和弦文件
	fstream filein(a2.c_str());
	if (!filein.is_open())
		translaterException("打开和弦文件失败");
	int beatN, beatM;
	double startTime, timePerBeat;
	filein >> timePerBeat >> beatN >> beatM >> startTime;
	filein.close();

	ofstream fileout(a3.c_str());
	if (!fileout.is_open())
		translaterException("打开输出文件失败");


	puts("lrc文件转换成功");
	return 0;
}
