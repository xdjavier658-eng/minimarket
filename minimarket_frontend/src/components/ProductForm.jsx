import { useState, useEffect } from "react";

const categorias = [
  "Lácteos",
  "Panadería",
  "Abarrotes",
  "Cereales",
  "Frutas",
  "Verduras",
  "Bebidas",
  "Snacks",
  "Limpieza",
  "Carnes",
];

export default function ProductForm({ onAddProduct, onExit }) {
  const [nombre, setNombre] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [categoria, setCategoria] = useState("Lácteos");
  const [precio, setPrecio] = useState("");
  const [imagen, setImagen] = useState("");

  const [mensaje, setMensaje] = useState("");
  const [tipoMensaje, setTipoMensaje] = useState(""); // "success" | "error"
  const [loading, setLoading] = useState(false);

  // Para que el mensaje desaparezca solo después de 2 segundos
  useEffect(() => {
    if (mensaje) {
      const timer = setTimeout(() => setMensaje(""), 2000);
      return () => clearTimeout(timer);
    }
  }, [mensaje]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!nombre || !precio || !descripcion) {
      setTipoMensaje("error");
      setMensaje("Por favor completa todos los campos obligatorios.");
      return;
    }

    setLoading(true);
    setMensaje("");

    const nuevoProducto = {
      nombre,
      descripcion,
      categoria,
      precio: parseFloat(precio),
      imagen: imagen || "default.png",
    };

    try {
      await onAddProduct(nuevoProducto);

      setTipoMensaje("success");
      setMensaje(`Producto "${nombre}" registrado correctamente.`);

      // Limpiar formulario
      setNombre("");
      setDescripcion("");
      setCategoria("Lácteos");
      setPrecio("");
      setImagen("");
    } catch (error) {
      console.error(error);
      setTipoMensaje("error");
      setMensaje("Error al registrar producto. Intenta de nuevo.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className=" min-h-100 d-flex align-items-center justify-content-center">
      <div className="card shadow-lg p-4">
        <form onSubmit={handleSubmit} className="mb-4">
          <h5 className="text-center text-success fw-bold">Registrar Producto</h5>

          <div className="mb-3">
            <label className="form-label">Nombre</label>
            <input
              type="text"
              className="form-control"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              required
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Descripción</label>
            <input
              type="text"
              className="form-control"
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              required
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Categoría</label>
            <select
              className="form-select"
              value={categoria}
              onChange={(e) => setCategoria(e.target.value)}
            >
              {categorias.map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label">Precio (S/)</label>
            <input
              type="number"
              step="0.01"
              min="0"
              className="form-control"
              value={precio}
              onChange={(e) => setPrecio(e.target.value)}
              required
            />
          </div>

          <div className="mb-3">
            <label className="form-label">Nombre imagen (opcional)</label>
            <input
              type="text"
              className="form-control"
              value={imagen}
              onChange={(e) => setImagen(e.target.value)}
              placeholder="ejemplo.png"
            />
            <small className="text-muted">
              Coloca el nombre del archivo de imagen (opcional)
            </small>
          </div>

          <button type="submit" className="btn btn-success" disabled={loading}>
            {loading ? "Guardando..." : "Agregar Producto"}
          </button>

          {/* Botón de salida */}
          {onExit && (
            <button
              type="button"
              className="btn btn-outline-success ms-2"
              onClick={onExit}
            >
              Salir
            </button>
          )}

          {/* Mensaje estético */}
          {mensaje && (
            <div
              className={`alert mt-3 ${tipoMensaje === "success" ? "alert-success" : "alert-danger"
                }`}
              role="alert"
            >
              {mensaje}
            </div>
          )}
        </form>
      </div>
    </div>
  );
}
