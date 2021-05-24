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

insert into songs values (
   NULL,  'Attention', 'Charlie Puth', 
   '.', '.', '.', '.', '.', '.', '.', '.'
);

