from flask import jsonify, request, send_file
from src import app, socketio
from src import services
from src.exceptions import *


@socketio.on('connect', namespace='/karaoke')
def connect():
    print('connected!')

@socketio.on('sync', namespace='/karaoke')
def sync(song_id, song_info):
    try:
        services.sync_files(song_id, song_info)
    except:
        socketio.emit('sync-fail', song_info)


@app.route('/login', methods=['POST'])
def login():
    try:
        post_data = request.get_json()
        username = post_data.get('username')
        password = post_data.get('password')
        user = services.verify_user(username, password)
    except UserNotExistException:
        return 'Admin account does not exist.', 400
    except WrongPasswordException:
        return 'Wrong password.', 400
    except:
        return 'Fail to login.', 400

    return user


@app.route("/getSongs", methods=['GET'])
def get_songs():
    try:
        songs = jsonify(services.get_songs())
    except:
        return 'Fail to get songs.', 400

    return songs


@app.route("/deleteSong", methods=['DELETE'])
def delete_song():
    try:
        data = request.get_json()
        song_id = data.get('id')
        services.delete_song(song_id)
    except:
        return 'Fail to delete the song.', 400

    return 'success'


@app.route("/updateSongInfo", methods=['POST'])
def update_song_info():
    try:
        song = request.get_json()
        services.update_song_info(song)
    except DuplicateSongException:
        return 'Song already exists.', 400
    except:
        return 'Fail to update the song.', 400

    return 'success'


@app.route("/uploadSong", methods=['POST'])
def upload_song():
    try:
        services.upload_song(request.form, request.files)
    except ChordParseException:
        return 'Cannot parse the chord', 400
    except:
        return 'Fail to upload the song', 400

    return 'success'


@app.route("/uploadOneFile/<song_id>", methods=['POST'])
def upload_one_file(song_id):
    try:
        services.upload_one_file(song_id, request.files)
    except:
        return 'Fail to upload the file.', 400

    return 'success'



''' 
Returns:
    A JSON object including the id, the name and the singer
    of all the songs in database.
'''
@app.route("/getSongInfo", methods=['GET'])
def get_song_info():
    try:
        song_infos = services.get_song_info()
    except:
        return 'Fail to get song information.', 400

    return jsonify(song_infos)


'''
Args:
    file_type: Field name in the database which denotes the type of 
        the file required.
Returns:
    The corresponding file of the requested song.
'''
@app.route("/getFile/<file_type>", methods=['GET'])
def get_file(file_type):
    try:
        song_id = request.args['id']
        filename = services.get_file_path(song_id, file_type)
    except:
        return 'Cannot get file path.', 400

    return send_file(filename)



if __name__ == "__main__":
    socketio.run(app, host='0.0.0.0', port=5000, debug=False)
