import Vue from "vue";
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
import App from "./App.vue";
import router from "./router";
import Vuex from "vuex";
import createPersistedState from 'vuex-persistedstate'

Vue.use(ElementUI);
Vue.use(Vuex);

Vue.config.productionTip = false;

const store = new Vuex.Store({
  plugins: [createPersistedState({
      storage: window.sessionStorage,
  })],
  state: {
    loginStatus: false,
    uploadRequests: [],
    syncSongs: [],
  },
  mutations: {
    login(state) {
      state.loginStatus = true;
    },
    loginExpire(state) {
      state.loginStatus = false;
    },
    addUploadingSong(state, songRequest) {
      state.uploadRequests.push(songRequest);
    },
    deleteUploadingSong(state, songInfo) {
      let n = state.uploadRequests.length;
      for (let i = 0; i < n; i++) 
        if (state.uploadRequests[i].songInfo === songInfo) {
          state.uploadRequests.splice(i, 1);
          break;
        }
    },
    onUploadFinish(state, songInfo) {
      let n = state.uploadRequests.length;
      for (let i = 0; i < n; i++) 
        if (state.uploadRequests[i].songInfo === songInfo) {
          state.uploadRequests[i].uploadFinished = true;
          break;
        }
    },
    onChordFinish(state, songInfo) {
      let n = state.uploadRequests.length;
      for (let i = 0; i < n; i++) 
        if (state.uploadRequests[i].songInfo === songInfo) {
          state.uploadRequests[i].chordFinished = true;
          break;
        }
    },
    onInstrumentFinish(state, songInfo) {
      let n = state.uploadRequests.length;
      for (let i = 0; i < n; i++) 
        if (state.uploadRequests[i].songInfo === songInfo) {
          state.uploadRequests[i].instrumentFinished = true;
          break;
        }
    },
    onRateFinish(state, songInfo) {
      let n = state.uploadRequests.length;
      for (let i = 0; i < n; i++) 
        if (state.uploadRequests[i].songInfo === songInfo) {
          state.uploadRequests[i].rateFinished = true;
          break;
        }
    },
    startSyncSong(state, songInfo) {
      state.syncSongs.push(songInfo);
    },
    finishSyncSong(state, songInfo) {
      let n = state.syncSongs.length;
      for (let i = 0; i < n; i++)
        if (state.syncSongs[i] == songInfo) {
          state.syncSongs.splice(i, 1);
          break;
        }
    },
  },
  getters: {
    isLogin() {
      return this.state.loginStatus;
    },
    getUploadingSongs(state) {
      return state.uploadRequests;
    },
  },
});

new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount("#app");
