<template>
  <el-card shadow="never" class="box-card">
    <div slot="header" class="clearfix">
      <span>自弹自唱模式</span>
    </div>

    <el-form :inline="true" :model="ruleForm" :rules="rules" ref="ruleForm" label-width="100px" class="demo-ruleForm">
      
      <el-form-item label="和弦" prop="instrument">
        <el-upload
          class="upload-demo"
          action="https://jsonplaceholder.typicode.com/posts/"
          :on-change="onInstrumentChange"
          :on-preview="handlePreview"
          :on-remove="handleInstrumentRemove"
          :on-exceed="handleExceed"
          :limit="1"
          :before-remove="beforeRemove"
          :file-list="fileListInstrument"
          :auto-upload="false"
        >
          <el-button size="small" type="primary">选择文件</el-button>
        </el-upload>
      </el-form-item>


      <el-form-item label="伴奏" prop="accompany">
        <el-upload
          class="upload-demo"
          action="https://jsonplaceholder.typicode.com/posts/"
          :on-change="onAccompanyChange"
          :on-preview="handlePreview"
          :on-remove="handleAccompanyRemove"
          :on-exceed="handleExceed"
          :limit="1"
          :before-remove="beforeRemove"
          :file-list="fileListAccompany"
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
      fileListInstrument: [],
      fileListAccompany: [],
      rules: {
          instrument: [
            { required: true },
          ],
          accompany: [
            { required: true, }
          ],
      },
    };
  },

  methods: {
    onInstrumentChange(file) {
      if (file.raw) {
        var instrument = file.raw;
        const fileType = getFileType(instrument);
        if (fileType !== 'txt') {
          this.$message.error("请选择txt格式文件！");
          this.fileListInstrument = [];
        } else {
          this.uploadFiles['instrument'] = instrument;
        }
      }
    },
    onAccompanyChange(file) {
      if (file.raw) {
        var accompany = file.raw;
        const fileType = getFileType(accompany);
        if (fileType !== 'wav') {
          this.$message.error("请选择wav格式文件！");
          this.fileListAccompany = [];
        } else {
          this.uploadFiles['accompany_instrument'] = accompany;
        }
      }
    },
    handleInstrumentRemove() {
      delete this.uploadFiles.instrument;
    },
    handleAccompanyRemove() {
      delete this.uploadFiles.accompany_instrument;
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
      this.fileListInstrument = [];
      this.fileListAccompany = [];
      this.uploadFiles = {};
    },
  },
};
</script>

