from flask import current_app as app
from flask_socketio import send, emit
import os
import shutil
from . import socketio
from . import dao, utils
from .models import Song
from .exceptions import *
from .verify import UserVerification
from threading import Thread


def verify_user(username, password):
    user_in_db = dao.get_user_by_username(username)
    if user_in_db is None:
        raise UserNotExistException

    d_user = user_in_db.as_dict()
    user_v = UserVerification(d_user)
    if not user_v.verify_password(password):
        raise WrongPasswordException

    return d_user
    

def get_songs():
    songs_in_db = dao.get_songs()
    return [song_in_db.as_dict() for song_in_db in songs_in_db]


def delete_song(song_id):

    # Delete the files
    d_song = dao.get_song_by_id(song_id).as_dict()
    delete_dir = utils.generate_song_directory(d_song)
    shutil.rmtree(delete_dir)

    # Delete in database
    dao.delete_song_by_id(song_id)


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
    
    # Get the directory of the new song
    new_dir = utils.generate_song_directory(req_song)
    if os.path.exists(new_dir):
        return

    # Rename the directory
    prev_song_in_db = dao.get_song_by_id(song_id)
    prev_d_song = prev_song_in_db.as_dict()
    prev_dir = utils.generate_song_directory(prev_d_song)
    os.rename(prev_dir, new_dir)

    # Update song information and file paths in database
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


def upload_song(req_song, req_files):

    real_app = app._get_current_object()
    new_song = {}

    new_song['song_name'] = req_song.get('song_name')
    new_song['singer'] = req_song.get('singer')

    # Remove the song if already exists
    song_in_db = dao.get_song_by_song_name_and_singer(new_song['song_name'], new_song['singer'])
    if song_in_db:
        d_song = song_in_db.as_dict()
        delete_dir = utils.generate_song_directory(d_song)
        shutil.rmtree(delete_dir)
        dao.delete_song_by_id(d_song['id'])

    # Create song directory
    song_info = new_song['song_name'] + '-' + new_song['singer']
    song_upload_dir = os.path.join(app.config['FILE_UPLOAD_DIR'], song_info)
    if not os.path.exists(song_upload_dir):
        os.makedirs(song_upload_dir)

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


def upload_one_file(song_id, upload_files):

    real_app = app._get_current_object()
    d_song = dao.get_song_by_id(song_id).as_dict()
    song_dir = utils.generate_song_directory(d_song)

    if len(upload_files.keys()) != 1:
        raise UploadQuantityException
    
    upload_stuff = list(upload_files.items())[0]
    file_type, upload_file = upload_stuff[0], upload_stuff[1]

    # Remove the previous file
    prev_file_path = d_song[file_type]
    if (os.path.exists(prev_file_path)):
        os.remove(prev_file_path)

    # Save the file to local
    new_file_path = os.path.join(song_dir, upload_file.filename)
    upload_file.save(new_file_path)

    d_song[file_type] = new_file_path

    if file_type == 'chord':
        utils.trans_chord(real_app, new_file_path, '')
        d_song['chord'] = os.path.join(song_dir, app.config['CHORD_TRANS_FILENAME'])

    # Update the file path in database
    song_to_save = Song.from_dict(d_song)
    song_to_save.id = d_song['id']
    dao.update_song(song_to_save)


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


def get_file_path(song_id, file_type):
    song_in_db = dao.get_song_by_id(song_id)
    if song_in_db is None:
        raise SongNotExistException
    d_song = song_in_db.as_dict()
    return d_song[file_type]
    

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
