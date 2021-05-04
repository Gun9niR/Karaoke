drop table if exists songs;
 
create table songs (
   `id` bigint not null auto_increment,
   `song_name` varchar(255) not null,
   `singer` varchar(255) not null,
   `album` varchar(255) not null,
   `original` varchar(255) not null,
   `accompany_accompany` varchar(255) not null,
   `lyric` varchar(255) not null,
   `mv` varchar(255) not null,
   `instrument` varchar(255) not null,
   `accompany_instrument` varchar(255) not null,
   `rate` varchar(255) not null,
   primary key (`id`),
   unique key `unique_song` (`song_name`, `singer`)
) charset = utf8;

INSERT INTO songs VALUES (
   NULL, 'Attention', 'Charlie Puth', 
   '/Users/david/Study/SJTU/Courses/SE/songs/Attention-Charlie Puth/Charlie_Puth_Voicenotes.png',
   '/Users/david/Study/SJTU/Courses/SE/songs/Attention-Charlie Puth/Attention_vocal.wav',
   '/Users/david/Study/SJTU/Courses/SE/songs/Attention-Charlie Puth/Attention1.wav',
   '/Users/david/Study/SJTU/Courses/SE/songs/Attention-Charlie Puth/Attention.lrc',
   '/Users/david/Study/SJTU/Courses/SE/songs/Attention-Charlie Puth/Attention.mp4',
   '/Users/david/Study/SJTU/Courses/SE/songs/Attention-Charlie Puth/instrument.txt',
   '/Users/david/Study/SJTU/Courses/SE/songs/Attention-Charlie Puth/Attention2.wav',
   '/Users/david/Study/SJTU/Courses/SE/songs/Attention-Charlie Puth/Attention.f0a'
);
