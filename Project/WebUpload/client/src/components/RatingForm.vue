<template>
  <el-card shadow="never" class="box-card">
    <div slot="header" class="clearfix">
      <span>歌曲打分</span>
    </div>

    <el-form :inline="true" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      <el-form-item label="人声音频" prop="rate">
        <el-upload
          class="upload-demo"
          action="https://jsonplaceholder.typicode.com/posts/"
          :on-change="onRateChange"
          :on-remove="handleRateRemove"
          :on-exceed="handleExceed"
          :limit="1"
          :before-remove="beforeRemove"
          :file-list="fileListRate"
          :auto-upload="false"
        >
          <el-button size="small" type="primary">选择文件</el-button>
        </el-upload>
      </el-form-item>
    </el-form>

  </el-card>
</template>

<script>
import { getFileType } from '../utils/utils';

export default {
  data() {
    return {
      uploadFiles: {},
      fileListRate: [],
      ruleForm: {},
      rules: {
        rate: [
          { required: true },
        ],
      }
    };
  },

  methods: {
    onRateChange(file) {
      if (file.raw) {
        let rate = file.raw;
        const fileType = getFileType(rate);
        if (fileType !== 'wav') {
          this.$message.error("请选择wav格式文件！");
          this.fileListRate = [];
        } else {
          this.uploadFiles['rate'] = rate;
        }
      }
    },
    handleRateRemove() {
      delete this.uploadFiles.rate;
    },
    beforeRemove(file) {
      return this.$confirm(`确定移除 ${ file.name }？`);
    },
    handleExceed() {
      this.$message.warning('限制选择 1 个文件');
    },
    clearFiles() {
      this.fileListRate = [];
      this.uploadFiles = {};
    },
  }
}
</script>
