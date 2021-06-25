import unittest
import sys
sys.path.append('../')
from wsgi import app

class TestGetSongInfo(unittest.TestCase):
    
    def setUp(self):
        self.client = app.test_client()

    def test_get_song_info(self):
        response = self.client.get('/getSongInfo')
        self.assertEqual(response.status_code, 200)

