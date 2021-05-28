from os import environ, path
from dotenv import load_dotenv

basedir = path.abspath(path.dirname(__file__))
load_dotenv(path.join(basedir, '.env'))

class Config:

    SECRET_KEY = environ.get('SECRET_KEY')
    FLASK_APP = environ.get('FLASK_APP')
    FLASK_ENV = environ.get('FLASK_ENV')

    SQLALCHEMY_DATABASE_URI = 'mysql+pymysql://root:@localhost/karaoke'
    SQLALCHEMY_ECHO = False
    SQLALCHEMY_TRACK_MODIFICATIONS = False

    FILE_UPLOAD_DIR = 'C:\Academic\UnderGraduate\Sophomore-2\Principle_and_Practice_of_Software_Engineering\project\data'
    WORKING_DIR = './utils/'
    CHORD_TRANS_WORKING_DIR = './utils/chordTranslator/'

    TRIMMER_PATH = './trimmer/trimmer'
    RATING_PATH = './rating/build/f0analysis'
    CHORD_TRANSLATOR_PATH = './chordTranslator'

    CHORD_TRANS_FILENAME = 'chord.chordTrans'
    LYRIC_INSTRUMENT_FILENAME = 'lyric_instrument.lrc'
    DRUM_FILENAME = 'drums.wav'
    BASS_FILENAME = 'bass.wav'
    ORCHESTRA_FILENAME = 'other.wav'
    RATE_FILENAME = 'rate.f0a'
    TRIMMED_WAV_FILENAME = 'frag.wav'

    BUTTON_ANI_SEC = 3