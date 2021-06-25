<template>
  <el-button @click="logout">退出登录</el-button>
</template>

<script>
export default {
  methods: {
    logout() {
      const user = localStorage.getItem('user');
      if (user && Object.keys(user).length !== 0)
        localStorage.removeItem('user');
        this.$router.push({path: '/login'});
    }
  }
}
</script>