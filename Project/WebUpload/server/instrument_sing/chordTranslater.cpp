#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <map>
#include <set>
#include <vector>
using namespace std;
const int PITCH_CNT = 12;
string getNameOfPitch[20] = {"1", "#1", "2", "#2", "3", "4", "#4", "5", "#5", "6", "#6", "7"};
map<string, int> getIdOfPitch;
map<string, vector<int> > chordDict;
set<string> usedChord;

//显示使用帮助
void showTutorial()
{
	puts("欢迎使用和弦语言解释器，下面是使用说明:");
	puts("请使用命令行调用该程序，并传入两个参数，");
	puts("分别代表需要解释的和弦文件文件名以及输出文件名");
	puts("示例：chordTranslater.exe 时间煮雨.txt 时间煮雨.chordTrans");
}

//抛出异常并终止程序
void translaterException(string s)
{
	puts(s.c_str());
	exit(-1);
}

int main(int argc, char *argv[])
{
	if (argc != 3)
	{
		showTutorial();
		return 0;
	}

	//初始化解释器
	string chordName, line, str;
	string a2 = argv[1], a3 = argv[2];
	for (int i = 0; i < PITCH_CNT; i++)
		getIdOfPitch[getNameOfPitch[i]] = i;

	//读取和弦字典文件
	fstream filein("chorddict.txt");
	if (!filein.is_open())
		translaterException("打开和弦字典文件失败");
	while (getline(filein, line))
	{
		stringstream ss(line);
		ss >> chordName;
		vector<int> structure;
		while (ss.good())
		{
			ss >> str;
			structure.push_back(getIdOfPitch[str]);
		}
		chordDict[chordName] = structure;
	}
	filein.close();

	//打开输入输出文件
	filein.open(a2.c_str());
	if (!filein.is_open())
		translaterException("打开和弦文件失败");
	ofstream fileout(a3.c_str());
	if (!fileout.is_open())
		translaterException("打开输出文件失败");

	//解析和弦文件头
	int bpm, delta, beatN, beatM, lastBeat;
	double startTime, timePerBeat;
	getline(filein, line);
	stringstream ss(line);
	ss >> bpm >> delta >> beatN >> beatM >> startTime;
	if (ss.fail() || ss.bad())	translaterException("输入的和弦文件不合法");
	timePerBeat = 60.0 / bpm;
	fileout << timePerBeat << " " << beatN << " " << beatM << " " << startTime << endl
			<< endl;

	//解析和弦文件主体部分
	while (getline(filein, line))
	{
		stringstream ss(line);
		ss >> chordName;
		if (ss.good())
		{
			ss >> lastBeat;
			if (ss.fail() || ss.bad())
				translaterException("输入的和弦文件不合法");
		}
		else
			translaterException("输入的和弦文件不合法");
		if (chordDict.find(chordName) == chordDict.end())
			translaterException("输入的和弦不在和弦字典中");
		usedChord.insert(chordName);
		fileout << chordName << " " << timePerBeat * lastBeat << endl;
	}
	fileout << endl;

	//根据移调附加本乐段所需和弦字典
	for (auto &it : usedChord)
	{
		auto curStruct = chordDict[it];
		fileout << it << " ";
		for (auto curPitch : curStruct)
		{
			int id = (curPitch + delta + PITCH_CNT) % PITCH_CNT;
			fileout << getNameOfPitch[id] << " ";
		}
		fileout << endl;
	}
	fileout.close();
	filein.close();
	return 0;
}
