<template>
  <el-container>
    <el-header class="header-wrapper">
      <h1 class="header-title">添加歌曲</h1>
      <div class="header-button">
        <el-button size="middle" type="success" @click="onSubmit" :loading="uploading">上传文件</el-button>
      </div>
      <div class="header-button">
        <el-button size="middle" @click="redirectToSongs">查看曲库</el-button>
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
            <instrument-sing-form ref="instrumentSing" id="instrument-sing" class="upload-card" />
            <rating-form ref="rating" id="rating-form" class="upload-card" />
          </el-col>
        </el-row>
      </div>
    </el-main>

  </el-container>
</template>

<script>
import axios from 'axios';
import SongInfoForm from "./SongInfoForm";
import AccompanySingForm from "./AccompanySingForm";
import InstrumentSingForm from "./InstrumentSingForm";
import RatingForm from './RatingForm.vue';

export default {
  data() {
    return {
      uploading: false,
      files: [],
    };
  },

  components: {
    SongInfoForm,
    AccompanySingForm,
    InstrumentSingForm,
    RatingForm, 
  },

  methods: {
    redirectToSongs() {
      this.$router.push({path: '/songs'});
    },
    onSubmit() {

      this.uploading = true;

      let config = { 
        headers: { "Content-Type": "multipart/form-data" }
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
      if (Object.keys(instrumentFiles).length !== 2) {
        this.$message.error("请上传自弹自唱模式相关文件！");
        return;
      }

      let ratingFiles = this.$refs.rating.uploadFiles;
      if (Object.keys(ratingFiles).length !== 1) {
        this.$message.error("请上传歌曲打分相关文件！");
        return;
      }

      uploadForm.append('song_name', songName);
      uploadForm.append('singer', singer);
      uploadForm.append('album', album);

      for (let key in accompanyFiles) 
        uploadForm.append(key, accompanyFiles[key]);

      for (let key in instrumentFiles) 
        uploadForm.append(key, instrumentFiles[key]);

      for (let key in ratingFiles)
        uploadForm.append(key, ratingFiles[key]);

      const url = process.env.VUE_APP_AJAX_URL + '/uploadSong';

      axios.post(url, uploadForm, config)
        .then(() => {
            this.$message({
              message: '歌曲上传成功！',
              type: 'success',
            });
            this.$refs.songInfo.clearInfo();
            this.$refs.accompanySing.clearFiles();
            this.$refs.instrumentSing.clearFiles();
            this.$refs.rating.clearFiles();
            this.uploading = false;
          })
          .catch(() => {
            this.$message.error('歌曲上传失败。');
            this.uploading = false;
          });
    }
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
  max-width: 400px;
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

</style>
