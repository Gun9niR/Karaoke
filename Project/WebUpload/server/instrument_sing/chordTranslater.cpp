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

//��ʾʹ�ð���
void showTutorial()
{
	puts("��ӭʹ�ú������Խ�������������ʹ��˵��:");
	puts("��ʹ�������е��øó��򣬲���������������");
	puts("�ֱ������Ҫ���͵ĺ����ļ��ļ����Լ�����ļ���");
	puts("ʾ����chordTranslater.exe ʱ������.txt ʱ������.chordTrans");
}

//�׳��쳣����ֹ����
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

	//��ʼ��������
	string chordName, line, str;
	string a2 = argv[1], a3 = argv[2];
	for (int i = 0; i < PITCH_CNT; i++)
		getIdOfPitch[getNameOfPitch[i]] = i;

	//��ȡ�����ֵ��ļ�
	fstream filein("chorddict.txt");
	if (!filein.is_open())
		translaterException("�򿪺����ֵ��ļ�ʧ��");
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

	//����������ļ�
	filein.open(a2.c_str());
	if (!filein.is_open())
		translaterException("�򿪺����ļ�ʧ��");
	ofstream fileout(a3.c_str());
	if (!fileout.is_open())
		translaterException("������ļ�ʧ��");

	//���������ļ�ͷ
	int bpm, delta, beatN, beatM, lastBeat;
	double startTime, timePerBeat;
	getline(filein, line);
	stringstream ss(line);
	ss >> bpm >> delta >> beatN >> beatM >> startTime;
	if (ss.fail() || ss.bad())	translaterException("����ĺ����ļ����Ϸ�");
	timePerBeat = 60.0 / bpm;
	fileout << timePerBeat << " " << beatN << " " << beatM << " " << startTime << endl
			<< endl;

	//���������ļ����岿��
	while (getline(filein, line))
	{
		stringstream ss(line);
		ss >> chordName;
		if (ss.good())
		{
			ss >> lastBeat;
			if (ss.fail() || ss.bad())
				translaterException("����ĺ����ļ����Ϸ�");
		}
		else
			translaterException("����ĺ����ļ����Ϸ�");
		if (chordDict.find(chordName) == chordDict.end())
			translaterException("����ĺ��Ҳ��ں����ֵ���");
		usedChord.insert(chordName);
		fileout << chordName << " " << timePerBeat * lastBeat << endl;
	}
	fileout << endl;

	//�����Ƶ����ӱ��ֶ���������ֵ�
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
