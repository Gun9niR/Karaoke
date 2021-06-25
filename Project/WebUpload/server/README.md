# Server

## Set up the server

1. Run `./server.sh` in the command line. (On windows, you may need to type the commands by yourself in the command line.)

2. Install [spleeter](https://github.com/deezer/spleeter) on your machine.

3. Manually put `pretrained_models` and `rating` folders into `utils`.

4. Alter `SQLALCHEMY_DATABASE_URI` and `FILE_UPLOAD_DIR` in `config.py` according to your own configurations. 
   
5. On Windows, it is strongly recommended that you use `os.path.join` in all path-related entries in `config.py`.

## Run the server

Run `python wsgi.py` in the command line.