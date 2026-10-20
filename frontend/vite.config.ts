import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    port: 5173,
    // En desarrollo, /api se reenvía al backend Spring Boot.
    // Se quitan las cookies: la API usa token Bearer, y las cookies que otros proyectos
    // dejan en localhost pueden superar el límite de cabeceras de Tomcat (400).
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq) => proxyReq.removeHeader('cookie'));
        },
      },
    },
  },
});
