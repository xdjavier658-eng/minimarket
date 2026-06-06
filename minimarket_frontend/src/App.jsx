import { Routes, Route, Navigate } from "react-router-dom";
import Layout from "./components/Layout";
import PrivateRoute from "./routes/PrivateRoute";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import ProductRegister from "./pages/ProductRegister";
import UserRegister from "./pages/UserRegister";
import PublicCatalog from "./pages/PublicCatalog";
import Reportes from "./pages/Reportes";
import PagoExitoso from "./pages/PagoExitoso";
import PagoFallido from "./pages/PagoFallido";
import PagoPendiente from "./pages/PagoPendiente";
import Ticket from "./pages/Ticket";

export default function App() {
  return (
    <Routes>
      {/* Ruta pública - Catálogo */}
      <Route path="/" element={<PublicCatalog />} />

      {/* Login admin */}
      <Route path="/admin/login" element={<Login />} />

      {/* Rutas protegidas del admin */}
      <Route
        path="/admin"
        element={
          <PrivateRoute>
            <Layout />
          </PrivateRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="ProductRegister" element={<ProductRegister />} />
        <Route path="UserRegister" element={<UserRegister />} />
        <Route path="reportes" element={<Reportes />} />
      </Route>

      {/* Rutas de pago (públicas) */}
      <Route path="/pago-exitoso" element={<PagoExitoso />} />
      <Route path="/pago-fallido" element={<PagoFallido />} />
      <Route path="/pago-pendiente" element={<PagoPendiente />} />
      <Route path="/ticket/:ventaId" element={<Ticket />} />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
