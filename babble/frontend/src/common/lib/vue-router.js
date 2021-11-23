import { createRouter, createWebHistory } from 'vue-router'
import store from '@/common/lib/store'
import Home from '@/views/home/home'
import Category from '@/views/categories/category'
import MyPage from '@/views/mypage/my-page'
import Keyword from '@/views/mypage/components/keyword'
import History1 from '@/views/mypage/components/history1'
import History2 from '@/views/mypage/components/history2'
import UserInfo from '@/views/mypage/components/user-info'
import CategoryResult from '@/views/categories/components/category-result'
import ConferencesDetail from '@/views/conferences/conference-detail'
import SearchResult from '@/views/search/search-result'
import ErrorPage from '@/views/error/error-page'
import AboutUs from '@/views/home/about-us'

const fullMenu = require('@/views/main/menu.json')
const categories_list = ['all', 'sports', 'cooking', 'handcraft', 'music', 'finance', 'game', 'movie', 'drawing', 'book', 'pet']
const categoryChildren = []
for (let index = 0; index < categories_list.length; index++) {
  categoryChildren.push({
    name: `${categories_list[index]}`,
    path: `${categories_list[index]}`,
    component: CategoryResult,
  })
}

function makeRoutesFromMenu() {
  let routes = Object.keys(fullMenu).map((key) => {
    if (key === 'home') {
      store.commit('menu/setMenuActive', 0)
      return { path: fullMenu[key].path, name: key, component: Home  }
    } else if (key === 'category') {
      store.commit('menu/setMenuActive', 1)
      return { path: fullMenu[key].path, name: key, component: Category, children: categoryChildren}
    } else if (key === 'mypage') {
      store.commit('menu/setMenuActive', 2)
      return { path: fullMenu[key].path, name: key, component: MyPage,
      children: [
        {
          name: 'keyword',
          path: 'keyword',
          component: Keyword
        },
        {
          name: 'history1',
          path: 'history1',
          component: History1
        },
        {
          name: 'history2',
          path: 'history2',
          component: History2
        },
        {
          name: 'user-info',
          path: 'user-info',
          component: UserInfo
        },

      ]}
    } else if (key === 'search-result') {
      store.commit('menu/setMenuActive', 3)
      return { path: fullMenu[key].path, name: key, component: SearchResult}
    } else if (key === 'about-us') {
      store.commit('menu/setMenuActive', 4)
      return { path: fullMenu[key].path, name: key, component: AboutUs  }
    } else { // menu.json 에 들어있는 로그아웃 메뉴
      return null
    }
  })

  // 로그아웃 파싱한 부분 제거
  routes = routes.filter(item => item)

  // menu 자체에는 나오지 않는 페이지 라우터에 추가(방 상세보기)
  routes.push({
    path: '/conferences/:conferenceId',
    name: 'conference-detail',
    component: ConferencesDetail
  })

  routes.push({
    path: '/error',
    name: 'error',
    component: ErrorPage
  })

  // 카카오 라우터에 추가해주기
  routes.push({
    path: '/oauth/:pathMatch(.*)*',
  })

  // 구글 라우터에 추가해주기
  routes.push({
    path: '/login/oauth2/code/google/:pathMatch(.*)*',
  })

  // 라우터에 존재하지 않을 경우 에러페이지로 이동
  routes.push({
    path: '/:pathMatch(.*)*',
    redirect: "/error"
  })




  return routes
}

const routes = makeRoutesFromMenu()

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(function (to, from, next) {
  // 마이페이지
  if (to.name == 'keyword' || to.name == 'history1' || to.name == 'history2' || to.name == 'user-info') {
    // 로그인 확인
    let isAuthenticated = false
    if (store.getters['auth/getToken'] != null) {
      isAuthenticated = true
    }
    // 로그인 안됐으면 alert
    if (!isAuthenticated) {
      alert('로그인이 필요한 페이지입니다.')
      router.go('-1')
    }
  }
  
  // 카테고리 페이지
  if (categories_list.indexOf(to.name) >= 0) {
    store.commit('menu/setActiveCategory', to.name)
  }

  // 검색 페이지
  if (to.name == 'search-result') {
    store.commit("menu/setSearchWord", to.params.searchword);
  }
  next()
})


router.afterEach((to) => {
})

export default router
