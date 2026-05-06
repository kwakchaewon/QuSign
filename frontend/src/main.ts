import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import './assets/tokens.css'
import './assets/login.css'
import './assets/signup.css'
import './assets/dashboard.css'
import './assets/request.css'
import './assets/signer.css'
import './assets/verify.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
