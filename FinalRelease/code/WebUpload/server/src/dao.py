from . import db
from .models import User, Song

'''
由用户名获取数据库中的一个用户。
'''
def get_user_by_username(username):
    return User.query.filter_by(username=username).first()

'''
获取数据库中的所有歌曲。
'''
def get_songs():
    return Song.query.all()

'''
由歌曲id获取数据库中的一首歌曲。
'''
def get_song_by_id(song_id):
    return Song.query.get(song_id)

'''
由歌曲名称与歌手获取数据库中的一首歌曲。
'''
def get_song_by_song_name_and_singer(song_name, singer):
    return Song.query.filter_by(song_name=song_name, singer=singer).first()
    
'''
获取数据库中所有歌曲的歌曲信息。
'''
def get_song_info():
    return Song.query.with_entities(Song.id, Song.song_name, Song.singer).all()

'''
由歌曲id删除数据库中的一首歌曲。
'''
def delete_song_by_id(song_id):
    song = Song.query.get(song_id)
    db.session.delete(song)
    db.session.commit()

'''
更新数据库中已有的一首歌曲。
'''
def update_song(new_song):
    db.session.merge(new_song)
    db.session.commit()

'''
在数据库中添加一首新的歌曲。
'''
def save_song(new_song):
    db.session.add(new_song)
    db.session.commit()