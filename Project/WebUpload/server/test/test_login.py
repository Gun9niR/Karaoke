import unittest
import sys
sys.path.append('../')
from wsgi import app

class TestLogin(unittest.TestCase):
    
    def setUp(self):
        self.client = app.test_client()

    def test_login_success(self):
        post_data = {
            'username': 'deepsand',
            'password': '987654',
        }
        response = self.client.post('/login', json=post_data)
        self.assertEqual(response.status_code, 200)

    def test_login_fail(self):
        post_data = {
            'username': 'abc',
            'password': 'uuukkk',
        }
        response = self.client.post('/login', data=post_data)
        self.assertEqual(response.status_code, 400)

