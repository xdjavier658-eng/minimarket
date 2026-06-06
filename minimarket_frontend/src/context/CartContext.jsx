import React, { createContext, useState } from "react";

export const CartContext = createContext();

export function CartProvider({ children }) {
  const [carrito, setCarrito] = useState([]);

  // Agregar producto (si ya existe, suma cantidad)
  function agregarProducto(producto) {
    setCarrito((prev) => {
      const existe = prev.find((p) => p.id === producto.id);
      if (existe) {
        return prev.map((p) =>
          p.id === producto.id ? { ...p, cantidad: p.cantidad + 1 } : p
        );
      } else {
        return [...prev, { ...producto, cantidad: 1 }];
      }
    });
  }

  // Disminuir cantidad o eliminar si llega a 0
  function disminuirProducto(id) {
    setCarrito((prev) => {
      return prev
        .map((p) =>
          p.id === id ? { ...p, cantidad: p.cantidad - 1 } : p
        )
        .filter((p) => p.cantidad > 0);
    });
  }

  // Eliminar producto directamente
  function eliminarProducto(id) {
    setCarrito((prev) => prev.filter((p) => p.id !== id));
  }

  // Vaciar carrito
  function limpiarCarrito() {
    setCarrito([]);
  }

  // Total calculado
  const total = carrito.reduce(
    (acc, item) => acc + item.precio * item.cantidad,
    0
  );

  return (
    <CartContext.Provider
      value={{
        carrito,
        agregarProducto,
        disminuirProducto,
        eliminarProducto,
        limpiarCarrito,
        total,
      }}
    >
      {children}
    </CartContext.Provider>
  );
}
