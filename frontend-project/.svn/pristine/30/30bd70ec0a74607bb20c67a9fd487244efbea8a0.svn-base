declare module '*.vue' {
    import type { DefineComponent } from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}

// 解决 vue-router 类型识别问题
import 'vue-router'
declare module 'vue-router' {
    interface RouteMeta {
        title?: string
    }
}