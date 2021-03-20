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
          :on-preview="handlePreview"
          :on-remove="handleRemove"
          :before-remove="beforeRemove"
          :on-success="uploadSuccess"
          :on-error="uploadError"
          :limit="1"
          multiple
          :on-exceed="handleExceed"
          :file-list="fileListInstrument">
          <el-button size="small" type="primary">选择文件</el-button>
        </el-upload>
      </el-form-item>


      <el-form-item label="伴奏" prop="accompany">
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

    </el-form>
  </el-card>
</template>

<script>
export default {
  data() {
    return {
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
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}
</style>
