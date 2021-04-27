import Vue from "vue";
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
import App from "./App.vue";
import router from "./router";
import axios from "axios";
import Vuex from "vuex";


Vue.use(ElementUI);
Vue.use(Vuex);


Vue.config.productionTip = false;
axios.defaults.baseURL = "http://10.166.32.42:5000";

const store = new Vuex.Store({
  state: {
    loginStatus: false,
  },
  mutations: {
    login() {
      this.state.loginStatus = true;
    },
    loginExpire() {
      this.state.loginStatus = false;
    },
  },
  getters: {
    isLogin() {
      return this.state.loginStatus;
    }
  },
});

// router.beforeEach((to,from,next) => {
//   if (to.meta.requireAuth) {
//       if (store.getters.isLogin) {
//           next();
//       } else {
//           next({
//               path : '/login',
//               query : {redirect : to.fullPath}
//           });
//       }
//   } else {
//       next();
//   }
// });


new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount("#app");
