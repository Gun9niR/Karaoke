<template>
  <el-dialog title="更新歌曲信息" :visible.sync="visible">
    <el-form :model="song">
      <el-form-item label="歌曲名称" :label-width="editerLabelWidth" required>
        <el-input v-model="song.song_name" autocomplete="off" />
      </el-form-item>

      <el-form-item label="歌手" :label-width="editerLabelWidth" required>
        <el-input v-model="song.singer" autocomplete="off" />
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  data() {
    return {
      visible: false,
      song: {},
      editerLabelWidth: '120px',
    };
  },

  methods: {
    handleCancel() {
      this.visible = false;
    },
    handleSubmit() {
      if (this.song.song_name.length === 0) {
        this.$message({
          type: 'error',
          message: '请输入歌曲名称！'
        });
        return;
      } else if (this.song.singer.length === 0) {
        this.$message({
          type: 'error',
          message: '请输入歌手！'
        });
        return;
      }
      this.$emit('submit-song-info', this.song);
    }
  },
};
</script>
