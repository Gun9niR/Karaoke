import os
import sys
from dotenv import load_dotenv

basedir = os.path.abspath(os.path.dirname(__file__))
load_dotenv(os.path.join(basedir, '.env'))

class Config:

    SECRET_KEY = os.environ.get('SECRET_KEY')
    FLASK_APP = os.environ.get('FLASK_APP')
    FLASK_ENV = os.environ.get('FLASK_ENV')

    SQLALCHEMY_DATABASE_URI = 'mysql+pymysql://root:haveagoodday@localhost/karaoke?charset=utf8mb4'
    SQLALCHEMY_ECHO = False
    SQLALCHEMY_TRACK_MODIFICATIONS = False

    REQUIRE_SHELL = sys.platform == 'win32'

    FILE_UPLOAD_DIR = '/Users/cyx/College/2021Spring/SE/test/'
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