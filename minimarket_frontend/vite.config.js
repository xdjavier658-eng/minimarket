import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/img": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
  build: {
    // El build va directo a la carpeta static del backend
    outDir: "../minimarket_backend/src/main/resources/static",
    emptyOutDir: true,
  },
});
