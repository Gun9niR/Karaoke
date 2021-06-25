import unittest
import sys
sys.path.append('../')
from wsgi import app

class TestUpdateSongInfo(unittest.TestCase):
    
    def setUp(self):
        self.client = app.test_client()

    def test_update_song_info(self):
        song_info = { 
            'id': 22,
            'song_name': '宁夏',
            'singer': '梁静茹',
        }
        response = self.client.post('/updateSongInfo', json=song_info)
        self.assertEqual(response.status_code, 200)

