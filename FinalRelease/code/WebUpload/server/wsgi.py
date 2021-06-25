from flask import jsonify, request, send_file
from src import app, socketio
from src import services
from src.exceptions import *


'''
连接客户端socket。
'''
@socketio.on('connect', namespace='/karaoke')
def connect():
    print('connected!')

 
'''
进行服务器端歌曲文件的同步（用于上传某首歌曲单个文件之后）。若文件同步失败，则向
客户端发送同步失败的socket请求。

参数:
    song_id: 要进行文件同步的歌曲的id。
    song_info: 格式为“<歌曲名称>-<歌手>”的歌曲信息，用于向客户端发送socket。
'''
@socketio.on('sync', namespace='/karaoke')
def sync(song_id, song_info):
    try:
        services.sync_files(song_id, song_info)
    except:
        socketio.emit('sync-fail', song_info)


'''
管理员登录。

返回:
    若登录成功，则返回用户信息；否则，返回状态码为400的响应。
'''
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


'''
获取数据库中的所有歌曲。

返回:
    若成功，则返回一个包含数据库中所有歌曲的列表；否则，返回状态码
为400的响应。
'''
@app.route("/getSongs", methods=['GET'])
def get_songs():
    try:
        songs = jsonify(services.get_songs())
    except:
        return 'Fail to get songs.', 400

    return songs


'''
删除一首歌曲。

返回:
    若删除成功，则返回状态码为200的响应；否则，返回状态码为400的响应。
'''
@app.route("/deleteSong", methods=['DELETE'])
def delete_song():
    try:
        data = request.get_json()
        song_id = data.get('id')
        services.delete_song(song_id)
    except:
        return 'Fail to delete the song.', 400

    return 'success'


'''
更新歌曲信息（歌曲名称与歌手）。

返回:
    若更新成功，则返回状态码为200的响应；否则，返回状态码为400的响应。
'''
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


'''
上传一首新的歌曲。

返回:
    若上传成功，则返回状态码为200的响应；否则，返回状态码为400的响应。
'''
@app.route("/uploadSong", methods=['POST'])
def upload_song():
    try:
        services.upload_song(request.form, request.files)
    except ChordParseException:
        return 'Cannot parse the chord', 400
    except:
        return 'Fail to upload the song', 400

    return 'success'


'''
更新歌曲的单个文件。

参数:
    song_id: 歌曲的id。

返回:
    若更新成功，则返回状态码为200的响应；否则，返回状态码为400的响应。
'''
@app.route("/uploadOneFile/<song_id>", methods=['POST'])
def upload_one_file(song_id):
    try:
        services.upload_one_file(song_id, request.files)
    except:
        return 'Fail to upload the file.', 400

    return 'success'


''' 
获取所有歌曲的信息。

返回:
    若成功，则返回一个包含数据库内所有歌曲的id，名称和歌手的列表；否则，
返回状态码为400的响应。
'''
@app.route("/getSongInfo", methods=['GET'])
def get_song_info():
    try:
        song_infos = services.get_song_info()
    except:
        return 'Fail to get song information.', 400

    return jsonify(song_infos)


'''
向客户端发送指定文件。

参数:
    file_type: 要发送的文件的类型。

返回:
    若成功，则发送相应文件；否则，返回状态码为400的响应。
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
