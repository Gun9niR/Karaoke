#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <map>
#include <set>
#include <vector>
using namespace std;
const int PITCH_CNT = 12;//支持的音域 
string getNameOfPitch[20] = {"1", "#1", "2", "#2", "3", "4", "#4", "5", "#5", "6", "#6", "7"};//音名
map<string, int> getIdOfPitch;//音名到getNameOfPitch下标的映射，即getNameOfPitch的对偶数组 
map<string, vector<int> > chordDict;//和弦字典 
set<string> usedChord;//该演奏片段实际用到的和弦 

//显示使用帮助
void showTutorial()
{
	puts("欢迎使用和弦语言解释器，下面是使用说明:");
	puts("请使用命令行调用该程序，并传入两个参数，");
	puts("分别代表需要解释的和弦文件文件名以及输出文件名");
	puts("示例：chordTranslater.exe 时间煮雨.txt 时间煮雨.chordTrans");
	puts("屏幕上提示\"和弦文件解释成功\"字样说明程序运行正常");
}

//抛出异常并终止程序
void translaterException(string s)
{
	puts(s.c_str());
	exit(-1);
}

int main(int argc, char *argv[])
{
	//检查传参是否正确 
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
		translaterException("Fail to open the chord dictionary.");
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
		translaterException("Fail to open the original chord file.");
	ofstream fileout(a3.c_str());
	if (!fileout.is_open())
		translaterException("Fail to open the output chord file.");

	//解析和弦文件头
	int bpm, delta, beatN, beatM, lastBeat;
	double startTime, timePerBeat, beatCnt = 0;
	getline(filein, line);
	stringstream ss(line);
	ss >> bpm >> delta >> beatN >> beatM >> startTime;
	if (ss.fail() || ss.bad())	translaterException("Invalid input chord file.");
	timePerBeat = 60.0 / bpm;
	fileout << int(timePerBeat * 1000 + 0.5) << " " << beatN << " " << beatM << " " << int(startTime * 1000 + 0.5) << " ";

	vector<string> outSting;
	vector<double> outDouble;
	
	//解析和弦文件主体部分
	while (getline(filein, line))
	{
		stringstream ss(line);
		ss >> chordName;
		if (ss.good())
		{
			ss >> lastBeat;
			beatCnt += lastBeat;
			if (ss.fail() || ss.bad())
				translaterException("Invalid input chord file.");
		}
		else
			translaterException("Invalid input chord file.");
		if (chordDict.find(chordName) == chordDict.end())
			translaterException("Chord not found in dictionary.");
		usedChord.insert(chordName);
		outSting.push_back(chordName);
		outDouble.push_back(timePerBeat * lastBeat);
	}
	fileout << int((startTime + beatCnt * timePerBeat) * 1000 + 0.5) << endl << endl;

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
	
	//输出和弦文件主体部分
	fileout << endl;
	for (int i = 0; i < outSting.size(); i++)
	{
		fileout << outSting[i] << " " << int(outDouble[i] * 1000 + 0.5) << endl;
	}
	
	fileout.close();
	filein.close();
	puts("Successfully parsed the chord file.");
	return 0;
}

