#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <map>
#include <set>
#include <vector>
using namespace std;
const int PITCH_CNT = 12;//֧�ֵ����� 
string getNameOfPitch[20] = {"1", "#1", "2", "#2", "3", "4", "#4", "5", "#5", "6", "#6", "7"};//����
map<string, int> getIdOfPitch;//������getNameOfPitch�±��ӳ�䣬��getNameOfPitch�Ķ�ż���� 
map<string, vector<int> > chordDict;//�����ֵ� 
set<string> usedChord;//������Ƭ��ʵ���õ��ĺ��� 

//��ʾʹ�ð���
void showTutorial()
{
	puts("��ӭʹ�ú������Խ�������������ʹ��˵��:");
	puts("��ʹ�������е��øó��򣬲���������������");
	puts("�ֱ������Ҫ���͵ĺ����ļ��ļ����Լ�����ļ���");
	puts("ʾ����chordTranslater.exe ʱ������.txt ʱ������.chordTrans");
	puts("��Ļ����ʾ\"�����ļ����ͳɹ�\"����˵��������������");
}

//�׳��쳣����ֹ����
void translaterException(string s)
{
	puts(s.c_str());
	exit(-1);
}

int main(int argc, char *argv[])
{
	//��鴫���Ƿ���ȷ 
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

	//����������ļ�
	filein.open(a2.c_str());
	if (!filein.is_open())
		translaterException("Fail to open the original chord file.");
	ofstream fileout(a3.c_str());
	if (!fileout.is_open())
		translaterException("Fail to open the output chord file.");

	//���������ļ�ͷ
	int bpm, delta, beatN, beatM, lastBeat;
	double startTime, timePerBeat, beatCnt = 0;
	getline(filein, line);
	stringstream ss(line);
	ss >> bpm >> delta >> beatN >> beatM >> startTime;
	if (ss.fail() || ss.bad())	translaterException("Invalid input chord file.");
	timePerBeat = 60.0 / bpm;
	fileout << timePerBeat * 1000 << " " << beatN << " " << beatM << " " << startTime * 1000 << " ";

	vector<string> outSting;
	vector<double> outDouble;
	
	//���������ļ����岿��
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
	fileout << (startTime + beatCnt * timePerBeat) * 1000 << endl << endl;

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
	
	//��������ļ����岿��
	fileout << endl;
	for (int i = 0; i < outSting.size(); i++)
	{
		fileout << outSting[i] << " " << outDouble[i] * 1000 << endl;
	}
	
	fileout.close();
	filein.close();
	puts("Successfully parsed the chord file.");
	return 0;
}
