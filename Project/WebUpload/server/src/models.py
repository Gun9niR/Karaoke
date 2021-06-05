from sqlalchemy.schema import UniqueConstraint
from . import db

'''
数据库中的users表格所映射到的对象。
'''
class User(db.Model):

    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    password = db.Column(db.String(200), unique=False, nullable=False)

    def __init__(self, username, password):
        self.username = username
        self.password = password

    def __repr__(self):
        return '<User %r>' % self.username

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}


'''
数据库中的songs表格所映射到的对象。
'''
class Song(db.Model):

    __tablename__ = 'songs'

    __table_args__ = (
        UniqueConstraint('song_name', 'singer'),
    )

    # Song information
    id = db.Column(db.Integer, primary_key=True)
    song_name = db.Column(db.String(255), unique=False, nullable=False)
    singer = db.Column(db.String(255), unique=False, nullable=False)
    album = db.Column(db.String(255), unique=False, nullable=False)

    # Accompany Sing
    original = db.Column(db.String(255), unique=False, nullable=False)
    accompany = db.Column(db.String(255), unique=False, nullable=False)
    lyric_accompany = db.Column(db.String(255), unique=False, nullable=False)
    mv = db.Column(db.String(255), unique=False, nullable=False)
    
    # Instrument Sing
    chord = db.Column(db.String(255), unique=False, nullable=False)
    lyric_instrument = db.Column(db.String(255), unique=False, nullable=False)
    drum = db.Column(db.String(255), unique = False, nullable=False)
    bass = db.Column(db.String(255), unique = False, nullable=False)
    orchestra = db.Column(db.String(255), unique = False, nullable=False)

    # Rating
    rate = db.Column(db.String(255), unique=False, nullable=True)

    def __init__(self, song_name, singer, album, original, accompany, 
                 lyric_accompany, mv, chord, lyric_instrument, 
                 drum, bass, orchestra, rate):
        self.song_name = song_name
        self.singer = singer
        self.album = album
        self.original = original
        self.accompany = accompany
        self.lyric_accompany = lyric_accompany
        self.mv = mv
        self.chord = chord
        self.lyric_instrument = lyric_instrument
        self.drum = drum
        self.bass = bass
        self.orchestra = orchestra
        self.rate = rate


    @classmethod
    def from_dict(cls, d_song):
        return cls(d_song['song_name'], d_song['singer'], d_song['album'],
                   d_song['original'], d_song['accompany'], d_song['lyric_accompany'],
                   d_song['mv'], d_song['chord'], d_song['lyric_instrument'],
                   d_song['drum'], d_song['bass'], d_song['orchestra'], d_song['rate'])

    def __repr__(self):
        return '<Song %r %r>' % (self.song_name, self.singer)

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}