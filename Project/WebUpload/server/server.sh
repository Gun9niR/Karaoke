#！/bin/bash
pip install -r requirements.txt
cd utils/trimmer
gcc -O2 trimmer.c -o trimmer
cd ../chordTranslator
g++ -O2 chordTranslator.cpp -std=c++11 -o chordTranslator
