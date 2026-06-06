import { useState, useMemo } from "react";

/**
 * Hook para filtrar productos por categoría.
 * @param {Array} productos - Lista completa de productos
 * @param {string} categoriaInicial - Categoría por defecto ("Todos")
 */
export function useFiltradoProductos(productos, categoriaInicial = "Todos") {
  const [categoriaSeleccionada, setCategoriaSeleccionada] = useState(categoriaInicial);

  const productosFiltrados = useMemo(() => {
    if (categoriaSeleccionada === "Todos") {
      return productos;
    } else {
      return productos.filter((p) => p.categoria === categoriaSeleccionada);
    }
  }, [productos, categoriaSeleccionada]);

  return { productosFiltrados, categoriaSeleccionada, setCategoriaSeleccionada };
}
