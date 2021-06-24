import unittest
import sys
sys.path.append('../')
from wsgi import app

class TestGetFile(unittest.TestCase):
    
    def setUp(self):
        self.client = app.test_client()

    def test_get_file(self):
        get_file_data = { 'id': 22 }
        file_type = 'accompany'
        response = self.client.get(f'/getFile/{file_type}', query_string=get_file_data)
        self.assertEqual(response.status_code, 200)

