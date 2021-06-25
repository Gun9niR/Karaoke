import unittest
import HtmlTestRunner
from test_get_songs import TestGetSongs
from test_login import TestLogin
from test_delete_song import TestDeleteSong
from test_get_song_info import TestGetSongInfo
from test_get_file import TestGetFile
from test_update_song_info import TestUpdateSongInfo

def suite():
    test_suite = unittest.TestSuite()
    test_suite.addTest(unittest.makeSuite(TestGetSongs))
    test_suite.addTest(unittest.makeSuite(TestLogin))
    test_suite.addTest(unittest.makeSuite(TestDeleteSong))
    test_suite.addTest(unittest.makeSuite(TestGetSongInfo))
    test_suite.addTest(unittest.makeSuite(TestGetFile))
    test_suite.addTest(unittest.makeSuite(TestUpdateSongInfo))
    return test_suite

if __name__ == '__main__':
    unittest.main(testRunner=HtmlTestRunner.HTMLTestRunner(output='report'))
