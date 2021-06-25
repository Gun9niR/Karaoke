<template>
  <el-card shadow="never" class="box-card">
    <div slot="header" class="clearfix">
      <span>歌曲信息</span>
    </div>

    <el-form :inline="true" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm-inline">
      <el-form-item label="歌曲名称" prop="song">
        <el-input v-model="ruleForm.song"></el-input>
      </el-form-item>

      <el-form-item label="歌手" prop="singer">
        <el-input v-model="ruleForm.singer"></el-input>
      </el-form-item>

      <el-form-item label="专辑图" prop="album">
          <el-upload
            class="avatar-uploader"
            action="https://jsonplaceholder.typicode.com/posts/"
            accept="image/png"
            :show-file-list="false"
            :on-change="onCoverChange"
            :auto-upload="false"
          >
            <img v-if="imageUrl" :src="imageUrl" class="avatar" />
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
export default {
  data() {
    return {
      album: null,
      imageUrl: '',
      ruleForm: {
        song: '',
        singer: ''
      },
      rules: {
          song: [
            { required: true, message: '请输入歌曲名称', trigger: 'blur' },
          ],
          singer: [
            { required: true, message: '请输入歌手', trigger: 'blur' },
          ],
          album: [
            { required: true }
          ],
      },
    };
  },

  methods: {
    onCoverChange(file) {
      if (file.raw) {
        let cover = file.raw;
        this.imageUrl = URL.createObjectURL(cover);
        this.album = cover;
      }
    },
    clearInfo() {
      this.imageUrl = '';
      this.ruleForm.song = '';
      this.ruleForm.singer = '';
      this.album = null;
    },
  },
};
</script>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}
.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}

.box-card {
  max-width: 1000px;
}
</style>
