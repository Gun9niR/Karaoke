<template>
  <el-container>
    <el-header class="header-wrapper">
      <div>
        <h1 class="header-title">添加歌曲</h1>
      </div>
      <div class="header-button">
        <el-button size="middle" type="success" @click="onSubmit">上传文件</el-button>
      </div>
      <div class="header-button">
        <el-button size="middle" @click="redirectToSongs">查看曲库</el-button>
      </div>
      <div class="header-button">
        <el-button size="middle" @click="drawer = true">上传队列</el-button>
      </div>
    </el-header>

    <el-main>
      <div id="upload-wrapper">
        <el-row>          
          <el-col :span="8">
            <song-info-form ref="songInfo" class="upload-card" />
          </el-col>
          <el-col :span="8">
            <accompany-sing-form ref="accompanySing" class="upload-card" />
          </el-col>
          <el-col :span="8">
            <instrument-sing-form ref="instrumentSing" class="upload-card" @show-help-dialog="showHelpDialog" />
          </el-col>
        </el-row>
      </div>

      <el-drawer
        class="drawer"
        title="上传歌曲"
        :visible.sync="drawer"
        direction="btt">
        <div v-if="this.$store.state.uploadRequests.length === 0" class="empty-upload">
          没有正在上传的歌曲
        </div>
        <ul v-else style="list-style: none;">
          <li v-for="song in this.$store.state.uploadRequests" :key="song.songInfo">
            <progress-monitor 
              :song-info="song.songInfo" 
              :uploadFinished="song.uploadFinished"
              :chordFinished="song.chordFinished"
              :instrumentFinished="song.instrumentFinished"
              :rateFinished="song.rateFinished"
              :socket="socket"
            />
          </li>
        </ul>
      </el-drawer>
    </el-main>

    <el-dialog
      style="border-radius: 30px;"
      title="使用帮助"
      :visible.sync="helpDialogVisible"
      width="75%"
      custom-class="help-dialog">
      <span>
        “和弦语言”是一款用于定义软件在“自弹自唱”模式下行为的描述性编程语言。
        文件的第一行用以描述曲目的基本信息，接下来若干行依次描述每个被演奏的和弦名称以及持续的节拍数。
        下面以《时间煮雨》的和弦文件为例说明其使用方法。该文件如下：<br /><br />
        <div class="chord-scroll">
          85 -1 4 4	107.800<br />
          Cmaj7 2<br />
          Dsus2 2<br />
          Em 4<br />
          Cmaj7 2<br />
          Dsus2 2<br />
          Gadd9 4<br />
          Cmaj7 2<br />
          Dsus2 2<br />
          Em 2<br />
          Cmaj7 2<br />
          Am7 2<br />
          D11 2<br />
          Em 4<br />
          Cmaj7 2<br />
          Dsus2 2<br />
          Em 4<br />
          Cmaj7 2<br />
          Dsus2 2<br />
          Gadd9 4<br />
          Cmaj7 2<br />
          Dsus2 2<br />
          Em 1<br />
          D11 1<br />
          Cmaj7 2<br />
          Am7 2<br />
          D11 2<br />
          Gadd9 4<br />
        </div><br />
        根据百度上搜到的乐谱，该乐曲的曲速为85，乐谱上的移调半音数为-1，以4分音符为一拍，每小节4拍，用户的演奏片段在完整乐曲中的开始时间为107.800秒。<br />
        故文件的第一行为“85 -1 4 4 107.800”。其中第三个数字n和第四个数字m分别代表乐曲以n分音符为一拍，每小节m拍。<br />
        从第107.800秒开始，乐谱上和弦为Cmaj7，持续2拍，因此第1行为Cmaj7 2。<br />
        接着，乐谱上和弦为Dsus2，持续2拍，因此第2行为Dsus2 2。<br />
        以此类推编写和弦文件的每一行，<br />
        直到最后一小节，乐谱上和弦为Gadd9，持续4拍，因此最后一行为Gadd9 4。<br /><br />

        <b>需要注意的是，如果该文件存在语法错误或者出现了错误的和弦名称，则乐曲将无法成功上传。</b>
      </span>
      <!-- <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="helpDialogVisible = false">确 定</el-button>
      </span> -->
    </el-dialog>


  </el-container>
</template>

<script>
const io = require('socket.io-client')

import axios from 'axios';
import SongInfoForm from "./SongInfoForm";
import AccompanySingForm from "./AccompanySingForm";
import InstrumentSingForm from "./InstrumentSingForm";
import ProgressMonitor from "./ProgressMonitor";

export default {
  data() {
    return {
      socket: io('127.0.0.1:5000/karaoke', {
        transports: ['websocket']
      }),
      striped: true,
      files: [],
      progress: [],
      drawer: false,
      helpDialogVisible: false,
    };
  },

  components: {
    SongInfoForm,
    AccompanySingForm,
    InstrumentSingForm,
    ProgressMonitor,
  },

  mounted() {
    this.socket.on('upload', (songInfo) => {
      console.log('receive upload');
      this.$store.commit('onUploadFinish', songInfo);
    });
    this.socket.on('chord', (songInfo) => {
      console.log('receive chord');
      this.$store.commit('onChordFinish', songInfo);
    });
    this.socket.on('instrument', (songInfo) => {
      console.log('receive instrument');
      this.$store.commit('onInstrumentFinish', songInfo);
    });
    this.socket.on('rate', (songInfo) => {
      console.log('receive rate');
      this.$store.commit('onRateFinish', songInfo);
    });
  },

  methods: {

    redirectToSongs() {
      this.$router.push({path: '/songs'});
    },

    showHelpDialog() {
      this.helpDialogVisible = true;
    },

    onSubmit() {
      let config = { 
        headers: { 
          "Content-Type": "multipart/form-data",
        }
      };
      let uploadForm = new FormData();

      let songName = this.$refs.songInfo.ruleForm.song;
      if (songName.length === 0) {
        this.$message.error("请输入歌曲名称！");
        return;
      }

      let singer = this.$refs.songInfo.ruleForm.singer;
      if (singer.length === 0) {
        this.$message.error("请输入歌手！");
        return;
      }

      let album = this.$refs.songInfo.album;
      if (album === null) {
        this.$message.error("请上传专辑图片！");
        return;
      }

      let accompanyFiles = this.$refs.accompanySing.uploadFiles;
      if (Object.keys(accompanyFiles).length !== 4) {
        this.$message.error("请上传伴奏演唱模式相关文件！");
        return;
      }

      let instrumentFiles = this.$refs.instrumentSing.uploadFiles;
      if (Object.keys(instrumentFiles).length !== 1) {
        this.$message.error("请上传自弹自唱模式相关文件！");
        return;
      }

      let songInfo = songName + '-' + singer;
      this.$store.state.uploadRequests.forEach(song => {
        if (song.songInfo === songInfo) {
          this.$message.error('上传歌曲重复!');
          return;
        }
      });

      uploadForm.append('song_name', songName);
      uploadForm.append('singer', singer);
      uploadForm.append('album', album);

      for (let key in accompanyFiles) 
        uploadForm.append(key, accompanyFiles[key]);

      for (let key in instrumentFiles) 
        uploadForm.append(key, instrumentFiles[key]);

      let songRequest = {
        songInfo: songInfo,
        uploadFinished: false,
        chordFinished: false,
        instrumentFinished: false,
        rateFinished: false,
      }
      this.$store.commit('addUploadingSong', songRequest);

      this.$refs.songInfo.clearInfo();
      this.$refs.accompanySing.clearFiles();
      this.$refs.instrumentSing.clearFiles();

      const url = process.env.VUE_APP_AJAX_URL + '/uploadSong';
      axios.post(url, uploadForm, config)
        .then(() => {
          this.$store.commit('deleteUploadingSong', songInfo);
          this.$message({
            message: songInfo + '上传成功！',
            type: 'success',
          });
        })
        .catch(() => {
          this.$store.commit('deleteUploadingSong', songInfo);
          this.$message({
            message: songInfo + '上传失败！',
            type: 'error',
          });        
        });
    },
  },

  computed: {
    getLoginStatus() {
      return this.$store.state.loginStatus;
    },
  },

  watch: {
    getLoginStatus(status) {
      if (!status) {
        this.$router.push({ path: "/login" });
      }
    },
  },
};
</script>

<style scoped> 

.header-title {
  float: left;
}

.header-button {
  float: right;
  margin-top: 20px;
  padding-left: 10px;
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

#instrument-sing {
  height: 245px;
}

#rating-form {
  width: 400px;
  margin-top: 5px;
  height: 158.3px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}

.empty-upload {
  left: 20%;
  margin: 3% 45%;
}


.chord-scroll {
  max-height: 200px;
  overflow-y: auto;
}

</style>
