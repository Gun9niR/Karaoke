import Vue from 'vue';
import Router from 'vue-router';
import Login from '../components/Login';
import Upload from '../components/Upload';
import Songs from '../components/Songs.vue';

Vue.use(Router);

const router = new Router({
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
        requireAuth: false, 
      },
    },
    {
      path: '/songs',
      name: 'Songs',
      component: Songs,
      meta : {
        requireAuth: false, 
      },
    }
  ]
});

router.beforeEach((to, from, next) => {
  if (to.matched.some((r) => r.meta.requireAuth)) {
    const user = localStorage.getItem('user');
    if (user && Object.keys(user).length !== 0) {
      next();
    } else {
      next({
        path: '/login',
        query: {redirect: to.fullPath}
      });
    }
  } else {
    next();
  }
});

export default router;