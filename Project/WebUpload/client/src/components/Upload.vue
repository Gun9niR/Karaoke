<template>
  <el-container>
    <el-header class="header-wrapper">
      <div><h1 class="header-title">更新曲库</h1></div>
      <div class="header-button"><el-button size="middle" type="success">上传文件</el-button></div>
    </el-header>

    <el-main>
      <div id="upload-wrapper">
        <el-row>
          <el-col :span="8">
            <song-info class="upload-card"></song-info>
          </el-col>
          <el-col :span="8">
            <accompany-sing class="upload-card"></accompany-sing>
          </el-col>
          <el-col :span="8">
            <instrument-sing class="upload-card"></instrument-sing>
          </el-col>
        </el-row>
      </div>
    </el-main>

  </el-container>
</template>

<script>
import SongInfo from "./SongInfo";
import AccompanySing from "./AccompanySing";
import InstrumentSing from "./InstrumentSing";

export default {
  data() {
    return {};
  },

  components: {
    SongInfo,
    AccompanySing,
    InstrumentSing,
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
</style>
