import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    server: {
        proxy: {
            // 原有的 api 代理
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            },
            // 对上传文件夹的代理转发
            '/uploads': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    }
})