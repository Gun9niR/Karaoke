<template>
  <el-card shadow="never" class="box-card">
    <div slot="header" class="clearfix">
      <span>伴奏演唱模式</span>
    </div>

    <el-form :inline="true" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-form-item label="原唱音频" prop="original">
        <el-upload
          class="upload-demo"
          action="https://jsonplaceholder.typicode.com/posts/"
          :on-preview="handlePreview"
          :on-remove="handleRemove"
          :before-remove="beforeRemove"
          :on-success="uploadSuccess"
          :on-error="uploadError"
          :limit="1"
          multiple
          :on-exceed="handleExceed"
          :file-list="fileListOriginal">
          <el-button size="small" type="primary">选择文件</el-button>
        </el-upload>
      </el-form-item>


      <el-form-item label="伴奏音频" prop="accompany">
        <el-upload
          class="upload-demo"
          action="https://jsonplaceholder.typicode.com/posts/"
          :on-preview="handlePreview"
          :on-remove="handleRemove"
          :before-remove="beforeRemove"
          :limit="1"
          multiple
          :on-exceed="handleExceed"
          :file-list="fileListAccompany">
          <el-button size="small" type="primary">选择文件</el-button>
        </el-upload>
      </el-form-item>

      <el-form-item label="歌词" prop="lyric">
        <el-upload
          class="upload-demo"
          action="https://jsonplaceholder.typicode.com/posts/"
          :on-preview="handlePreview"
          :on-remove="handleRemove"
          :on-error="uploadError"
          :before-remove="beforeRemove"
          :limit="1"
          multiple
          :on-exceed="handleExceed"
          :file-list="fileListLyric">
          <el-button size="small" type="primary">选择文件</el-button>
        </el-upload>
      </el-form-item>

      <el-form-item label="MV" prop="mv">
        <el-upload
          class="upload-demo"
          action="https://jsonplaceholder.typicode.com/posts/"
          :on-preview="handlePreview"
          :on-remove="handleRemove"
          :before-remove="beforeRemove"
          :limit="1"
          multiple
          :on-exceed="handleExceed"
          :file-list="fileListMV">
          <el-button size="small" type="primary">选择文件</el-button>
        </el-upload>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
export default {
  data() {
    return {
      ruleForm: {},
      fileListOriginal: [],
      fileListAccompany: [],
      fileListLyric: [],
      fileListMV: [],
      rules: {
          original: [
            { required: true },
          ],
          accompany: [
            { required: true, }
          ],
          lyric: [
            { required: true, }
          ],
          mv: [
            { required: true, }
          ]
      },
    };
  },

  methods: {
    handleRemove(file, fileList) {
      console.log(file, fileList);
    },
    handlePreview(file) {
      console.log(file);
    },
    uploadSuccess() {
      console.log("Success");
    },
    uploadError() {
      alert('Error!');
    },
    handleExceed() {
      this.$message.warning('限制选择 1 个文件');
    },
    beforeRemove(file) {
      return this.$confirm(`确定移除 ${ file.name }？`);
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
  border-color: #409eff;
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
</style>
