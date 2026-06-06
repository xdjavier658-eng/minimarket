import React from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useCart } from "../hooks/useCart";

export default function Ticket() {
  const { carrito: contextCarrito, total: contextTotal, limpiarCarrito } = useCart();
  const navigate = useNavigate();
  const location = useLocation();

  // Datos recibidos desde el modal (si existen)
  const { venta, carrito: stateCarrito, metodoPago: stateMetodoPago, total: stateTotal } = location.state || {};

  // Usar datos del state si vinieron, si no, usar el contexto
  const carrito = stateCarrito || contextCarrito;
  const total = stateTotal || contextTotal;
  const metodoPago = stateMetodoPago || (venta && venta.metodoPago) || "No especificado";
  const ventaId = venta?.ventaId || null;

  const igv = total * 0.18;
  const subtotal = total - igv;

  const formatCurrency = (num) =>
    num.toLocaleString("es-PE", { style: "currency", currency: "PEN" });

  const fecha = new Date();
  const fechaStr = fecha.toLocaleDateString("es-PE");
  const horaStr = fecha.toLocaleTimeString("es-PE", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });

  const handleCerrar = () => {
    // Si se realizó una venta, limpiar carrito del contexto
    if (ventaId) {
      limpiarCarrito();
    }
    navigate("/");
  };

  return (
    <div
      className="ticket-container border p-3 p-md-4 mt-3 mt-md-5 mx-auto"
      style={{
        maxWidth: 400,
        width: "100%",
        fontFamily: "Courier New, monospace",
        backgroundColor: "white",
      }}
    >
      <div className="text-center mb-3">
        <h4 className="mb-0">🛒 MiniMarket</h4>
        <small>Av. Principal 123, Trujillo</small>
        <br />
        <small>RUC: 20123456789</small>
        <br />
        <small>Tel: (044) 123-456</small>
        <hr />
      </div>

      <div className="d-flex justify-content-between mb-2 small">
        <span>
          <strong>Boleta Nº:</strong> {ventaId ? `B001-${ventaId}` : "B001-1176"}
        </span>
        <span>
          <strong>Fecha:</strong> {fechaStr}
        </span>
      </div>
      <div className="d-flex justify-content-between mb-3 small">
        <span>
          <strong>Hora:</strong> {horaStr}
        </span>
        <span>
          <strong>Cajero:</strong> Administrador
        </span>
      </div>

      <div className="mb-3 small">
        <strong>Cliente:</strong> Administrador
        <br />
        admin@minimarket.com
      </div>

      <table className="table table-sm" style={{ fontSize: "0.85rem" }}>
        <thead>
          <tr>
            <th>Producto</th>
            <th className="text-center">Cant.</th>
            <th className="text-end">P.Unit</th>
            <th className="text-end">Subtotal</th>
          </tr>
        </thead>
        <tbody>
          {carrito.map((item) => (
            <tr key={item.id}>
              <td>{item.nombre}</td>
              <td className="text-center">{item.cantidad}</td>
              <td className="text-end">{formatCurrency(item.precio)}</td>
              <td className="text-end">{formatCurrency(item.precio * item.cantidad)}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="text-end small mb-2">
        <div>Subtotal: {formatCurrency(subtotal)}</div>
        <div>IGV (18%): {formatCurrency(igv)}</div>
        <hr />
        <div style={{ fontWeight: "bold", fontSize: "1.2rem" }}>
          TOTAL: <span className="text-success">{formatCurrency(total)}</span>
        </div>
        <div className="mt-1">
          <small>Método de pago: <strong>{metodoPago}</strong></small>
        </div>
      </div>

      <div className="text-center small text-muted mt-3">
        <div>
          Total de productos: {carrito.reduce((acc, item) => acc + item.cantidad, 0)}
        </div>
        <div>¡Gracias por su compra! Vuelva pronto</div>
        <div className="mt-2">
          Esta es una representación electrónica de la boleta de venta
        </div>
      </div>

      <div className="d-flex justify-content-between mt-3">
        <button className="btn btn-secondary btn-sm" onClick={handleCerrar}>
          Cerrar
        </button>
        <button
          className="btn btn-success btn-sm"
          onClick={() => window.print()}
        >
          Imprimir
        </button>
      </div>
    </div>
  );
}