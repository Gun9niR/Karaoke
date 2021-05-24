<template>
  <el-card shadow="never" class="box-card">
    <div slot="header" class="clearfix">
      <span>自弹自唱模式</span>
    </div>

    <el-form :inline="true" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      
      <el-form-item label="和弦" prop="chord">
        <el-upload
          class="upload-demo"
          action="https://jsonplaceholder.typicode.com/posts/"
          :on-change="onChordChange"
          :on-preview="handlePreview"
          :on-remove="handleChordRemove"
          :on-exceed="handleExceed"
          :limit="1"
          :before-remove="beforeRemove"
          :file-list="fileListChord"
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
      ruleForm: {},
      fileListChord: [],
      rules: {
          chord: [
            { required: true },
          ],
          accompany: [
            { required: true, }
          ],
      },
    };
  },

  methods: {
    onChordChange(file) {
      if (file.raw) {
        var chord = file.raw;
        const fileType = getFileType(chord);
        if (fileType !== 'txt') {
          this.$message.error("请选择txt格式文件！");
          this.fileListChord = [];
        } else {
          this.uploadFiles['chord'] = chord;
        }
      }
    },
    
    handleChordRemove() {
      delete this.uploadFiles.chord;
    },
    
    handlePreview(file) {
      console.log(file);
    },
    handleExceed() {
      this.$message.warning('限制选择 1 个文件');
    },
    beforeRemove(file) {
      return this.$confirm(`确定移除 ${ file.name }？`);
    },
    clearFiles() {
      this.fileListChord = [];
      this.uploadFiles = {};
    },
  },
};
</script>

