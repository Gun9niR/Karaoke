<template>
  <el-container>
    <el-header class="header-wrapper">
      <h1 class="header-title">曲库</h1>
      <div class="header-button">
        <el-button size="middle" type="success" @click="redirectToUpload">添加歌曲</el-button>
        <logout-button />
      </div>
    </el-header>

    <el-main>
      <el-table
        :data="songData"
        height="calc(100vh - 120px)"
        border
        style="width: 100%"
        :header-cell-style="{textAlign: 'center'}"
        :cell-style="{textAlign: 'center'}"
      >
        <el-table-column
          fixed
          prop="song_name"
          label="歌曲"
        >
        </el-table-column>

        <el-table-column
          fixed
          prop="singer"
          label="歌手"
        >
        </el-table-column>

        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button
              size="mini"
              @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              size="mini"
              type="primary"
              @click="handleUpload(scope.row)">上传文件</el-button>
            <el-button
              size="mini"
              type="primary"
              :loading="syncSongs.indexOf(scope.row.song_name + '-' + scope.row.singer) !== -1"
              @click="handleSync(scope.row)">同步文件</el-button>
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <song-upload-dialog @update-song="updateSong" ref="songUploadDialog" />
      <song-info-editer @submit-song-info="submitSongInfo" ref="songInfoEditer" />
    </el-main>
  </el-container>
</template>

<script>
const io = require('socket.io-client')

import axios from 'axios';
import SongInfoEditer from './SongInfoEditer';
import SongUploadDialog from './SongUploadDialog';
import LogoutButton from './LogoutButton';

export default {
  data() {
    return {
      socket: io('127.0.0.1:5000/karaoke', {
        transports: ['websocket']
      }),
      songData: [],
      editSong: {},
      editerVisible: false,
    };
  },

  components: {
    SongInfoEditer,
    SongUploadDialog,
    LogoutButton,
  },

  mounted() {
    this.socket.on('sync-success', (songInfo) => {
      this.$store.commit('finishSyncSong', songInfo);
      console.log('Successfully synchronized all the files.');
      this.$message({
        message: '成功同步' + songInfo + '的所有文件。',
        type: 'success',
      });
    });
    this.socket.on('sync-fail', (songInfo) => {
      this.$store.commit('finishSyncSong', songInfo);
      console.log('Fail to synchronize the files.');
      this.$message({
        message: '同步' + songInfo + '文件失败。',
        type: 'error',
      });
    });
  },

  computed: {
    syncSongs: function () {
      return this.$store.state.syncSongs;
    }
  },

  methods: {
    redirectToUpload() {
      this.$router.push({path: '/upload'});
    },

    handleEdit(row) {
      let song = JSON.parse(JSON.stringify(row));
      this.$refs.songInfoEditer.song = song;
      this.$refs.songInfoEditer.visible = true;
    },

    handleUpload(row) {
      let song = JSON.parse(JSON.stringify(row));
      this.$refs.songUploadDialog.song = song;
      this.$refs.songUploadDialog.visible = true;
    },

    handleSync(row) {
      let song = JSON.parse(JSON.stringify(row));
      let song_info = song.song_name + '-' + song.singer;
      this.$store.commit('startSyncSong', song_info);
      this.syncSong(song.id, song_info);
    },

    handleDelete(row) {
      const h = this.$createElement;
      this.$msgbox({
        title: '提示',
        message: h('p', null, [
          h('span', null, '确认删除歌曲'),
          h('span', { style: 'color: #af3112' }, row.song_name),
          h('span', null, '及其所有文件？'),
        ]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            instance.confirmButtonLoading = true;
            instance.confirmButtonText = '正在删除';
            this.deleteSong(row, instance, done);
          } else {
            done();
          }
        },
      }).then(() => {
        this.$message({
          type: 'success',
          message: '删除成功!'
        });
      });
    },

    getSongData() {
      const url = process.env.VUE_APP_AJAX_URL + '/getSongs';
      axios.get(url)
        .then((response) => {
          this.songData = response.data;
        })
        .catch((error) => {
          console.log(error);
          this.$message.error("获取歌曲信息失败!");
        });
    },

    deleteSong(song, instance, done) {
      const url = process.env.VUE_APP_AJAX_URL + '/deleteSong';
      axios.delete(url, { data: { id: song.id }})
        .then(() => {
          this.getSongData();
          instance.confirmButtonLoading = false;
          done();
        })
        .catch((error) => {
          console.log(error);
          this.$message.error("删除歌曲失败!");
        });
    },

    submitSongInfo(song) {
      const url = process.env.VUE_APP_AJAX_URL + '/updateSongInfo';
      axios.post(url, song)
        .then(() => {
          this.$refs.songInfoEditer.visible = false;
          this.$message({
            message: '成功更新歌曲信息！',
            type: 'success',
          });
          this.getSongData();
        })
        .catch((error) => {
          console.log(error);
          this.$message({
            message: '歌曲信息更新失败。',
            type: 'error',
          });
        });
    },

    updateSong(id) {

      const url = process.env.VUE_APP_AJAX_URL + '/uploadOneFile/' + id;
      let uploadForm = new FormData();
      let field = this.$refs.songUploadDialog.field;
      let uploadFile = this.$refs.songUploadDialog.uploadFile;
      uploadForm.append(field, uploadFile);

      let config = { 
        headers: { "Content-Type": "multipart/form-data" }
      };

      axios.post(url, uploadForm, config)
        .then(() => {
          this.$refs.songUploadDialog.uploading = false;
          this.$refs.songUploadDialog.resetDialog();
          this.$message({
            message: '成功上传文件！',
            type: 'success',
          });
        })
        .catch(() => {
          this.$refs.songUploadDialog.uploading = false;
          this.$message({
            message: '文件上传失败。',
            type: 'error',
          });
        });
    },

    syncSong(song_id, song_info) {
      this.socket.emit('sync', song_id, song_info);
    },
  },

  created() {
    this.getSongData();
  }
};
</script>

<style scoped> 

.header-title {
  float: left;
}

.header-button {
  float: right;
  margin-top: 20px;
}


#upload-wrapper {
  margin: 20px 10px;
}

.upload-card {
  float: left;
  padding: 20px;
}

.box-card {
  width: 400px;
  height: 450px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}

</style>