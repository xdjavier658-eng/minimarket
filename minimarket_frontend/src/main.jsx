import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { CartProvider } from "./context/CartContext";
import App from "./App";
import "bootstrap/dist/css/bootstrap.min.css"; // 👈 IMPORTANTE
import "bootstrap-icons/font/bootstrap-icons.css";
import 'bootstrap/dist/js/bootstrap.bundle.js';
import "./index.css";

console.log("🚀 main.jsx cargado");

const root = ReactDOM.createRoot(document.getElementById("root"));
console.log("✓ Root creado");

root.render(
  <React.StrictMode>
    <BrowserRouter>
      <CartProvider>
        <App />
      </CartProvider>
    </BrowserRouter>
  </React.StrictMode>
);

console.log("✓ Aplicación renderizada");


