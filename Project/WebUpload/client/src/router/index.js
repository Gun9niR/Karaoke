import Vue from 'vue';
import Router from 'vue-router';
import Login from '../components/Login';
import Upload from '../components/Upload';

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
    }
  ]
});