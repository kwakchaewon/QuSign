import { cpSync, mkdirSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const src = (p) => resolve(__dirname, '../node_modules/pdfjs-dist', p)
const dest = (p) => resolve(__dirname, '../public', p)

for (const dir of ['standard_fonts', 'cmaps', 'wasm']) {
  mkdirSync(dest(dir), { recursive: true })
  cpSync(src(dir), dest(dir), { recursive: true })
  console.log(`✓ copied pdfjs-dist/${dir} → public/${dir}`)
}
