from flask_login import UserMixin 
from werkzeug.security import check_password_hash

'''
用于验证用户登录密码是否正确。
'''
class UserVerification(UserMixin):

    def __init__(self, user):
        self.id = user['id']
        self.username = user['username']
        
        self.password_hash = user['password']

    def verify_password(self, password):
        if self.password_hash is None:
            return False
        return check_password_hash(self.password_hash, password)

    def get_id(self):
        return self.id