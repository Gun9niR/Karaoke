<template>
  <div>
    <alert v-if="showErrorMessage"></alert>

    <el-card class="box-card" id="login-card">
      <div id="title-wrapper">
        <h2>
          <p class="title">天天爱K歌</p>
          <p class="title">管理员登录</p>
        </h2>
      </div>

      <div id="login-form-wrapper">
        <el-form
          :model="ruleForm"
          status-icon
          :rules="rules"
          ref="ruleForm"
          label-width="100px"
          class="demo-ruleForm"
        >
          <el-form-item label="用户名" prop="name">
            <el-input v-model="ruleForm.name" autocomplete="off"></el-input>
          </el-form-item>

          <el-form-item label="密码" prop="pass">
            <el-input
              type="password"
              v-model="ruleForm.pass"
              autocomplete="off"
            ></el-input>
          </el-form-item>

          <el-form-item id="button-wrapper">
            <el-button type="primary" @click="submitForm('ruleForm')">登录</el-button>
            <el-button @click="resetForm('ruleForm')">清空</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script>
import axios from "axios";
import Alert from "./Alert";

export default {
  data() {
    var validateName = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请输入用户名"));
      } else {
        if (this.ruleForm.pass !== "") {
          this.$refs.ruleForm.validateField("pass");
        }
        callback();
      }
    };

    var validatePass = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请输入密码"));
      } else {
        callback();
      }
    };

    return {
      showErrorMessage: false,
      ruleForm: {
        name: "",
        pass: "",
      },
      rules: {
        name: [{ validator: validateName, trigger: "blur" }],
        pass: [{ validator: validatePass, trigger: "blur" }],
      },
    };
  },

  components: {
    alert: Alert,
  },

  methods: {
    verify(payload) {
      const url = process.env.VUE_APP_AJAX_URL + '/login';
      axios
        .post(url, payload)
        .then((response) => {
            const user = response.data;
            const user_safe = {id: user['id'], username: user['username']}
            localStorage.setItem('user', JSON.stringify(user_safe));
            this.$router.push({ path: "/songs" });
            this.$store.commit("login");
        })
        .catch(() => {
          this.$message({
            message: '登录失败。',
            type: 'error',
          });
        });
    },

    initForm() {
      this.ruleForm.name = "";
      this.ruleForm.pass = "";
    },

    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const payload = {
            username: this.ruleForm.name,
            password: this.ruleForm.pass,
          };
          this.verify(payload);
          this.initForm();
        } else {
          console.log("error submit!!");
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
  },
};
</script>

<style scoped>
#login-card {
  max-width: 500px;
  max-height: 350px;
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  margin: auto;
}

#title-wrapper {
  margin: 25px;
  font-size: 18.5px;
}

.title {
  margin: 0;
}

#login-form-wrapper {
  max-width: 430px;
  margin: 0 auto;
  padding-right: 50px;
}

#button-wrapper {
  margin: 0, auto;
  padding-right: 40px;
}

#login-card {
  text-align: center;
}
</style>
