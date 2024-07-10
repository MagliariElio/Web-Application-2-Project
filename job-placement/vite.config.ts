import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  base: "/ui",
  server: {
    host: "0.0.0.0",
    port: 5173, // Puoi cambiare la porta qui
    proxy: {
      // Server 1: API principale
      '/documentStoreService/v1': {
        target: 'http://localhost:8081', // Indirizzo del server 1
        changeOrigin: true,
        rewrite: path => path.replace(/^\/documentStoreService\/v1/, '')
      },
      // Server 2: API secondaria
      '/crmService/v1/': {
        target: 'http://localhost:8082', // Indirizzo del server 2
        changeOrigin: true,
        rewrite: path => path.replace(/^\/crmService\/v1/, '')
      },
      // Server 3: API terziaria
      '/comunicationManagerService/v1/': {
        target: 'http://localhost:8083', // Indirizzo del server 3
        changeOrigin: true,
        rewrite: path => path.replace(/^\/comunicationManagerService\/v1/, '')
      }
    }
  }
})
