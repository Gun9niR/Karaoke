import Vue from 'vue';
import Router from 'vue-router';
import Login from '../components/Login';
import Upload from '../components/Upload';
import Songs from '../components/Songs.vue';

Vue.use(Router);

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login,
    },
    {
      path: '/upload',
      name: 'Upload',
      component: Upload,
      meta : {
        requireAuth: true, 
      },
    },
    {
      path: '/songs',
      name: 'Songs',
      component: Songs,
    }
  ]
});