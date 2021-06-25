import os
import sys
import shutil
import subprocess
from flask import current_app as app
from threading import Thread
from . import socketio
from src.exceptions import InvalidSongSegmentException, ChordParseException


'''
从完整的歌曲对象分离出歌曲存储目录。

参数:
    d_song: 数据库中的歌曲所映射的字典。
返回:
    该歌曲的存储目录。
'''
def generate_song_directory(d_song):
    song_name = d_song['song_name']
    singer = d_song['singer']
    folder_name = song_name + '-' + singer
    return os.path.join(app.config['FILE_UPLOAD_DIR'], folder_name)


'''
由歌曲原唱生成歌曲打分文件。

参数:
    real_app: Flask的app对象，需要手动在调用函数中传入。
    original_file_path: 歌曲原唱文件路径。
    song_info: 格式为“<歌曲名称>-<歌手>”的歌曲信息，用于向客户端发送socket。
'''
def rate_by_original(real_app, original_file_path, song_info):

    with real_app.app_context():
        
        vocal_file_path = generate_vocal_file(original_file_path)
        single_track_file_path = generate_single_track_file(vocal_file_path)
        generate_rate_file(single_track_file_path)

        if song_info:
            socketio.emit('rate', song_info, namespace='/karaoke')


'''
解释和弦文件。

参数:
    real_app: Flask的app对象，需要手动在调用函数中传入。
    org_chord_path: 原始和弦文件路径。
    song_info: 格式为“<歌曲名称>-<歌手>”的歌曲信息，用于向客户端发送socket。
'''
def trans_chord(real_app, org_chord_path, song_info):

    with real_app.app_context():

        directory, _ = os.path.split(org_chord_path)
        chord_trans_path = os.path.join(directory, app.config['CHORD_TRANS_FILENAME'])

        shell_args = [app.config['CHORD_TRANSLATOR_PATH'], org_chord_path, chord_trans_path]
        process = subprocess.Popen(shell_args, cwd=app.config['CHORD_TRANS_WORKING_DIR'], 
                                   shell=app.config['REQUIRE_SHELL'])
        process.wait()

        # 删除原始和弦文件
        try:
            os.remove(org_chord_path)
        except OSError:
            pass

        if song_info:
            socketio.emit('chord', song_info, namespace='/karaoke')


'''
生成片段歌词文件、音轨文件等自弹自唱相关文件。

参数:
    real_app: Flask的app对象，需要手动在调用函数中传入。
    chord_path: 解释完毕后的和弦文件路径。
    lyric_path: 完整歌词文件路径。
    original_path: 歌曲原唱文件路径。
    song_info: 格式为“<歌曲名称>-<歌手>”的歌曲信息，用于向客户端发送socket。
'''
def generate_instrument_sing_files(real_app, chord_path, lyric_path, original_path, song_info):

    start_time, end_time = read_chord(chord_path)

    lrc_thread = Thread(target=trim_lrc, args=(real_app, lyric_path, start_time, end_time,))
    track_thread = Thread(target=separate_audio_track, args=(real_app, original_path, start_time, end_time,))

    lrc_thread.start()
    track_thread.start()

    lrc_thread.join()
    track_thread.join()

    if song_info:
        socketio.emit('instrument', song_info, namespace='/karaoke')


'''
对歌曲特定片段进行音轨分离。

参数:
    real_app: Flask的app对象，需要手动在调用函数中传入。
    original_path: 歌曲原唱文件路径。
    start_time: 歌曲特定片段的开始时间。
    end_time: 歌曲特定片段的结束时间。
'''
def separate_audio_track(real_app, original_path, start_time, end_time):

    with real_app.app_context():

        start_time = start_time - app.config['BUTTON_ANI_SEC']
        if start_time  < 0:
            raise InvalidSongSegmentException

        trimmed_wav_path = trim_wav(original_path, start_time, end_time)
        generate_inst_wav(trimmed_wav_path)

        # 删除切分出的歌曲特定片段文件
        try:
            os.remove(trimmed_wav_path)
        except OSError:
            pass


'''
从歌曲原唱中分离出人声。

参数:
    original_file_path: 歌曲原唱文件路径。

返回:
    分离出的无伴奏人声文件路径。
'''
def generate_vocal_file(original_file_path):

    directory, original_filename = os.path.split(original_file_path)
    filename_without_suffix = original_filename.rsplit('.', 1)[0]

    shell_args = ['spleeter', 'separate', '-p', 'spleeter:2stems', '-o', 
                  directory, original_file_path]
    if sys.platform == 'win32':
        shell_args.insert(0, 'python')
        shell_args.insert(1, '-m')

    process = subprocess.Popen(shell_args, cwd=app.config['WORKING_DIR'],
                               shell=app.config['REQUIRE_SHELL'])
    process.wait()

    return os.path.join(directory, filename_without_suffix, 'vocals.wav')


'''
将无伴奏人声文件转换为单音轨。

参数:
    vocal_file_path: 人声文件路径。

返回:
    单音轨无伴奏人声文件路径。
'''
def generate_single_track_file(vocal_file_path):

    directory, _ = os.path.split(vocal_file_path)
    single_track_file_path = os.path.join(directory, 'vocal_single.wav')

    shell_args = ['ffmpeg', '-y', '-i', vocal_file_path, 
                    '-ar', '44100', '-ac', '1', single_track_file_path]
    process = subprocess.Popen(shell_args, cwd=app.config['WORKING_DIR'],
                               shell=app.config['REQUIRE_SHELL'])
    process.wait()

    return single_track_file_path


'''
由单音轨无伴奏人声文件生成歌曲打分文件。

参数:
    single_track_file_path: 单音轨无伴奏人声文件路径。
'''
def generate_rate_file(single_track_file_path):

    rm_dir, _ = os.path.split(single_track_file_path)

    if sys.platform == 'win32':     # Windows
        directory = rm_dir.rsplit('\\', 1)[0]
    else:                           # Linux 或 MacOS
        directory = rm_dir.rsplit('/', 1)[0]

    rate_file_path = os.path.join(directory, app.config['RATE_FILENAME'])
    shell_args = [app.config['RATING_PATH'], single_track_file_path, 
                  '-t', '-s', '50', '-o', rate_file_path]
    process = subprocess.Popen(shell_args, cwd=app.config['WORKING_DIR'],
                               shell=app.config['REQUIRE_SHELL'])
    process.wait()

    try:
        shutil.rmtree(rm_dir)
    except OSError:
        pass


'''
从完整的歌词文件中提取出歌曲特定片段的歌词。

参数:
    real_app: Flask的app对象，需要手动在调用函数中传入。
    org_lrc_path: 完整歌词文件路径。
    start_time: 歌曲特定片段的开始时间。
    end_time: 歌曲特定片段的结束时间。
'''
def trim_lrc(real_app, org_lrc_path, start_time, end_time):

    with real_app.app_context():

        directory, _ = os.path.split(org_lrc_path)
        new_lrc_path = os.path.join(directory, app.config['LYRIC_INSTRUMENT_FILENAME'])
        
        shell_args = [app.config['TRIMMER_PATH'], str(start_time), str(end_time), org_lrc_path, new_lrc_path]
        process = subprocess.Popen(shell_args, cwd=app.config['WORKING_DIR'],
                                   shell=app.config['REQUIRE_SHELL'])
        process.wait()


'''
从解释完毕的和弦文件中获取歌曲片段的开始时间与结束时间。

参数:
    chord_trans_path: 解释完毕的和弦文件路径。

返回:
    歌曲特定片段的开始时间与结束时间。
'''
def read_chord(chord_trans_path):

    with open(chord_trans_path, 'r') as chord:
        line = chord.readline()
        args = line.split()

        if len(args) != 5:
            raise ChordParseException
            
        start_time = float(args[3]) / 1000
        end_time = float(args[4]) / 1000
        return start_time, end_time


'''
切分特定片段的歌曲原唱。

参数:
    org_wav_path: 歌曲原唱文件路径。
    start_time: 歌曲特定片段的开始时间。
    end_time: 歌曲特定片段的结束时间。

返回:
    切分出的歌曲原唱片段文件路径。
'''
def trim_wav(org_wav_path, start_time, end_time):

    directory, _ = os.path.split(org_wav_path)
    trimmed_wav_path = os.path.join(directory, app.config['TRIMMED_WAV_FILENAME'])

    duration = end_time - start_time
    shell_args = ['ffmpeg', '-ss', str(start_time), '-t', str(duration), 
                  '-i', org_wav_path, trimmed_wav_path]

    process = subprocess.Popen(shell_args, shell=app.config['REQUIRE_SHELL'])
    process.wait()

    return trimmed_wav_path


'''
对歌曲原唱片段文件进行音轨分离，产生自弹自唱所需文件。

参数:
    trimmed_wav_path: 歌曲原唱片段文件路径。
'''
def generate_inst_wav(trimmed_wav_path):

    directory, _ = os.path.split(trimmed_wav_path)

    shell_args = ['spleeter', 'separate', '-p', 'spleeter:5stems', 
                  '-o', directory, trimmed_wav_path]
    if sys.platform == 'win32':
        shell_args.insert(0, 'python')
        shell_args.insert(1, '-m')

    process = subprocess.Popen(shell_args, cwd=app.config['WORKING_DIR'],
                               shell=app.config['REQUIRE_SHELL'])
    process.wait()
