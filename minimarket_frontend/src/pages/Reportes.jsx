import { useState, useEffect, useCallback } from "react";
import { Link } from "react-router-dom";
import { fetchWithAuth } from "../api/api";
import NavbarAdmin from "../components/NavbarAdmin";
import "../assets/css/reportes.css";

export default function Reportes() {
  const [activeTab, setActiveTab] = useState("ventas");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Estados para los diferentes reportes
  const [reporteVentas, setReporteVentas] = useState([]);
  const [topProductos, setTopProductos] = useState([]);
  const [ventasPorVendedor, setVentasPorVendedor] = useState([]);
  const [metodosPago, setMetodosPago] = useState([]);
  const [inventario, setInventario] = useState(null);
  const [resumenHoy, setResumenHoy] = useState(null);
  
  // Estados para filtros
  const [fechaInicio, setFechaInicio] = useState(
    new Date(new Date().setDate(new Date().getDate() - 7)).toISOString().split('T')[0]
  );
  const [fechaFin, setFechaFin] = useState(
    new Date().toISOString().split('T')[0]
  );
  const [limite, setLimite] = useState(10);

  // Cargar datos según la pestaña activa
  useEffect(() => {
    switch(activeTab) {
      case "ventas":
        cargarReporteVentas();
        break;
      case "top-productos":
        cargarTopProductos();
        break;
      case "vendedores":
        cargarVentasPorVendedor();
        break;
      case "metodos-pago":
        cargarMetodosPago();
        break;
      case "inventario":
        cargarInventario();
        break;
      case "resumen":
        cargarResumenHoy();
        break;
      default:
        break;
    }
  }, [activeTab, fechaInicio, fechaFin, limite, cargarReporteVentas, cargarTopProductos, cargarVentasPorVendedor, cargarMetodosPago, cargarInventario, cargarResumenHoy]);

  // ========== FUNCIONES DE CARGA DE DATOS ==========

  const cargarReporteVentas = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const inicio = `${fechaInicio}T00:00:00`;
      const fin = `${fechaFin}T23:59:59`;
      const data = await fetchWithAuth(`/reportes/rango?inicio=${inicio}&fin=${fin}`);
      setReporteVentas(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [fechaInicio, fechaFin]);

  const cargarTopProductos = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const inicio = `${fechaInicio}T00:00:00`;
      const fin = `${fechaFin}T23:59:59`;
      const data = await fetchWithAuth(
        `/reportes/productos/top?inicio=${inicio}&fin=${fin}&limite=${limite}`
      );
      setTopProductos(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [fechaInicio, fechaFin, limite]);

  const cargarVentasPorVendedor = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const inicio = `${fechaInicio}T00:00:00`;
      const fin = `${fechaFin}T23:59:59`;
      const data = await fetchWithAuth(`/reportes/vendedores?inicio=${inicio}&fin=${fin}`);
      setVentasPorVendedor(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [fechaInicio, fechaFin]);

  const cargarMetodosPago = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const inicio = `${fechaInicio}T00:00:00`;
      const fin = `${fechaFin}T23:59:59`;
      const data = await fetchWithAuth(`/reportes/metodos-pago?inicio=${inicio}&fin=${fin}`);
      setMetodosPago(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [fechaInicio, fechaFin]);

  const cargarInventario = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchWithAuth("/reportes/inventario/estadisticas");
      setInventario(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  const cargarResumenHoy = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchWithAuth("/reportes/resumen/hoy");
      setResumenHoy(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  // ========== FUNCIONES DE EXPORTACIÓN ==========

  const exportarExcel = async (tipo) => {
    try {
      let url = "";
      switch(tipo) {
        case "semanal":
          url = "/reportes/exportar/semanal/excel";
          break;
        case "mensual":
          url = "/reportes/exportar/mensual/excel";
          break;
        case "rango": {
          const inicio = `${fechaInicio}T00:00:00`;
          const fin = `${fechaFin}T23:59:59`;
          url = `/reportes/exportar/rango/excel?inicio=${inicio}&fin=${fin}`;
          break;
        }
        case "top-productos": {
          const inicioP = `${fechaInicio}T00:00:00`;
          const finP = `${fechaFin}T23:59:59`;
          url = `/reportes/exportar/top-productos/excel?inicio=${inicioP}&fin=${finP}&limite=${limite}`;
          break;
        }
        case "inventario":
          url = "/reportes/exportar/inventario/excel";
          break;
        default:
          return;
      }

      const token = localStorage.getItem("token");
      const response = await fetch(`http://localhost:8080/api${url}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) throw new Error("Error al descargar");

      const blob = await response.blob();
      const downloadUrl = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = downloadUrl;
      a.download = `reporte_${tipo}_${new Date().toISOString().split('T')[0]}.xlsx`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(downloadUrl);
    } catch (err) {
      setError("Error al exportar: " + err.message);
    }
  };

  // ========== FORMATO DE MONEDA ==========

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

  const formatPercentage = (value) => {
    return new Intl.NumberFormat("es-PE", {
      style: "percent",
      minimumFractionDigits: 1,
      maximumFractionDigits: 1,
    }).format((value || 0) / 100);
  };

  // ========== RENDERIZADO ==========

  return (
    <>
      
      <div className="container-fluid px-4">
        {/* Header */}
        <div className="row mb-4">
          <div className="col-12">
            <div className="d-flex justify-content-between align-items-center mt-3">
              <h1 className="h2">
                <i className="bi bi-bar-chart-line me-2"></i>
                Reportes y Estadísticas
              </h1>
              <div className="btn-group">
                <button
                  className="btn btn-outline-success"
                  onClick={() => exportarExcel("semanal")}
                >
                  <i className="bi bi-file-earmark-excel me-2"></i>
                  Semanal
                </button>
                <button
                  className="btn btn-outline-success"
                  onClick={() => exportarExcel("mensual")}
                >
                  <i className="bi bi-file-earmark-excel me-2"></i>
                  Mensual
                </button>
              </div>
            </div>
            <hr />
          </div>
        </div>

        {/* Pestañas */}
        <div className="row mb-4">
          <div className="col-12">
            <ul className="nav nav-tabs">
              <li className="nav-item">
                <button
                  className={`nav-link ${activeTab === "ventas" ? "active" : ""}`}
                  onClick={() => setActiveTab("ventas")}
                >
                  <i className="bi bi-cash-stack me-2"></i>
                  Ventas
                </button>
              </li>
              <li className="nav-item">
                <button
                  className={`nav-link ${activeTab === "top-productos" ? "active" : ""}`}
                  onClick={() => setActiveTab("top-productos")}
                >
                  <i className="bi bi-box-seam me-2"></i>
                  Top Productos
                </button>
              </li>
              <li className="nav-item">
                <button
                  className={`nav-link ${activeTab === "vendedores" ? "active" : ""}`}
                  onClick={() => setActiveTab("vendedores")}
                >
                  <i className="bi bi-people me-2"></i>
                  Por Vendedor
                </button>
              </li>
              <li className="nav-item">
                <button
                  className={`nav-link ${activeTab === "metodos-pago" ? "active" : ""}`}
                  onClick={() => setActiveTab("metodos-pago")}
                >
                  <i className="bi bi-credit-card me-2"></i>
                  Métodos de Pago
                </button>
              </li>
              <li className="nav-item">
                <button
                  className={`nav-link ${activeTab === "inventario" ? "active" : ""}`}
                  onClick={() => setActiveTab("inventario")}
                >
                  <i className="bi bi-boxes me-2"></i>
                  Inventario
                </button>
              </li>
              <li className="nav-item">
                <button
                  className={`nav-link ${activeTab === "resumen" ? "active" : ""}`}
                  onClick={() => setActiveTab("resumen")}
                >
                  <i className="bi bi-pie-chart me-2"></i>
                  Resumen del Día
                </button>
              </li>
            </ul>
          </div>
        </div>

        {/* Filtros (excepto para inventario y resumen) */}
        {activeTab !== "inventario" && activeTab !== "resumen" && (
          <div className="row mb-4">
            <div className="col-md-12">
              <div className="card shadow-sm">
                <div className="card-body">
                  <div className="row g-3 align-items-end">
                    <div className="col-md-3">
                      <label className="form-label">Fecha Inicio</label>
                      <input
                        type="date"
                        className="form-control"
                        value={fechaInicio}
                        onChange={(e) => setFechaInicio(e.target.value)}
                      />
                    </div>
                    <div className="col-md-3">
                      <label className="form-label">Fecha Fin</label>
                      <input
                        type="date"
                        className="form-control"
                        value={fechaFin}
                        onChange={(e) => setFechaFin(e.target.value)}
                      />
                    </div>
                    {activeTab === "top-productos" && (
                      <div className="col-md-2">
                        <label className="form-label">Límite</label>
                        <select
                          className="form-select"
                          value={limite}
                          onChange={(e) => setLimite(parseInt(e.target.value))}
                        >
                          <option value="5">5</option>
                          <option value="10">10</option>
                          <option value="20">20</option>
                          <option value="50">50</option>
                        </select>
                      </div>
                    )}
                    <div className="col-md-2">
                      <button
                        className="btn btn-success w-100"
                        onClick={() => {
                          if (activeTab === "ventas") cargarReporteVentas();
                          if (activeTab === "top-productos") cargarTopProductos();
                          if (activeTab === "vendedores") cargarVentasPorVendedor();
                          if (activeTab === "metodos-pago") cargarMetodosPago();
                        }}
                      >
                        <i className="bi bi-search me-2"></i>
                        Filtrar
                      </button>
                    </div>
                    <div className="col-md-2">
                      <button
                        className="btn btn-outline-success w-100"
                        onClick={() => exportarExcel("rango")}
                      >
                        <i className="bi bi-file-earmark-excel me-2"></i>
                        Exportar
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Contenido según pestaña */}
        <div className="row">
          <div className="col-12">
            {loading && (
              <div className="text-center py-5">
                <div className="spinner-border text-success" role="status">
                  <span className="visually-hidden">Cargando...</span>
                </div>
                <p className="mt-2">Cargando reporte...</p>
              </div>
            )}

            {error && (
              <div className="alert alert-danger">
                <i className="bi bi-exclamation-triangle me-2"></i>
                {error}
              </div>
            )}

            {!loading && !error && activeTab === "ventas" && (
              <div className="card shadow">
                <div className="card-header bg-success text-white">
                  <h5 className="mb-0">
                    <i className="bi bi-calendar-range me-2"></i>
                    Ventas del {new Date(fechaInicio).toLocaleDateString()} al {new Date(fechaFin).toLocaleDateString()}
                  </h5>
                </div>
                <div className="card-body">
                  <div className="table-responsive">
                    <table className="table table-hover">
                      <thead className="table-light">
                        <tr>
                          <th>Fecha</th>
                          <th className="text-end">Cantidad Ventas</th>
                          <th className="text-end">Total Ventas</th>
                          <th className="text-end">Ticket Promedio</th>
                        </tr>
                      </thead>
                      <tbody>
                        {reporteVentas.map((venta, index) => (
                          <tr key={index}>
                            <td>{new Date(venta.fecha).toLocaleDateString()}</td>
                            <td className="text-end">{formatNumber(venta.cantidadVentas)}</td>
                            <td className="text-end">{formatCurrency(venta.totalVentas)}</td>
                            <td className="text-end">
                              {formatCurrency(venta.cantidadVentas > 0 
                                ? venta.totalVentas / venta.cantidadVentas 
                                : 0)}
                            </td>
                          </tr>
                        ))}
                        {reporteVentas.length === 0 && (
                          <tr>
                            <td colSpan="4" className="text-center py-4 text-muted">
                              No hay datos para el período seleccionado
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

            {!loading && !error && activeTab === "top-productos" && (
              <div className="card shadow">
                <div className="card-header bg-success text-white">
                  <h5 className="mb-0">
                    <i className="bi bi-trophy me-2"></i>
                    Top {limite} Productos Más Vendidos
                  </h5>
                </div>
                <div className="card-body">
                  <div className="table-responsive">
                    <table className="table table-hover">
                      <thead className="table-light">
                        <tr>
                          <th>#</th>
                          <th>Producto</th>
                          <th className="text-end">Cantidad Vendida</th>
                          <th className="text-end">Total Ventas</th>
                          <th className="text-end">Precio Promedio</th>
                        </tr>
                      </thead>
                      <tbody>
                        {topProductos.map((producto, index) => (
                          <tr key={producto.productoId}>
                            <td>{index + 1}</td>
                            <td>
                              <strong>{producto.nombre}</strong>
                            </td>
                            <td className="text-end">{formatNumber(producto.cantidadVendida)}</td>
                            <td className="text-end">{formatCurrency(producto.totalVentas)}</td>
                            <td className="text-end">
                              {formatCurrency(producto.cantidadVendida > 0 
                                ? producto.totalVentas / producto.cantidadVendida 
                                : 0)}
                            </td>
                          </tr>
                        ))}
                        {topProductos.length === 0 && (
                          <tr>
                            <td colSpan="5" className="text-center py-4 text-muted">
                              No hay datos para el período seleccionado
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

            {!loading && !error && activeTab === "vendedores" && (
              <div className="card shadow">
                <div className="card-header bg-success text-white">
                  <h5 className="mb-0">
                    <i className="bi bi-people me-2"></i>
                    Ventas por Vendedor
                  </h5>
                </div>
                <div className="card-body">
                  <div className="table-responsive">
                    <table className="table table-hover">
                      <thead className="table-light">
                        <tr>
                          <th>Vendedor</th>
                          <th className="text-end">Cantidad Ventas</th>
                          <th className="text-end">Total Ventas</th>
                          <th className="text-end">Ticket Promedio</th>
                        </tr>
                      </thead>
                      <tbody>
                        {ventasPorVendedor.map((vendedor, index) => (
                          <tr key={index}>
                            <td>
                              <i className="bi bi-person-circle me-2"></i>
                              {vendedor.vendedor}
                            </td>
                            <td className="text-end">{formatNumber(vendedor.cantidadVentas)}</td>
                            <td className="text-end">{formatCurrency(vendedor.totalVentas)}</td>
                            <td className="text-end">{formatCurrency(vendedor.promedioVenta || 0)}</td>
                          </tr>
                        ))}
                        {ventasPorVendedor.length === 0 && (
                          <tr>
                            <td colSpan="4" className="text-center py-4 text-muted">
                              No hay ventas en el período seleccionado
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}

            {!loading && !error && activeTab === "metodos-pago" && (
              <div className="row">
                <div className="col-md-6 mb-4">
                  <div className="card shadow h-100">
                    <div className="card-header bg-success text-white">
                      <h5 className="mb-0">
                        <i className="bi bi-pie-chart me-2"></i>
                        Distribución por Método de Pago
                      </h5>
                    </div>
                    <div className="card-body">
                      {metodosPago.map((metodo, index) => (
                        <div key={index} className="mb-3">
                          <div className="d-flex justify-content-between mb-1">
                            <span>
                              <strong>{metodo.metodoPago}</strong>
                            </span>
                            <span>
                              {formatCurrency(metodo.totalMonto)} 
                              {metodo.porcentaje && (
                                <span className="text-muted ms-2">
                                  ({formatPercentage(metodo.porcentaje)})
                                </span>
                              )}
                            </span>
                          </div>
                          <div className="progress" style={{ height: "25px" }}>
                            <div
                              className={`progress-bar ${
                                metodo.metodoPago === "EFECTIVO" ? "bg-success" :
                                metodo.metodoPago === "YAPE" ? "bg-info" :
                                metodo.metodoPago === "PLIN" ? "bg-warning" : "bg-secondary"
                              }`}
                              style={{ width: `${metodo.porcentaje || 0}%` }}
                            >
                              {metodo.cantidadPagos} transacciones
                            </div>
                          </div>
                        </div>
                      ))}
                      {metodosPago.length === 0 && (
                        <p className="text-center text-muted py-4">
                          No hay pagos en el período seleccionado
                        </p>
                      )}
                    </div>
                  </div>
                </div>
                <div className="col-md-6 mb-4">
                  <div className="card shadow h-100">
                    <div className="card-header bg-success text-white">
                      <h5 className="mb-0">
                        <i className="bi bi-table me-2"></i>
                        Detalle por Método de Pago
                      </h5>
                    </div>
                    <div className="card-body">
                      <div className="table-responsive">
                        <table className="table">
                          <thead>
                            <tr>
                              <th>Método</th>
                              <th className="text-end">Transacciones</th>
                              <th className="text-end">Total</th>
                            </tr>
                          </thead>
                          <tbody>
                            {metodosPago.map((metodo, index) => (
                              <tr key={index}>
                                <td>{metodo.metodoPago}</td>
                                <td className="text-end">{formatNumber(metodo.cantidadPagos)}</td>
                                <td className="text-end">{formatCurrency(metodo.totalMonto)}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {!loading && !error && activeTab === "inventario" && inventario && (
              <div className="row">
                <div className="col-md-3 mb-4">
                  <div className="card bg-primary text-white shadow">
                    <div className="card-body">
                      <h6 className="text-white-50">Total Productos</h6>
                      <h2 className="mb-0">{formatNumber(inventario.totalProductos)}</h2>
                    </div>
                  </div>
                </div>
                <div className="col-md-3 mb-4">
                  <div className="card bg-success text-white shadow">
                    <div className="card-body">
                      <h6 className="text-white-50">Con Stock</h6>
                      <h2 className="mb-0">{formatNumber(inventario.productosConStock)}</h2>
                      <small>{formatPercentage(inventario.porcentajeConStock)}</small>
                    </div>
                  </div>
                </div>
                <div className="col-md-3 mb-4">
                  <div className="card bg-warning text-white shadow">
                    <div className="card-body">
                      <h6 className="text-white-50">Stock Bajo</h6>
                      <h2 className="mb-0">{formatNumber(inventario.productosStockBajo)}</h2>
                      <small>{formatPercentage(inventario.porcentajeStockBajo)}</small>
                    </div>
                  </div>
                </div>
                <div className="col-md-3 mb-4">
                  <div className="card bg-danger text-white shadow">
                    <div className="card-body">
                      <h6 className="text-white-50">Agotados</h6>
                      <h2 className="mb-0">{formatNumber(inventario.productosAgotados)}</h2>
                      <small>{formatPercentage(inventario.porcentajeAgotados)}</small>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {!loading && !error && activeTab === "resumen" && resumenHoy && (
              <div className="row">
                <div className="col-md-6 mb-4">
                  <div className="card shadow h-100">
                    <div className="card-header bg-success text-white">
                      <h5 className="mb-0">
                        <i className="bi bi-calendar-day me-2"></i>
                        Resumen del Día
                      </h5>
                    </div>
                    <div className="card-body">
                      <table className="table table-borderless">
                        <tbody>
                          <tr>
                            <td>Total Ventas:</td>
                            <td className="text-end fw-bold fs-5 text-success">
                              {formatCurrency(resumenHoy.totalVentas)}
                            </td>
                          </tr>
                          <tr>
                            <td>Cantidad de Ventas:</td>
                            <td className="text-end fw-bold">{formatNumber(resumenHoy.cantidadVentas)}</td>
                          </tr>
                          <tr>
                            <td>Ticket Promedio:</td>
                            <td className="text-end fw-bold">{formatCurrency(resumenHoy.ticketPromedio)}</td>
                          </tr>
                          <tr>
                            <td>Producto Más Vendido:</td>
                            <td className="text-end fw-bold">{resumenHoy.productoMasVendido || "N/A"}</td>
                          </tr>
                          <tr>
                            <td>Unidades Vendidas:</td>
                            <td className="text-end fw-bold">{formatNumber(resumenHoy.cantidadProductosVendidos || 0)}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
                <div className="col-md-6 mb-4">
                  <div className="card shadow h-100">
                    <div className="card-header bg-success text-white">
                      <h5 className="mb-0">
                        <i className="bi bi-clock-history me-2"></i>
                        Acciones Rápidas
                      </h5>
                    </div>
                    <div className="card-body d-flex flex-column gap-3">
                      <button
                        className="btn btn-outline-success w-100 py-3"
                        onClick={() => exportarExcel("semanal")}
                      >
                        <i className="bi bi-file-earmark-excel me-2"></i>
                        Exportar Reporte Semanal
                      </button>
                      <button
                        className="btn btn-outline-success w-100 py-3"
                        onClick={() => exportarExcel("mensual")}
                      >
                        <i className="bi bi-file-earmark-excel me-2"></i>
                        Exportar Reporte Mensual
                      </button>
                      <button
                        className="btn btn-outline-success w-100 py-3"
                        onClick={() => exportarExcel("inventario")}
                      >
                        <i className="bi bi-file-earmark-excel me-2"></i>
                        Exportar Inventario
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      <style jsx>{`
        .nav-tabs .nav-link {
          color: #495057;
          font-weight: 500;
        }
        .nav-tabs .nav-link.active {
          color: #28a745;
          font-weight: 600;
        }
        .progress {
          border-radius: 5px;
        }
        .progress-bar {
          font-size: 0.85rem;
          line-height: 25px;
        }
      `}</style>
    </>
  );
}