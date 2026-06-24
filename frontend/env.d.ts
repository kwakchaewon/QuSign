/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent
  export default component
}

declare module 'pdfjs-dist/build/pdf.worker.min.mjs?url' {
  const src: string
  export default src
}
