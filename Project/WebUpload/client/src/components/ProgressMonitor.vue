<template>
  <div class="drawer-content">
    <span class="monitor-item monitor-song-name">{{songInfo}}</span>
    <span class="monitor-item">
      <span v-if="!uploadFinished"><i class="el-icon-loading" /> 文件上传中</span>
      <span v-else><i class="el-icon-success" /> 文件上传成功</span>
    </span>
    <span class="monitor-item">
      <span v-if="!chordFinished"><i class="el-icon-loading" /> 正在执行和弦解释器</span>
      <span v-else><i class="el-icon-success" /> 和弦文件解释成功</span>
    </span>
    <span class="monitor-item">
      <span v-if="!instrumentFinished"><i class="el-icon-loading" /> 正在进行AI音轨分离</span>
      <span v-else><i class="el-icon-success" /> AI音轨分离成功</span>
    </span>
    <span class="monitor-item">
      <span v-if="!rateFinished"><i class="el-icon-loading" /> 正在进行人声基频分析</span>
      <span v-else><i class="el-icon-success" /> 人声基频分析成功</span>
    </span>
  </div>
</template>

<script>
export default {
  props: {
    songInfo: String,
    uploadFinished: Boolean,
    chordFinished: Boolean,
    instrumentFinished: Boolean,
    rateFinished: Boolean,
  },
};
</script>

<style>
.drawer-content {
  margin: 0 20px;
}

.monitor-item {
  margin: 0 10px;
  font-size: 15px;
}

.monitor-song-name {
  font-size: 17px;
  font-weight: 600;
}

.el-icon-success {
  color: rgb(94 160 47);
}
</style>