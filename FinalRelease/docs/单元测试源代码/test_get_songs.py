import unittest
import sys
sys.path.append('../')
from wsgi import app

class TestGetSongs(unittest.TestCase):
    
    def setUp(self):
        self.client = app.test_client()

    def test_get_songs(self):
        response = self.client.get('/getSongs')
        self.assertEqual(response.status_code, 200)

