import eventlet
from flask import Flask
from flask_socketio import SocketIO
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from config import Config

eventlet.monkey_patch()

app = Flask(__name__, instance_relative_config=False)
app.config.from_object(Config)
CORS(app, resources={r'/*': {'origins': '*'}})

db = SQLAlchemy()
db.init_app(app)

with app.app_context():
    from src import models
    db.create_all()  # 新建数据库中的表格（若已存在，则不会重新新建）
    socketio = SocketIO(app, cors_allowed_origins='*', always_connect=True)
