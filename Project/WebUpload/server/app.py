from flask import Flask, jsonify, request, send_file
from flask_cors import CORS, cross_origin
from config import Config
import pymysql
import os
import shutil
import subprocess

app = Flask(__name__)
app.config.from_object(Config)
CORS(app, resources={r'/*': {'origins': '*'}})


@app.route('/verify', methods=['GET', 'POST'])
def verify_admin():

    post_data = request.get_json()
    login_name = post_data.get('name')
    login_password = post_data.get('password')

    for admin in app.config['ADMINS']:
        admin_name = admin['name']
        admin_password = admin['password']
        if (login_name == admin_name and login_password == admin_password):
            return 'success'

    return 'Login failed!', 400


@app.route("/getSongs", methods=['GET'])
@cross_origin()
def get_songs():
    songs = get_songs_in_db()
    return jsonify(songs)

@app.route("/deleteSong", methods=['DELETE'])
@cross_origin()
def delete_song():
    data = request.get_json()
    song_id = data.get('id')

    delete_dir = get_dir_by_id(song_id)
    shutil.rmtree(delete_dir)

    delete_song_in_db(song_id)

    return 'success'


@app.route("/updateSongInfo", methods=['POST'])
@cross_origin()
def update_song_info():

    song = request.get_json()

    song_id = song['id']
    song_in_db = get_song_by_song_name_and_singer(song['song_name'], song['singer'])
    if song_in_db and song_in_db['id'] != song_id:
        return 'Song already exists!', 400

    new_dir = generate_save_directory(song)
    if os.path.exists(new_dir):
        return 'success'

    # Rename the directory
    previous_dir = get_dir_by_id(song_id)
    os.rename(previous_dir, new_dir)

    # Update song information and file paths in database
    update_song_info_in_db(song)
    update_dir_in_db(song_id, new_dir)

    return 'success'


@app.route("/uploadOneFile/<song_id>", methods=['POST'])
@cross_origin()
def upload_one_file(song_id):
    
    upload_files = request.files
    song_dir = get_dir_by_id(song_id)

    for file_type in upload_files.keys():

        # Remove the previous file
        previous_file_path = get_path_by_id_and_file_type(song_id, file_type)
        if os.path.exists(previous_file_path):
            os.remove(previous_file_path)

        # Save the file to local
        upload_file = upload_files[file_type]
        new_file_path = os.path.join(song_dir, upload_file.filename)
        upload_file.save(new_file_path)

        if file_type == 'rate':
            new_file_path = generate_rate_file(new_file_path)
            
        # Save the file path to database
        update_file_path_in_db(song_id, file_type, new_file_path)

    return 'success'


@app.route("/uploadSong", methods=['POST'])
@cross_origin()
def upload_song():

    save = {}

    save['song_name'] = request.form.get('song_name')
    save['singer'] = request.form.get('singer')
    
    # Remove the song if already exists
    song = get_song_by_song_name_and_singer(save['song_name'], save['singer'])
    if song:
        song_id = song['id']
        delete_dir = get_dir_by_id(song_id)
        shutil.rmtree(delete_dir)
        delete_song_in_db(song_id)

    # Create song directory
    folder_name = save['song_name'] + '-' + save['singer']
    song_upload_path = os.path.join(app.config['FILE_UPLOAD_PATH'], folder_name)
    if not os.path.exists(song_upload_path):
        os.makedirs(song_upload_path)

    # Save files to local
    upload_files = request.files
    for file_field in upload_files.keys():

        upload_file = upload_files[file_field]
        save_path = os.path.join(song_upload_path, upload_file.filename)
        upload_file.save(save_path)

        if file_field == 'rate':
            save_path = generate_rate_file(save_path)

        save[file_field] = save_path

    # Save file paths to database
    save_song_in_db(save)
        
    return 'success'

'''
Args:
    file_type: Field name in the database which denotes the type of 
        the file required.
Returns:
    The corresponding file of the requested song.
'''
@app.route("/getFile/<file_type>", methods=['GET'])
@cross_origin()
def get_file(file_type):
    song_id = request.args['id']
    filename = get_path_by_id_and_file_type(song_id, file_type)
    return send_file(filename)

''' 
Returns:
    A JSON object including the id, the name and the singer
    of all the songs in database.
'''
@app.route("/getSongInfo", methods=['GET'])
@cross_origin()
def get_song_info():
    return jsonify(get_song_info_in_db())



''' Functions below are for SQL queries.'''

def get_songs_in_db():
    with pymysql.connect(host=app.config['MYSQL_HOST'], user=app.config['MYSQL_USER'], password=app.config['MYSQL_PASSWORD'],
                             database=app.config['MYSQL_DATABASE'], charset=app.config['MYSQL_CHARSET']) as connection:
        with connection.cursor(pymysql.cursors.DictCursor) as cursor:
            sql = 'SELECT * FROM songs'
            cursor.execute(sql)
            connection.commit()
            return cursor.fetchall()

def get_song_info_in_db():
    with pymysql.connect(host=app.config['MYSQL_HOST'], user=app.config['MYSQL_USER'], password=app.config['MYSQL_PASSWORD'],
                             database=app.config['MYSQL_DATABASE'], charset=app.config['MYSQL_CHARSET']) as connection:
        with connection.cursor(pymysql.cursors.DictCursor) as cursor:
            sql = 'SELECT id, song_name, singer FROM songs'
            cursor.execute(sql)
            connection.commit()
            song_info = cursor.fetchall()
            # print(song_info)
            return song_info

def get_song_by_id(song_id):
    with pymysql.connect(host=app.config['MYSQL_HOST'], user=app.config['MYSQL_USER'], password=app.config['MYSQL_PASSWORD'],
                             database=app.config['MYSQL_DATABASE'], charset=app.config['MYSQL_CHARSET']) as connection:
        with connection.cursor(pymysql.cursors.DictCursor) as cursor:
            sql = 'SELECT * FROM songs WHERE id = %s'
            cursor.execute(sql, song_id)
            connection.commit()
            return cursor.fetchone()

def get_song_name_by_id(song_id):
    with pymysql.connect(host=app.config['MYSQL_HOST'], user=app.config['MYSQL_USER'], password=app.config['MYSQL_PASSWORD'],
                             database=app.config['MYSQL_DATABASE'], charset=app.config['MYSQL_CHARSET']) as connection:
        with connection.cursor() as cursor:
            sql = 'SELECT song_name FROM songs WHERE id = %s'
            cursor.execute(sql, song_id)
            connection.commit()
            return cursor.fetchone()

def get_song_by_song_name_and_singer(song_name, singer):
    with pymysql.connect(host=app.config['MYSQL_HOST'], user=app.config['MYSQL_USER'], password=app.config['MYSQL_PASSWORD'],
                             database=app.config['MYSQL_DATABASE'], charset=app.config['MYSQL_CHARSET']) as connection:
        with connection.cursor(pymysql.cursors.DictCursor) as cursor:
            sql = 'SELECT * FROM songs WHERE song_name = %s AND singer = %s'
            cursor.execute(sql, (song_name, singer))
            connection.commit()
            return cursor.fetchone()

def get_path_by_id_and_file_type(song_id, file_type):
    with pymysql.connect(host=app.config['MYSQL_HOST'], user=app.config['MYSQL_USER'], password=app.config['MYSQL_PASSWORD'],
                             database=app.config['MYSQL_DATABASE'], charset=app.config['MYSQL_CHARSET']) as connection:
        with connection.cursor() as cursor:
            sql = 'SELECT {file_type} FROM songs WHERE id = %s'.format(file_type=file_type)
            cursor.execute(sql, song_id)
            connection.commit()
            return cursor.fetchone()[0]

def get_dir_by_id(song_id):
    song = get_song_by_id(song_id)
    return generate_save_directory(song)

def delete_song_in_db(song_id):
    with pymysql.connect(host=app.config['MYSQL_HOST'], user=app.config['MYSQL_USER'], password=app.config['MYSQL_PASSWORD'],
                             database=app.config['MYSQL_DATABASE'], charset=app.config['MYSQL_CHARSET']) as connection:
        with connection.cursor() as cursor:
            sql = 'DELETE FROM songs WHERE id = %s'
            cursor.execute(sql, song_id)
            connection.commit()

def update_song_info_in_db(newSong):
    with pymysql.connect(host=app.config['MYSQL_HOST'], user=app.config['MYSQL_USER'], password=app.config['MYSQL_PASSWORD'],
                             database=app.config['MYSQL_DATABASE'], charset=app.config['MYSQL_CHARSET']) as connection:
        with connection.cursor() as cursor:
            sql = 'UPDATE songs SET song_name = %s, singer = %s WHERE id = %s'
            cursor.execute(sql, (newSong['song_name'], newSong['singer'], newSong['id']))
            connection.commit()

def update_dir_in_db(song_id, new_dir):
    with pymysql.connect(host=app.config['MYSQL_HOST'], user=app.config['MYSQL_USER'], password=app.config['MYSQL_PASSWORD'],
                             database=app.config['MYSQL_DATABASE'], charset=app.config['MYSQL_CHARSET']) as connection:
        with connection.cursor(pymysql.cursors.DictCursor) as cursor:
            sql =   '''
                        SELECT album, original, accompany_accompany,
                            lyric, mv, instrument, accompany_instrument
                            FROM songs WHERE id = %s
                    '''
            cursor.execute(sql, song_id)
            connection.commit()
            path_info = cursor.fetchone()

            for file_type, file_path in path_info.items():
                previous_dir, filename = os.path.split(file_path)
                new_file_path = os.path.join(new_dir, filename)
                update_file_path_in_db(song_id, file_type, new_file_path)


def update_file_path_in_db(song_id, file_type, file_path):
    with pymysql.connect(host=app.config['MYSQL_HOST'], user=app.config['MYSQL_USER'], password=app.config['MYSQL_PASSWORD'],
                             database=app.config['MYSQL_DATABASE'], charset=app.config['MYSQL_CHARSET']) as connection:
        with connection.cursor() as cursor:
            sql = 'UPDATE songs SET {file_type} = %s WHERE id = %s'.format(file_type=file_type)
            cursor.execute(sql, (file_path, song_id))
            connection.commit();

def save_song_in_db(song):

    with pymysql.connect(host=app.config['MYSQL_HOST'], user=app.config['MYSQL_USER'], password=app.config['MYSQL_PASSWORD'],
                             database=app.config['MYSQL_DATABASE'], charset=app.config['MYSQL_CHARSET']) as connection:
        with connection.cursor() as cursor:

            sql =   '''
                        INSERT INTO songs (
                            song_name, singer, album, original, 
                            accompany_accompany, lyric, mv, 
                            instrument, accompany_instrument, rate
                        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s);
                    '''
            cursor.execute(sql, (
                song['song_name'], song['singer'], song['album'], song['original'], 
                song['accompany_accompany'], song['lyric'], song['mv'], 
                song['instrument'], song['accompany_instrument'], song['rate']
            ))

            connection.commit()


'''Utils'''

def generate_save_directory(song):
    song_name = song['song_name']
    singer = song['singer']
    folder_name = song_name + '-' + singer
    return os.path.join(app.config['FILE_UPLOAD_PATH'], folder_name)


def generate_rate_file(vocal_file_path):

    directory, vocal_filename = os.path.split(vocal_file_path)
    filename_without_suffix = vocal_filename.rsplit('.', 1)[0]
    rate_filename = filename_without_suffix + ".txt"
    rate_file_path = os.path.join(directory, rate_filename)

    shell_args = [app.config['RATING_PATH'], vocal_file_path, '-t', '-s', '50', '-o', rate_file_path]
    subprocess.run(shell_args)

    os.remove(vocal_file_path)

    return rate_file_path



if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
