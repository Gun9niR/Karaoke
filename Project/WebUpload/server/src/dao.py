from . import db
from .models import User, Song

def get_user_by_username(username):
    return User.query.filter_by(username=username).first()

def get_songs():
    return Song.query.all()

def get_song_by_id(song_id):
    return Song.query.get(song_id)

def get_song_by_song_name_and_singer(song_name, singer):
    return Song.query.filter_by(song_name=song_name, singer=singer).first()
    
def get_song_info():
    return Song.query.with_entities(Song.id, Song.song_name, Song.singer).all()

def delete_song_by_id(song_id):
    song = Song.query.get(song_id)
    db.session.delete(song)
    db.session.commit()

def update_song(new_song):
    db.session.merge(new_song)
    db.session.commit()

def save_song(new_song):
    db.session.add(new_song)
    db.session.commit()