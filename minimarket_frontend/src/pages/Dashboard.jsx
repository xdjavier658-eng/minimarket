

import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { fetchWithAuth } from "../api/api";
import NavbarAdmin from "../components/NavbarAdmin";
import "../assets/css/dashboard.css";

export default function Dashboard() {
  const [resumen, setResumen] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const user = JSON.parse(localStorage.getItem("user"));
  const role = user?.roles[0];

  useEffect(() => {
    cargarResumen();
  }, []);

  const cargarResumen = async () => {
    try {
      setLoading(true);
      const data = await fetchWithAuth("/dashboard/resumen");
      setResumen(data);
      setError(null);
    } catch (err) {
      console.error("Error al cargar dashboard:", err);
      setError(err.message);

      // Datos de ejemplo para desarrollo
      setResumen({
        ventasHoy: 1250.50,
        cantidadVentasHoy: 15,
        totalProductos: 245,
        productosConStock: 198,
        productosStockBajo: 32,
        productosAgotados: 15,
        usuariosActivos: 8,
        productosStockBajoList: [
          { id: 1, nombre: "Arroz", stock: 3, stockMinimo: 10, estado: "STOCK BAJO", categoria: "Abarrotes", precio: 3.50 },
          { id: 2, nombre: "Aceite", stock: 2, stockMinimo: 5, estado: "STOCK BAJO", categoria: "Abarrotes", precio: 8.90 },
          { id: 3, nombre: "Leche", stock: 0, stockMinimo: 8, estado: "AGOTADO", categoria: "Lácteos", precio: 2.30 },
        ]
      });
    } finally {
      setLoading(false);
    }
  };

  const getRoleIcon = () => {
    switch (role) {
      case "ADMIN": return "👑";
      case "VENDEDOR": return "💰";
      case "ALMACENERO": return "📦";
      default: return "👤";
    }
  };


  const formatCurrency = (value) => {
    return new Intl.NumberFormat("es-PE", {
      style: "currency",
      currency: "PEN",
      minimumFractionDigits: 2,
    }).format(value || 0);
  };

  const formatNumber = (value) => {
    return new Intl.NumberFormat("es-PE").format(value || 0);
  };

  if (loading) {
    return (
      <>
        <NavbarAdmin />
        <div className="container mt-5 text-center">
          <div className="spinner-border text-success" role="status">
            <span className="visually-hidden">Cargando...</span>
          </div>
          <p className="mt-2 text-muted">Cargando dashboard...</p>
        </div>
      </>
    );
  }

  return (
    <>
      
      <div className="container-fluid px-2 px-md-4">
        {/* Header de bienvenida con estilo verde */}
        <div className="row mb-3 mb-md-4">
          <div className="col-12">
            <div className="bg-success text-white rounded-3 p-3 p-md-4 mt-3 shadow dashboard-welcome">
              <div className="d-flex align-items-center flex-column flex-sm-row text-center text-sm-start">
                <span className="display-1 me-0 me-sm-3 mb-2 mb-sm-0 d-none d-sm-block">{getRoleIcon()}</span>
                <div>
                  <h1 className="display-6 mb-1" style={{ fontSize: 'clamp(1.1rem, 4vw, 2rem)' }}>
                    ¡Bienvenido, {user?.username || "Usuario"}!
                  </h1>
                  <p className="lead mb-0 opacity-90" style={{ fontSize: 'clamp(0.75rem, 2.5vw, 1.1rem)' }}>
                    <i className="bi bi-person-badge me-1 me-md-2"></i>
                    Rol: <strong>{role}</strong>
                    <span className="d-none d-md-inline"> | </span>
                    <br className="d-md-none" />
                    <i className="bi bi-calendar3 ms-0 ms-md-3 me-1 me-md-2"></i>
                    {new Date().toLocaleDateString('es-PE', {
                      weekday: 'long',
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric'
                    })}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {error && (
          <div className="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
            <i className="bi bi-exclamation-triangle me-2"></i>
            {error}
            <button type="button" className="btn-close" onClick={() => setError(null)}></button>
          </div>
        )}

        {/* Tarjetas de estadísticas  */}
        {role === "ADMIN" && resumen && (
          <>
            <div className="row g-4 mb-4">
              {/* Tarjeta 1: Ventas Hoy */}
              <div className="col-6 col-xl-3">
                <div className="card stat-card shadow h-100">
                  <div className="card-body">
                    <div className="d-flex align-items-center mb-3">
                      <div className="stat-icon-wrapper bg-success-soft me-3">
                        <i className="bi bi-cash-stack text-success fs-3"></i>
                      </div>
                      <div>
                        <span className="stat-label m-3 text-muted text-uppercase small fw-bold">
                          Ventas Hoy
                        </span>
                        <h3 className="stat-value mb-0">
                          {formatCurrency(resumen.ventasHoy)}
                        </h3>
                      </div>
                    </div>
                    <div className="d-flex justify-content-between align-items-center">
                      <span className="text-muted small">
                        <i className="bi bi-calculator me-1"></i>
                        {formatNumber(resumen.cantidadVentasHoy)} transacciones
                      </span>
                      <span className="stat-trend positive">
                        <i className="bi bi-arrow-up-short"></i>
                        +12% vs ayer
                      </span>
                    </div>
                    <div className="stat-progress mt-3">
                      <div className="progress" style={{ height: "4px" }}>
                        <div
                          className="progress-bar bg-success"
                          style={{ width: "75%" }}
                        ></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Tarjeta 2: Usuarios Activos */}
              <div className="col-6 col-xl-3">
                <div className="card stat-card shadow h-100">
                  <div className="card-body">
                    <div className="d-flex align-items-center mb-3">
                      <div className="stat-icon-wrapper bg-info-soft me-3">
                        <i className="bi bi-people text-info fs-3"></i>
                      </div>
                      <div>
                        <span className="stat-label m-3 text-muted text-uppercase small fw-bold">
                          Usuarios Activos
                        </span>
                        <h3 className="stat-value mb-0">
                          {formatNumber(resumen.usuariosActivos)}
                        </h3>
                      </div>
                    </div>
                    <div className="d-flex justify-content-between align-items-center">
                      <span className="text-muted small">
                        <i className="bi bi-person-plus me-1"></i>
                        {resumen.nuevosUsuariosMes || 0} nuevos este mes
                      </span>
                      <span className="stat-trend positive">
                        <i className="bi bi-arrow-up-short"></i>
                        +5%
                      </span>
                    </div>
                    <div className="stat-progress mt-3">
                      <div className="progress" style={{ height: "4px" }}>
                        <div
                          className="progress-bar bg-info"
                          style={{ width: "60%" }}
                        ></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Tarjeta 3: Total Productos */}
              <div className="col-6 col-xl-3">
                <Link to="/admin/ProductRegister" className="text-decoration-none">
                  <div className="card stat-card shadow h-100 hover-card">
                    <div className="card-body">
                      <div className="d-flex align-items-center mb-3">
                        <div className="stat-icon-wrapper bg-primary-soft me-3">
                          <i className="bi bi-box-seam text-primary fs-3"></i>
                        </div>
                        <div>
                          <span className="stat-label m-3 text-muted text-uppercase small fw-bold">
                            Total Productos
                          </span>
                          <h3 className="stat-value mb-0 text-dark">
                            {formatNumber(resumen.totalProductos)}
                          </h3>
                        </div>
                      </div>
                      <div className="d-flex justify-content-between align-items-center">
                        <span className="text-muted small">
                          <i className="bi bi-check-circle me-1 text-success"></i>
                          {formatNumber(resumen.productosConStock)} con stock
                        </span>
                        <span className="stat-trend neutral">
                          <i className="bi bi-gear-fill me-1"></i>
                          Gestionar
                        </span>
                      </div>
                      <div className="stat-progress mt-3">
                        <div className="progress" style={{ height: "4px" }}>
                          <div
                            className="progress-bar bg-primary"
                            style={{ width: "80%" }}
                          ></div>
                        </div>
                      </div>
                    </div>
                  </div>
                </Link>
              </div>

              {/* Tarjeta 4: Stock Crítico */}
              <div className="col-6 col-xl-3">
                <div className="card stat-card shadow h-100">
                  <div className="card-body">
                    <div className="d-flex align-items-center mb-3">
                      <div className="stat-icon-wrapper bg-warning-soft me-3">
                        <i className="bi bi-exclamation-triangle text-warning fs-3"></i>
                      </div>
                      <div>
                        <span className="stat-label m-3 text-muted text-uppercase small fw-bold">
                          Stock Crítico
                        </span>
                        <div className="d-flex align-items-baseline">
                          <h3 className="stat-value mb-0 me-2">
                            {formatNumber(resumen.productosStockBajo)}
                          </h3>
                          <span className="text-muted small">bajo</span>
                          <h3 className="stat-value mb-0 ms-3 me-2 text-danger">
                            {formatNumber(resumen.productosAgotados)}
                          </h3>
                          <span className="text-muted small">agotado</span>
                        </div>
                      </div>
                    </div>
                    <div className="d-flex justify-content-between align-items-center">
                      <span className="text-muted small">
                        <i className="bi bi-clock-history me-1"></i>
                        Requieren atención
                      </span>
                      <span className="stat-trend negative">
                        <i className="bi bi-arrow-down-short"></i>
                        -3% vs ayer
                      </span>
                    </div>
                    <div className="stat-progress mt-3">
                      <div className="progress" style={{ height: "4px" }}>
                        <div
                          className="progress-bar bg-warning"
                          style={{ width: "45%" }}
                        ></div>
                        <div
                          className="progress-bar bg-danger"
                          style={{ width: "15%" }}
                        ></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Tabla de productos con problemas (mejorada) */}
            {resumen.productosStockBajoList?.length > 0 && (
              <div className="row mb-5">
                <div className="col-12">
                  <div className="card shadow">
                    <div className="card-header bg-white py-3">
                      <div className="d-flex align-items-center justify-content-between">
                        <h5 className="mb-0 text-success">
                          <i className="bi bi-exclamation-triangle me-2"></i>
                          Productos que requieren atención
                        </h5>
                        <span className="badge bg-warning text-dark px-3 py-2">
                          {resumen.productosStockBajoList.length} productos
                        </span>
                      </div>
                    </div>
                    <div className="card-body p-0">
                      <div className="table-responsive">
                        <table className="table table-hover align-middle mb-0">
                          <thead className="bg-light">
                            <tr>
                              <th className="py-3 ps-4">Producto</th>
                              <th className="py-3">Categoría</th>
                              <th className="py-3 text-end">Precio</th>
                              <th className="py-3 text-center">Stock Actual</th>
                              <th className="py-3 text-center">Stock Mínimo</th>
                              <th className="py-3 text-center">Estado</th>
                              <th className="py-3 text-center pe-4">Acciones</th>
                            </tr>
                          </thead>
                          <tbody>
                            {resumen.productosStockBajoList.map((prod) => (
                              <tr key={prod.id} className="border-bottom">
                                <td className="ps-4">
                                  <div className="d-flex align-items-center">
                                    <div className="product-icon bg-success-soft rounded-circle p-2 me-3">
                                      <i className="bi bi-box text-success"></i>
                                    </div>
                                    <div>
                                      <strong>{prod.nombre}</strong>
                                      <div className="text-muted small">ID: #{prod.id}</div>
                                    </div>
                                  </div>
                                </td>
                                <td>
                                  <span className="badge bg-light text-dark px-3 py-2">
                                    {prod.categoria || "Sin categoría"}
                                  </span>
                                </td>
                                <td className="text-end fw-bold">
                                  {formatCurrency(prod.precio)}
                                </td>
                                <td className="text-center">
                                  <span className={`stock-badge ${prod.estado === 'AGOTADO' ? 'stock-agotado' :
                                      prod.estado === 'STOCK BAJO' ? 'stock-bajo' : 'stock-normal'
                                    }`}>
                                    {prod.stock} uds
                                  </span>
                                </td>
                                <td className="text-center fw-bold">
                                  {prod.stockMinimo} uds
                                </td>
                                <td className="text-center">
                                  {prod.estado === 'AGOTADO' ? (
                                    <span className="badge bg-danger px-3 py-2">
                                      <i className="bi bi-fire me-1"></i>
                                      Agotado
                                    </span>
                                  ) : prod.estado === 'STOCK BAJO' ? (
                                    <span className="badge bg-warning text-dark px-3 py-2">
                                      <i className="bi bi-exclamation-circle me-1"></i>
                                      Stock Bajo
                                    </span>
                                  ) : (
                                    <span className="badge bg-success px-3 py-2">
                                      <i className="bi bi-check-circle me-1"></i>
                                      Normal
                                    </span>
                                  )}
                                </td>
                                <td className="text-center pe-4">
                                  <Link
                                    to={`/admin/ProductRegister?id=${prod.id}`}
                                    className="btn btn-sm btn-outline-success me-2"
                                    title="Editar producto"
                                  >
                                    <i className="bi bi-pencil"></i>
                                  </Link>
                                  <button
                                    className="btn btn-sm btn-outline-warning"
                                    onClick={() => console.log("Reabastecer", prod.id)}
                                    title="Reabastecer"
                                  >
                                    <i className="bi bi-plus-circle"></i>
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </div>
                    <div className="card-footer bg-white py-3">
                      <div className="d-flex justify-content-end">
                        <Link to="/admin/reportes/inventario" className="btn btn-link text-success">
                          Ver todos los productos <i className="bi bi-arrow-right ms-1"></i>
                        </Link>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </>
        )}

        {/* Mensaje para otros roles */}
        {role !== "ADMIN" && (
          <div className="row">
            <div className="col-12">
              <div className="alert alert-success border-0 shadow-sm">
                <i className="bi bi-info-circle me-2"></i>
                Vista simplificada para rol <strong>{role}</strong>. Contacta al administrador para más funcionalidades.
              </div>
            </div>
          </div>
        )}

        {/* Acceso rápido - Todo con verde */}
        <div className="row mt-4">
          <div className="col-12">
            <h4 className="border-bottom pb-2 mb-3 text-success">
              <i className="bi bi-rocket-takeoff me-2"></i>
              Acceso Rápido
            </h4>
          </div>

          <div className="col-6 col-xl-3 mb-3 mb-md-4">
            <Link to="/admin/ProductRegister" className="text-decoration-none">
              <div className="card border-left-success shadow h-100 py-2 hover-card">
                <div className="card-body">
                  <div className="row no-gutters align-items-center">
                    <div className="col mr-2">
                      <div className="text-sm font-weight-bold text-success text-uppercase mb-1">
                        Registrar <br /> Producto
                      </div>
                      <div className="text-muted ms-4 small">Añadir nuevo producto al inventario</div>
                    </div>
                    <div className="col-auto">
                      <i className="bi bi-box-seam fs-1 text-success opacity-50"></i>
                    </div>
                  </div>
                </div>
              </div>
            </Link>
          </div>

          <div className="col-6 col-xl-3 mb-3 mb-md-4">
            <Link to="/admin/UserRegister" className="text-decoration-none">
              <div className="card border-left-success shadow h-100 py-2 hover-card">
                <div className="card-body">
                  <div className="row no-gutters align-items-center">
                    <div className="col mr-2">
                      <div className="text-sm font-weight-bold text-success text-uppercase mb-1">
                        Registrar <br /> Usuario
                      </div>
                      <div className="text-muted ms-4 small">Crear nuevo usuario del sistema</div>
                    </div>
                    <div className="col-auto">
                      <i className="bi bi-person-plus fs-1 text-success opacity-50"></i>
                    </div>
                  </div>
                </div>
              </div>
            </Link>
          </div>

          <div className="col-6 col-xl-3 mb-3 mb-md-4">
            <Link to="/admin/reportes" className="text-decoration-none">
              <div className="card border-left-success shadow h-100 py-2 hover-card">
                <div className="card-body">
                  <div className="row no-gutters align-items-center">
                    <div className="col mr-2">
                      <div className="text-sm font-weight-bold text-success text-uppercase mb-1">
                        Reportes
                      </div>
                      <div className="text-muted ms-4 small">Estadísticas y análisis de ventas</div>
                    </div>
                    <div className="col-auto">
                      <i className="bi bi-bar-chart-line fs-1 text-success opacity-50"></i>
                    </div>
                  </div>
                </div>
              </div>
            </Link>
          </div>

          <div className="col-6 col-xl-3 mb-3 mb-md-4">
            <Link to="/" className="text-decoration-none">
              <div className="card border-left-success shadow h-100 py-2 hover-card">
                <div className="card-body">
                  <div className="row no-gutters align-items-center">
                    <div className="col mr-2">
                      <div className="text-sm font-weight-bold text-success text-uppercase mb-1">
                        Tienda  <br /> Pública
                      </div>
                      <div className="text-muted  ms-4 small">Ver catálogo de productos</div>
                    </div>
                    <div className="col-auto">
                      <i className="bi bi-shop fs-1 text-success opacity-50"></i>
                    </div>
                  </div>
                </div>
              </div>
            </Link>
          </div>
        </div>
      </div>
    </>
  );
}