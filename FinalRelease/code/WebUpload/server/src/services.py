from flask import current_app as app
import os
import shutil
from . import socketio
from . import dao, utils
from .models import Song
from .exceptions import *
from .verify import UserVerification
from threading import Thread


'''
认证用户的登录信息。

参数:
    username: 用户输入的用户名。
    password: 用户输入的登录密码。

返回:
    用户信息构成的字典。

抛出:
    UserNotExistException: 用户名不存在。
    WrongPasswordException: 用户输入的密码错误。
'''
def verify_user(username, password):
    user_in_db = dao.get_user_by_username(username)
    if user_in_db is None:
        raise UserNotExistException

    d_user = user_in_db.as_dict()
    user_v = UserVerification(d_user)
    if not user_v.verify_password(password):
        raise WrongPasswordException

    return d_user
    

'''
获取数据库中的所有歌曲。

返回:
    一个包含数据库中所有歌曲的列表，每首歌曲都为字典形式。
'''
def get_songs():
    songs_in_db = dao.get_songs()
    return [song_in_db.as_dict() for song_in_db in songs_in_db]


'''
删除一首歌曲。

参数:
    song_id: 要删除的歌曲的id。
'''
def delete_song(song_id):

    # 删除服务器文件
    d_song = dao.get_song_by_id(song_id).as_dict()
    delete_dir = utils.generate_song_directory(d_song)
    shutil.rmtree(delete_dir)

    # 删除数据库中的歌曲信息
    dao.delete_song_by_id(song_id)


'''
更新歌曲信息（歌曲名称与歌手）。

参数:
    req_song: 要更新的歌曲的字典形式，包括歌曲名称、歌手与各文件的路径信息。

抛出:
    DuplicateSongException: 数据库中有另一首歌曲名称与歌手完全相同的歌曲。
'''
def update_song_info(req_song):

    song_id = req_song['id']
    song_name = req_song['song_name']
    singer = req_song['singer']

    print(song_name, singer)

    song_in_db = dao.get_song_by_song_name_and_singer(song_name, singer)
    if song_in_db:
        d_song = song_in_db.as_dict()
        if d_song['id'] != song_id:
            raise DuplicateSongException
    
    # 获取更新后歌曲的存储目录
    new_dir = utils.generate_song_directory(req_song)
    if os.path.exists(new_dir):
        return

    # 重命名现有目录
    prev_song_in_db = dao.get_song_by_id(song_id)
    prev_d_song = prev_song_in_db.as_dict()
    prev_dir = utils.generate_song_directory(prev_d_song)
    os.rename(prev_dir, new_dir)

    # 更新数据库中的歌曲信息
    new_song = {}
    inst_dir = app.config['TRIMMED_WAV_FILENAME'].split('.')[0];
    for field, info in prev_d_song.items():
        if field == 'id':
            continue
        if field == 'song_name': 
            new_song['song_name'] = song_name
        elif field == 'singer':
            new_song['singer'] = singer
        else:
            _, filename = os.path.split(info)
            if filename:
                if field == 'drum' or field == 'bass' or field == 'orchestra':
                    new_file_path = os.path.join(new_dir, inst_dir, filename)
                else:
                    new_file_path = os.path.join(new_dir, filename)
                new_song[field] = new_file_path

    song_to_save = Song.from_dict(new_song)
    song_to_save.id = song_id
    dao.update_song(song_to_save)


'''
上传一首新的歌曲。

在以下4个节点向客户端发送socket请求:
1. 客户端上传文件存储完毕
2. 和弦文件解释完毕
3. 自弹自唱相关文件转化完毕
4. 歌曲打分文件生成完毕

参数:
    req_song: 上传的歌曲信息，包括歌曲名称与歌手。
    req_files: 上传的歌曲文件，包括专辑图、原唱音频、伴奏音频、歌词文件、MV文件与和弦文件。
'''
def upload_song(req_song, req_files):

    real_app = app._get_current_object()
    new_song = {}

    new_song['song_name'] = req_song.get('song_name')
    new_song['singer'] = req_song.get('singer')

    # 如果歌曲已经存在，则删除
    song_in_db = dao.get_song_by_song_name_and_singer(new_song['song_name'], new_song['singer'])
    if song_in_db:
        d_song = song_in_db.as_dict()
        delete_dir = utils.generate_song_directory(d_song)
        shutil.rmtree(delete_dir)
        dao.delete_song_by_id(d_song['id'])

    # 新建歌曲存储目录
    song_info = new_song['song_name'] + '-' + new_song['singer']
    song_upload_dir = os.path.join(app.config['FILE_UPLOAD_DIR'], song_info)
    if not os.path.exists(song_upload_dir):
        os.makedirs(song_upload_dir)

    # 将客户端上传的文件与服务器生成的文件保存至服务器
    for file_type, upload_file in req_files.items():

        save_path = os.path.join(song_upload_dir, upload_file.filename)
        upload_file.save(save_path)
        new_song[file_type] = save_path

        if file_type == 'original':
            new_song['rate'] = os.path.join(song_upload_dir, app.config['RATE_FILENAME'])
            rate_thread = Thread(target=utils.rate_by_original, args=(real_app, save_path, song_info,))
            rate_thread.start()

        if file_type == 'chord':
            new_song['chord'] = os.path.join(song_upload_dir, app.config['CHORD_TRANS_FILENAME'])
            chord_thread = Thread(target=utils.trans_chord, args=(real_app, save_path, song_info,))
            chord_thread.start()

    socketio.emit('upload', song_info, namespace='/karaoke')

    chord_thread.join()

    instrument_sing_thread = Thread(target=utils.generate_instrument_sing_files, 
                                    args=(real_app, new_song['chord'], new_song['lyric_accompany'], 
                                          new_song['original'], song_info,))
    instrument_sing_thread.start()
    new_song['lyric_instrument'] = os.path.join(song_upload_dir, app.config['LYRIC_INSTRUMENT_FILENAME'])
    output_dir = app.config['TRIMMED_WAV_FILENAME'].rsplit('.', 1)[0]
    new_song['drum'] = os.path.join(song_upload_dir, output_dir, app.config['DRUM_FILENAME'])
    new_song['bass'] = os.path.join(song_upload_dir, output_dir, app.config['BASS_FILENAME'])
    new_song['orchestra'] = os.path.join(song_upload_dir, output_dir, app.config['ORCHESTRA_FILENAME'])

    rate_thread.join()
    instrument_sing_thread.join()

    song_to_save = Song.from_dict(new_song)
    dao.save_song(song_to_save)
    
    socketio.emit('finish', song_info, namespace='/karaoke')


'''
更新歌曲的单个文件。

参数:
    song_id: 歌曲的id。
    upload_files: 上传的文件。

抛出:
    UploadQuantityException: 上传的文件数量不为1。
'''
def upload_one_file(song_id, upload_files):

    real_app = app._get_current_object()
    d_song = dao.get_song_by_id(song_id).as_dict()
    song_dir = utils.generate_song_directory(d_song)

    if len(upload_files.keys()) != 1:
        raise UploadQuantityException
    
    upload_stuff = list(upload_files.items())[0]
    file_type, upload_file = upload_stuff[0], upload_stuff[1]

    # 删除原来的文件
    prev_file_path = d_song[file_type]
    if (os.path.exists(prev_file_path)):
        os.remove(prev_file_path)

    # 将上传文件保存至服务器
    new_file_path = os.path.join(song_dir, upload_file.filename)
    upload_file.save(new_file_path)

    d_song[file_type] = new_file_path

    if file_type == 'chord':
        utils.trans_chord(real_app, new_file_path, '')
        d_song['chord'] = os.path.join(song_dir, app.config['CHORD_TRANS_FILENAME'])

    # 更新数据库中的文件路径信息
    song_to_save = Song.from_dict(d_song)
    song_to_save.id = d_song['id']
    dao.update_song(song_to_save)


'''
进行服务器端歌曲文件的同步。在文件同步成功后向客户端发送同步成功的socket请求。

参数:
    song_id: 要进行文件同步的歌曲的id。
    song_info: 格式为“<歌曲名称>-<歌手>”的歌曲信息，用于向客户端发送socket。

抛出:
    SongNotExistException: 歌曲不存在。
    UnmatchedSongInfoException: 数据库中的歌曲信息与参数中的歌曲信息不相符。
'''
def sync_files(song_id, song_info):

    song_in_db = dao.get_song_by_id(song_id)
    if song_in_db is None:
        raise SongNotExistException

    d_song = song_in_db.as_dict()
    song_info_from_db = d_song['song_name'] + '-' + d_song['singer']
    if song_info != song_info_from_db:
        raise UnmatchedSongInfoException
        
    real_app = app._get_current_object()

    rate_thread = Thread(target=utils.rate_by_original, args=(real_app, d_song['original'], song_info,))
    rate_thread.start()

    instrument_sing_thread = Thread(target=utils.generate_instrument_sing_files, 
                                    args=(real_app, d_song['chord'], d_song['lyric_accompany'], 
                                          d_song['original'], song_info,))
    instrument_sing_thread.start()

    rate_thread.join()
    instrument_sing_thread.join()

    socketio.emit('sync-success', song_info, namespace='/karaoke')


''' 
获取所有歌曲的信息。

返回:
    一个包含数据库内所有歌曲的id，名称和歌手的字典的列表。
'''
def get_song_info():
    song_infos_in_db = dao.get_song_info()
    song_infos = []
    for song_info_in_db in song_infos_in_db:
        song_info = {
            'id': song_info_in_db[0],
            'song_name': song_info_in_db[1],
            'singer': song_info_in_db[2],
        }
        song_infos.append(song_info)
    return song_infos


'''
获取指定歌曲文件的存储路径。

参数:
    song_id: 歌曲的id。
    file_type: 要发送的文件的类型（与数据库中的字段名相同）。

返回:
    相应歌曲文件的在服务器上的存储路径。

抛出:
    SongNotExistException: 歌曲不存在。
'''
def get_file_path(song_id, file_type):
    song_in_db = dao.get_song_by_id(song_id)
    if song_in_db is None:
        raise SongNotExistException
    d_song = song_in_db.as_dict()
    return d_song[file_type]
    