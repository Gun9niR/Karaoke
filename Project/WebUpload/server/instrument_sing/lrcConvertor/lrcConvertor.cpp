#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <map>
#include <set>
#include <vector>
using namespace std;

//��ʾʹ�ð���
void showTutorial()
{
	puts("��ӭʹ��lrc�ļ�ת������������������ʹ��˵��:");
	puts("��ʹ�������е��øó��򣬲�����3��������");
	puts("�ֱ��������ĺ����ļ��ļ�������Ҫת����lrc�ļ��ļ����Լ�����ļ���");
	puts("ʾ����chordTranslater.exe ʱ���������.chordTrans ʱ������instrument.lrc ʱ������instrument2.lrc");
	puts("��Ļ����ʾ\"lrc�ļ�ת���ɹ�\"����˵��������������");
}

//�׳��쳣����ֹ����
void convertorException(string s)
{
	puts(s.c_str());
	exit(-1);
}

int main(int argc, char *argv[])
{
	//��鴫���Ƿ���ȷ 
	if (argc != 4)
	{
		showTutorial();
		return 0;
	}

	//��ʼ��������
	string a2 = argv[1], a3 = argv[2], a4 = argv[3];

	//��ȡ�����ļ�
	fstream filein(a2.c_str());
	if (!filein.is_open())
		translaterException("�򿪺����ļ�ʧ��");
	int beatN, beatM;
	double startTime, timePerBeat;
	filein >> timePerBeat >> beatN >> beatM >> startTime;
	filein.close();

	ofstream fileout(a3.c_str());
	if (!fileout.is_open())
		translaterException("������ļ�ʧ��");


	puts("lrc�ļ�ת���ɹ�");
	return 0;
}
