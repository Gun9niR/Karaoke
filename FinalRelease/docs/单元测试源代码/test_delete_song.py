import unittest
import sys
sys.path.append('../')
from wsgi import app

class TestDeleteSong(unittest.TestCase):
    
    def setUp(self):
        self.client = app.test_client()

    def test_delete(self):
        delete_data = { 'id': 10 }
        self.client.post('/deleteSong', json=delete_data)
        file_type = 'album'
        response = self.client.get(f'/getFile/{file_type}', query_string=delete_data)
        self.assertEqual(response.status_code, 400)

