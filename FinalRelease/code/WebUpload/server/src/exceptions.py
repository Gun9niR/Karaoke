class UserNotExistException(Exception):
    pass

class WrongPasswordException(Exception):
    pass

class DuplicateSongException(Exception):
    pass

class UploadQuantityException(Exception):
    pass

class SongNotExistException(Exception):
    pass

class InvalidSongSegmentException(Exception):
    pass

class UnmatchedSongInfoException(Exception):
    pass

class ChordParseException(Exception):
    pass