import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import ProductCard from "../components/ProductCard";
import { useCart } from "../hooks/useCart";
import { useFiltradoProductos } from "../hooks/useFiltradoProductos";
import MetodoPagoModal from '../components/MetodoPagoModal';

const categorias = [
  "Todos",
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

export default function PublicCatalog() {
  const [productos, setProductos] = useState([]);
  const navigate = useNavigate();
  const { carrito, agregarProducto, disminuirProducto, eliminarProducto, limpiarCarrito, total } = useCart();

  // Usamos el hook de filtrado
  const { productosFiltrados, categoriaSeleccionada, setCategoriaSeleccionada } = useFiltradoProductos(productos);

  const [showModal, setShowModal] = useState(false);
  const [cartOpen, setCartOpen] = useState(false);

  useEffect(() => {
    fetch("/api/productos")
      .then((res) => res.json())
      .then((data) => setProductos(data))
      .catch((err) => console.error("Error al cargar productos:", err));
  }, []);

  return (
    <div className="container py-3 py-md-4">
      {/* NAVBAR */}
      <nav className="navbar navbar-light shadow-sm rounded mb-3 mb-md-4">
        <div className="container-fluid catalog-navbar">
          <span className="navbar-brand mb-0 h1 text-success">MiniMarket</span>
          <div className="catalog-navbar-actions">
            {/* BOTÓN VOLVER PARA ADMIN */}
            {JSON.parse(localStorage.getItem("user"))?.roles?.includes("ADMIN") && (
              <button 
                className="btn btn-warning fw-bold shadow-sm btn-sm"
                onClick={() => navigate("/admin")}
              >
                <i className="bi bi-speedometer2 me-1"></i>
                <span className="d-none d-sm-inline">Volver al Sistema</span>
                <span className="d-sm-none">Admin</span>
              </button>
            )}
            <button 
              className="btn btn-outline-dark btn-sm"
              onClick={() => navigate("/admin/login")}
            >
              <i className="bi bi-person-lock me-1"></i>
              <span className="d-none d-sm-inline">Administrador</span>
            </button>
            <span className="text-success fw-bold d-none d-md-inline">
              {JSON.parse(localStorage.getItem("user"))?.username || "Invitado"}
            </span>
            <button 
              className="btn bg-success text-white position-relative btn-sm d-none d-lg-inline-block"
            >
              Carrito
              <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                {carrito.length}
              </span>
            </button>
          </div>
        </div>
      </nav>

      <div className="row">
        {/* COLUMNA PRODUCTOS */}
        <div className="col-lg-8">
          {/* Botones de categoría */}
          <div className="mb-3 category-filters">
            {categorias.map((cat) => (
              <button
                key={cat}
                className={`btn btn-sm me-1 mb-2 ${categoriaSeleccionada === cat ? "btn-success" : "btn-outline-success"
                  }`}
                onClick={() => setCategoriaSeleccionada(cat)}
              >
                {cat}
              </button>
            ))}
          </div>

          <div className="d-flex justify-content-between align-items-center mb-3">
            <h3 className="h4 h3-md mb-0">Productos Disponibles</h3>
          </div>

          <div className="row row-cols-2 row-cols-md-2 row-cols-lg-3 g-3 g-md-4 catalog-products">
            {productosFiltrados.length > 0 ? (
              productosFiltrados.map((producto) => (
                <ProductCard key={producto.id} producto={producto} onAdd={agregarProducto} />
              ))
            ) : (
              <div className="alert alert-info w-100 text-center">
                No hay productos disponibles en esta categoría.
              </div>
            )}
          </div>
        </div>

        {/* COLUMNA CARRITO — Desktop */}
        <div className={`col-lg-4 d-none d-lg-block`}>
          <div className="card shadow-sm sticky-top" style={{ top: '1rem' }}>
            <div className="card-header bg-success text-white">
              <h5 className="mb-0">🛒 Mi Carrito</h5>
            </div>

            <div className="card-body" style={{ maxHeight: '50vh', overflowY: 'auto' }}>
              {carrito.length === 0 ? (
                <p className="text-muted text-center">El carrito está vacío</p>
              ) : (
                carrito.map((item) => (
                  <div key={item.id} className="d-flex justify-content-between align-items-center mb-2 p-2 border rounded">
                    <div className="d-flex align-items-center">
                      <img src={`/img/${item.imagen}`} alt={item.nombre} width={40} className="me-2 rounded" />
                      <div>
                        <strong style={{ fontSize: '0.85rem' }}>{item.nombre}</strong>
                        <br />
                        <small>S/ {item.precio.toFixed(2)} c/u</small>
                        <br />
                        <div>
                          <button
                            className="btn btn-sm btn-outline-secondary me-1"
                            onClick={() => disminuirProducto(item.id)}
                            disabled={item.cantidad === 1}
                          >
                            -
                          </button>
                          <span>{item.cantidad}</span>
                          <button className="btn btn-sm btn-outline-secondary ms-1" onClick={() => agregarProducto(item)}>
                            +
                          </button>
                        </div>
                      </div>
                    </div>

                    <div className="text-success fw-bold" style={{ fontSize: '0.85rem' }}>S/ {(item.precio * item.cantidad).toFixed(2)}</div>

                    <button className="btn btn-danger btn-sm ms-2" onClick={() => eliminarProducto(item.id)}>
                      🗑️
                    </button>
                  </div>
                ))
              )}
            </div>

            <div className="card-footer">
              <div className="d-flex justify-content-between mb-2">
                <strong>Total:</strong>
                <strong className="text-success">S/ {total.toFixed(2)}</strong>
              </div>

              <button
                className="btn btn-success w-100 mb-2"
                disabled={carrito.length === 0}
                onClick={() => setShowModal(true)}
              >
                Finalizar Compra
              </button>

              <button className="btn btn-outline-danger w-100" disabled={carrito.length === 0} onClick={limpiarCarrito}>
                Vaciar Carrito
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* CARRITO MÓVIL — Overlay */}
      {cartOpen && <div className="cart-overlay open" onClick={() => setCartOpen(false)}></div>}
      
      <div className={`cart-sidebar bg-white ${cartOpen ? 'open' : ''}`}>
        <div className="card border-0 h-100 d-flex flex-column">
          <div className="card-header bg-success text-white d-flex justify-content-between align-items-center">
            <h5 className="mb-0">🛒 Mi Carrito</h5>
            <button className="btn btn-sm btn-light" onClick={() => setCartOpen(false)}>
              <i className="bi bi-x-lg"></i>
            </button>
          </div>

          <div className="card-body flex-grow-1" style={{ overflowY: 'auto' }}>
            {carrito.length === 0 ? (
              <p className="text-muted text-center mt-4">El carrito está vacío</p>
            ) : (
              carrito.map((item) => (
                <div key={item.id} className="d-flex justify-content-between align-items-center mb-2 p-2 border rounded">
                  <div className="d-flex align-items-center" style={{ minWidth: 0, flex: 1 }}>
                    <img src={`/img/${item.imagen}`} alt={item.nombre} width={35} className="me-2 rounded flex-shrink-0" />
                    <div style={{ minWidth: 0 }}>
                      <strong style={{ fontSize: '0.8rem', display: 'block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{item.nombre}</strong>
                      <small>S/ {item.precio.toFixed(2)}</small>
                      <div className="mt-1">
                        <button
                          className="btn btn-sm btn-outline-secondary me-1"
                          onClick={() => disminuirProducto(item.id)}
                          disabled={item.cantidad === 1}
                          style={{ padding: '0 6px', fontSize: '0.75rem' }}
                        >
                          -
                        </button>
                        <span style={{ fontSize: '0.85rem' }}>{item.cantidad}</span>
                        <button 
                          className="btn btn-sm btn-outline-secondary ms-1" 
                          onClick={() => agregarProducto(item)}
                          style={{ padding: '0 6px', fontSize: '0.75rem' }}
                        >
                          +
                        </button>
                      </div>
                    </div>
                  </div>

                  <div className="text-end ms-2 flex-shrink-0">
                    <div className="text-success fw-bold" style={{ fontSize: '0.8rem' }}>S/ {(item.precio * item.cantidad).toFixed(2)}</div>
                    <button className="btn btn-danger btn-sm mt-1" onClick={() => eliminarProducto(item.id)} style={{ padding: '2px 6px', fontSize: '0.7rem' }}>
                      🗑️
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>

          <div className="card-footer">
            <div className="d-flex justify-content-between mb-2">
              <strong>Total:</strong>
              <strong className="text-success">S/ {total.toFixed(2)}</strong>
            </div>

            <button
              className="btn btn-success w-100 mb-2"
              disabled={carrito.length === 0}
              onClick={() => { setShowModal(true); setCartOpen(false); }}
            >
              Finalizar Compra
            </button>

            <button className="btn btn-outline-danger w-100" disabled={carrito.length === 0} onClick={limpiarCarrito}>
              Vaciar Carrito
            </button>
          </div>
        </div>
      </div>

      {/* Botón flotante carrito — solo móvil */}
      <button
        className="cart-toggle-btn btn bg-success text-white d-lg-none"
        onClick={() => setCartOpen(true)}
      >
        🛒
        {carrito.length > 0 && (
          <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style={{ fontSize: '0.65rem' }}>
            {carrito.length}
          </span>
        )}
      </button>

      <MetodoPagoModal
        show={showModal}
        onClose={() => setShowModal(false)}
      />
    </div>
  );
}
