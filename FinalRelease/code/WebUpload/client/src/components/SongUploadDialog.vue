<template>
  <el-dialog 
    title="上传文件"
    width="27%"
    :visible.sync="visible" 
    @closed="resetDialog"
  >

  <div class="dialog-body">

    <div class="upload-dialog-note">
      请在上传原唱音频、歌词文件或和弦文件后进行文件同步。
    </div>

    <el-cascader
      class="file-type-selector"
      placeholder="请选择文件类型"
      v-model="selectedField"
      :options="options"
      @change="handleCascaderChange" 
    />

    <el-upload
      v-show="field === 'album'"
      class="upload-form"
      action="https://jsonplaceholder.typicode.com/posts/"
      accept="image/png"
      :on-remove="handleFileRemove"
      :on-change="handleFileChange"
      :on-exceed="handleFileNumberExceed"
      :limit="1"
      :file-list="fileList"
      list-type="picture"
      :auto-upload="false"
    >
      <el-button size="small" type="primary">选择图片</el-button>
      <div slot="tip" class="el-upload__tip">请上传png文件</div>
    </el-upload>

    <el-upload
      v-show="field !== '' && field !== 'album'"
      class="upload-form upload-demo"
      action="https://jsonplaceholder.typicode.com/posts/"
      :on-remove="handleFileRemove"
      :on-change="handleFileChange"
      :on-exceed="handleFileNumberExceed"
      :limit="1"
      :file-list="fileList"
      :auto-upload="false"
    >
      <el-button size="small" type="primary">选择文件</el-button>
      <div slot="tip" class="el-upload__tip">请上传{{fileSuffix}}文件</div>
    </el-upload>

    </div>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="uploading">确定</el-button>
    </div>

  </el-dialog>
</template>

<script>
export default {
  data() {
    return {
      uploading: false,
      visible: false,
      song: {},
      selectedField: [],
      uploadFile: null,
      fileList: [],
      options: [
        {
          value: 'songInfo',
          label: '歌曲信息',
          children: [
            {
              value: 'album',
              label: '专辑图',
            },
          ],
        },
        {
          value: 'accompanySing',
          label: '伴奏演唱模式',
          children: [
            {
              value: 'original',
              label: '原唱音频',
            },
            {
              value: 'accompany',
              label: '伴奏音频',
            },
            {
              value: 'lyric_accompany',
              label: '歌词',
            },
            {
              value: 'mv',
              label: 'MV',
            },
          ],
        },
        {
          value: 'instrumentSing',
          label: '自弹自唱模式',
          children: [
            {
              value: 'chord',
              label: '和弦',
            },
          ],
        },
      ],
    };
  },

  computed: {
    field: function() {
      return this.selectedField.length === 2 ? this.selectedField[1] : '';
    },
    fileSuffix: function() {
      const typeToSuffix = {
        'original': 'wav',
        'accompany': 'wav',
        'lyric': 'lrc',
        'mv': 'mp4',
        'chord': 'txt',
      };
      if (this.field in typeToSuffix)
        return typeToSuffix[this.field];
      return '';
    }
  },

  methods: {
    handleCascaderChange() {
      this.uploadFile = null;
      this.fileList = [];
    },
    handleCancel() {
      this.resetDialog();
    },
    handleSubmit() {
      if (this.uploadFile === null) {
        this.$message({
          message: '请选择上传文件！',
          type: 'error',
        });
        return;
      }
      this.uploading = true;
      this.$emit('update-song', this.song.id);
    },
    handleFileRemove() {
      this.uploadFile = null;
    },
    handleFileChange(file) {
      if (file.raw) {
        let fileObj = file.raw;
        if (!this.checkFileType(fileObj)) {
          this.fileList = [];
          this.$message({
            message: '文件格式不符合要求！',
            type: 'error',
          });
        } else {
          this.uploadFile = fileObj;
        }
      }
    },
    handleFileNumberExceed() {
      this.$message.warning('限制选择1个文件');
    },

    checkFileType(fileObj) {
      const filename = fileObj.name;
      const suffix = this.fileSuffix;
      if (suffix.length !== 0) {
        let suffixWithDot = '.' + suffix;
        return filename.endsWith(suffixWithDot);
      }
      return true;
    },
    resetDialog() {
      this.uploadFile = null;
      this.fileList = [];
      this.selectedField = [];
      this.visible = false;
    },
  },


};
</script>

<style scoped>

.dialog-body {
  position: relative;
  height: 230px;
}

.upload-form {
  display: block;
  margin-left: 3px;
}

.file-type-selector {
  margin-bottom: 20px;
  width: 200px;
}

.upload-dialog-note {
  color: gray; 
  font-size: 10px;
  margin-left: 5px;
  margin-bottom: 5px;
}

</style>