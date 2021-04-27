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
          :on-change="onOriginalChange"
          :on-preview="handlePreview"
          :on-remove="handleOriginalRemove"
          :on-exceed="handleExceed"
          :limit="1"
          :before-remove="beforeRemove"
          :file-list="fileListOriginal"
          :auto-upload="false"
        >
          <el-button size="small" type="primary">选择文件</el-button>
        </el-upload>
      </el-form-item>


      <el-form-item label="伴奏音频" prop="accompany">
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

      <el-form-item label="歌词" prop="lyric">
        <el-upload
          class="upload-demo"
          action="https://jsonplaceholder.typicode.com/posts/"
          :on-change="onLyricChange"
          :on-preview="handlePreview"
          :on-remove="handleLyricRemove"
          :on-exceed="handleExceed"
          :limit="1"
          :before-remove="beforeRemove"
          :file-list="fileListLyric"
          :auto-upload="false"
        >
          <el-button size="small" type="primary">选择文件</el-button>
        </el-upload>
      </el-form-item>

      <el-form-item label="MV" prop="mv">
        <el-upload
          class="upload-demo"
          action="https://jsonplaceholder.typicode.com/posts/"
          :on-change="onMVChange"
          :on-preview="handlePreview"
          :on-remove="handleMVRemove"
          :on-exceed="handleExceed"
          :limit="1"
          :before-remove="beforeRemove"
          :file-list="fileListMV"
          :auto-upload="false">
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
      fileListOriginal: [],
      fileListAccompany: [],
      fileListLyric: [],
      fileListMV: [],
      ruleForm: {},
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
        ],
      },
    };
  },

  methods: {
    onOriginalChange(file) {
      if (file.raw) {
        var original = file.raw;
        const fileType = getFileType(original);
        if (fileType !== 'wav') {
          this.$message.error("请选择wav格式文件！");
          this.fileListOriginal = [];
        } else {
          this.uploadFiles['original'] = original;
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
          this.uploadFiles['accompany_accompany'] = accompany;
        }
      }
    },
    onLyricChange(file) {
      if (file.raw) {
        var lyric = file.raw;
        const fileType = getFileType(lyric);
        if (fileType !== 'lrc') {
          this.$message.error("请选择lrc格式文件！");
          this.fileListLyric = [];
        } else {
          this.uploadFiles['lyric'] = lyric;
        }
      }
    },
    onMVChange(file) {
      if (file.raw) {
        var mv = file.raw;
        const fileType = getFileType(mv);
        if (fileType !== 'mp4') {
          this.$message.error("请选择mp4格式文件！");
          this.fileListMV = [];
        } else {
          this.uploadFiles['mv'] = mv;
        }
      }
    },
    handleOriginalRemove() {
      delete this.uploadFiles.original;
    },
    handleAccompanyRemove() {
      delete this.uploadFiles.accompany_accompany;
    },
    handleLyricRemove() {
      delete this.uploadFiles.lyric;
    },
    handleMVRemove() {
      delete this.uploadFiles.mv;
    },
    handlePreview(file) {
      console.log(file.raw);
    },
    beforeRemove(file) {
      return this.$confirm(`确定移除 ${ file.name }？`);
    },
    handleExceed() {
      this.$message.warning('限制选择 1 个文件');
    },
    clearFiles() {
      this.fileListOriginal = [];
      this.fileListAccompany = [];
      this.fileListLyric = [];
      this.fileListMV = [];
      this.uploadFiles = {};
    },
  },
};
</script>

