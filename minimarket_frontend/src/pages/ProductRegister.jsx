// src/pages/ProductRegister.jsx
import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useProductos } from '../hooks/useProductos';
import NavbarAdmin from '../components/NavbarAdmin';
import { uploadImage } from '../api/api';
import '../assets/css/productRegister.css';

const ProductRegister = () => {
  const { productos, loading, error, crearProducto, listarProductos, actualizarProducto, eliminarProducto, getProductoById } = useProductos();
  const location = useLocation();
  const navigate = useNavigate();
  
  const [isEditing, setIsEditing] = useState(false);
  const [editId, setEditId] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);
  
  const [formData, setFormData] = useState({
    nombre: '',
    descripcion: '',
    precio: '',
    stock: '',
    stockMinimo: '5',
    categoria: '',
    imagen: ''
  });
  
  const [formErrors, setFormErrors] = useState({});
  const [submitLoading, setSubmitLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  // Detectar modo edición por URL
  useEffect(() => {
    const cargarProductoParaEdicion = async (id) => {
      try {
        const prod = await getProductoById(id);
        if (prod) {
          setIsEditing(true);
          setEditId(id);
          setFormData({
              nombre: prod.nombre,
              descripcion: prod.descripcion || '',
              precio: prod.precio.toString(),
              stock: prod.stock.toString(),
              stockMinimo: prod.stockMinimo.toString(),
              categoria: prod.categoria || '',
              imagen: prod.imagen || ''
          });
        }
      } catch (err) {
        console.error("Error al cargar producto:", err);
      }
    };
    
    // Detectar ID en la URL para edición
    const query = new URLSearchParams(location.search);
    const id = query.get('id');
    
    if (id) {
      cargarProductoParaEdicion(id);
    }
  }, [location.search, getProductoById]);

  // ========== VALIDACIÓN CORREGIDA ==========
  const validateForm = () => {
    const errors = {};

    // Validación de nombre
    if (!formData.nombre.trim()) {
      errors.nombre = 'El nombre es requerido';
    } else if (formData.nombre.length < 3) {
      errors.nombre = 'El nombre debe tener al menos 3 caracteres';
    }

    // Validación de precio
    if (!formData.precio) {
      errors.precio = 'El precio es requerido';
    } else if (isNaN(formData.precio) || parseFloat(formData.precio) <= 0) {
      errors.precio = 'El precio debe ser un número mayor a 0';
    }

    // Validación de stock
    if (!formData.stock) {
      errors.stock = 'El stock es requerido';
    } else if (isNaN(formData.stock) || parseInt(formData.stock) < 0) {
      errors.stock = 'El stock debe ser un número válido';
    }

    // Validación de stock mínimo
    if (!formData.stockMinimo) {
      errors.stockMinimo = 'El stock mínimo es requerido';
    } else if (isNaN(formData.stockMinimo) || parseInt(formData.stockMinimo) < 0) {
      errors.stockMinimo = 'El stock mínimo debe ser un número válido';
    }

    // Validación de categoría
    if (!formData.categoria.trim()) {
      errors.categoria = 'La categoría es requerida';
    }

    // ✅ VALIDACIÓN CORREGIDA PARA IMAGEN
    // Ahora acepta: nombre de archivo (manzana.jpg), ruta relativa (img/manzana.jpg) o URL completa
    if (formData.imagen && formData.imagen.trim() !== '') {
      const imagenValue = formData.imagen.trim();
      
      // Patrones válidos:
      // - nombre.jpg (solo nombre de archivo)
      // - img/nombre.jpg (ruta relativa)
      // - https://ejemplo.com/imagen.jpg (URL completa)
      
      // Verificar que no tenga caracteres extraños
      const invalidChars = /[<>:"|?*]/;
      if (invalidChars.test(imagenValue)) {
        errors.imagen = 'El nombre de archivo contiene caracteres no válidos';
      }
      
      // Verificar que tenga extensión de imagen (opcional, pero recomendado)
      const hasImageExtension = /\.(jpg|jpeg|png|gif|webp|svg|bmp)$/i.test(imagenValue);
      if (!hasImageExtension && !imagenValue.startsWith('http')) {
        // Si no es URL y no tiene extensión de imagen, mostrar advertencia (no error)
        console.log('Advertencia: El archivo no tiene extensión de imagen común');
      }
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ 
      ...formData, 
      [name]: value 
    });
    
    // Limpiar error del campo cuando el usuario empieza a escribir
    if (formErrors[name]) {
      setFormErrors({
        ...formErrors,
        [name]: null
      });
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm("¿Estás seguro de eliminar este producto? (Se desactivará del sistema)")) {
        try {
            await eliminarProducto(id);
            setSuccessMessage("✅ Producto eliminado correctamente");
            setTimeout(() => setSuccessMessage(""), 3000);
        } catch (err) {
            console.error("Error al eliminar:", err);
            alert("Error al eliminar el producto");
        }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setSubmitLoading(true);
    setSuccessMessage('');
    
    try {
      let finalImagen = formData.imagen;

      // SUBIR IMAGEN SI HAY UNA SELECCIONADA
      if (selectedFile) {
        const uploadRes = await uploadImage(selectedFile);
        finalImagen = uploadRes.filename;
      }

      // Preparar datos para enviar al backend
      const productoData = {
        nombre: formData.nombre.trim(),
        descripcion: formData.descripcion.trim() || null,
        precio: parseFloat(formData.precio),
        stock: parseInt(formData.stock),
        stockMinimo: parseInt(formData.stockMinimo),
        categoria: formData.categoria.trim(),
        imagen: finalImagen
      };

      if (isEditing) {
        await actualizarProducto(editId, productoData);
        setSuccessMessage('✅ Producto actualizado exitosamente');
        // Regresar al modo creación después de 2 segundos
        setTimeout(() => {
            navigate('/admin/ProductRegister');
            setIsEditing(false);
            setEditId(null);
            setFormData({
                nombre: '', descripcion: '', precio: '', stock: '', stockMinimo: '5', categoria: '', imagen: ''
            });
        }, 2000);
      } else {
        await crearProducto(productoData);
        setSuccessMessage('✅ Producto creado exitosamente');
        
        // Limpiar formulario
        setFormData({
            nombre: '', descripcion: '', precio: '', stock: '', stockMinimo: '5', categoria: '', imagen: ''
        });
        setSelectedFile(null);
      }
      
      // Recargar lista de productos
      await listarProductos();
      
      // Ocultar mensaje después de 3 segundos
      setTimeout(() => {
        setSuccessMessage('');
      }, 3000);
      
    } catch (err) {
      console.error('Error al procesar producto:', err);
      alert('❌ Error: ' + (err.message || 'Error desconocido'));
    } finally {
      setSubmitLoading(false);
    }
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN',
      minimumFractionDigits: 2
    }).format(value || 0);
  };

  return (
    <>
      
      <div className="container-fluid px-4">
        {/* Header */}
        <div className="row mb-4">
          <div className="col-12">
            <div className="d-flex align-items-center mt-3">
              <div className="bg-success-soft rounded-circle p-3 me-3">
                <i className="bi bi-box-seam text-success fs-3"></i>
              </div>
              <div>
                <h1 className="h2 mb-1">Registro de Productos</h1>
                <p className="text-muted mb-0">
                  <i className="bi bi-info-circle me-2"></i>
                  Gestiona el inventario de tu minimarket
                </p>
              </div>
            </div>
            <hr className="mt-3" />
          </div>
        </div>

        <div className="row">
          {/* Formulario de registro */}
          <div className="col-lg-5 mb-4">
            <div className="card shadow border-0">
              <div className="card-header bg-success text-white py-3">
                <h5 className="mb-0">
                  <i className={`bi ${isEditing ? 'bi-pencil-square' : 'bi-plus-circle'} me-2`}></i>
                  {isEditing ? 'Editar Producto' : 'Nuevo Producto'}
                </h5>
              </div>
              <div className="card-body p-4">
                {successMessage && (
                  <div className="alert alert-success alert-dismissible fade show" role="alert">
                    <i className="bi bi-check-circle me-2"></i>
                    {successMessage}
                    <button 
                      type="button" 
                      className="btn-close" 
                      onClick={() => setSuccessMessage('')}
                    ></button>
                  </div>
                )}

                <form onSubmit={handleSubmit}>
                  {/* Nombre */}
                  <div className="mb-3">
                    <label className="form-label fw-bold">
                      Nombre del Producto <span className="text-danger">*</span>
                    </label>
                    <div className="input-group">
                      <span className="input-group-text bg-light">
                        <i className="bi bi-tag"></i>
                      </span>
                      <input
                        type="text"
                        className={`form-control ${formErrors.nombre ? 'is-invalid' : ''}`}
                        name="nombre"
                        value={formData.nombre}
                        onChange={handleChange}
                        placeholder="Ej: Arroz Extra 1kg"
                        maxLength="100"
                      />
                      {formErrors.nombre && (
                        <div className="invalid-feedback">{formErrors.nombre}</div>
                      )}
                    </div>
                  </div>

                  {/* Descripción */}
                  <div className="mb-3">
                    <label className="form-label fw-bold">Descripción</label>
                    <div className="input-group">
                      <span className="input-group-text bg-light">
                        <i className="bi bi-text-paragraph"></i>
                      </span>
                      <textarea
                        className="form-control"
                        name="descripcion"
                        value={formData.descripcion}
                        onChange={handleChange}
                        placeholder="Descripción detallada del producto"
                        rows="2"
                      />
                    </div>
                  </div>

                  {/* Precio y Categoría */}
                  <div className="row">
                    <div className="col-md-6 mb-3">
                      <label className="form-label fw-bold">
                        Precio (S/) <span className="text-danger">*</span>
                      </label>
                      <div className="input-group">
                        <span className="input-group-text bg-light">S/</span>
                        <input
                          type="number"
                          step="0.01"
                          min="0"
                          className={`form-control ${formErrors.precio ? 'is-invalid' : ''}`}
                          name="precio"
                          value={formData.precio}
                          onChange={handleChange}
                          placeholder="0.00"
                        />
                        {formErrors.precio && (
                          <div className="invalid-feedback">{formErrors.precio}</div>
                        )}
                      </div>
                    </div>

                    <div className="col-md-6 mb-3">
                      <label className="form-label fw-bold">
                        Categoría <span className="text-danger">*</span>
                      </label>
                      <div className="input-group">
                        <span className="input-group-text bg-light">
                          <i className="bi bi-folder"></i>
                        </span>
                        <input
                          type="text"
                          className={`form-control ${formErrors.categoria ? 'is-invalid' : ''}`}
                          name="categoria"
                          value={formData.categoria}
                          onChange={handleChange}
                          placeholder="Ej: Abarrotes"
                          list="categorias"
                        />
                        <datalist id="categorias">
                          <option value="Abarrotes" />
                          <option value="Lácteos" />
                          <option value="Bebidas" />
                          <option value="Snacks" />
                          <option value="Limpieza" />
                          <option value="Higiene Personal" />
                          <option value="Frescos" />
                          <option value="Congelados" />
                          <option value="Panadería" />
                          <option value="Otros" />
                        </datalist>
                        {formErrors.categoria && (
                          <div className="invalid-feedback">{formErrors.categoria}</div>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Stock y Stock Mínimo */}
                  <div className="row">
                    <div className="col-md-6 mb-3">
                      <label className="form-label fw-bold">
                        Stock Actual <span className="text-danger">*</span>
                      </label>
                      <div className="input-group">
                        <span className="input-group-text bg-light">
                          <i className="bi bi-box"></i>
                        </span>
                        <input
                          type="number"
                          min="0"
                          className={`form-control ${formErrors.stock ? 'is-invalid' : ''}`}
                          name="stock"
                          value={formData.stock}
                          onChange={handleChange}
                          placeholder="0"
                        />
                        {formErrors.stock && (
                          <div className="invalid-feedback">{formErrors.stock}</div>
                        )}
                      </div>
                    </div>

                    <div className="col-md-6 mb-3">
                      <label className="form-label fw-bold">
                        Stock Mínimo <span className="text-danger">*</span>
                      </label>
                      <div className="input-group">
                        <span className="input-group-text bg-light">
                          <i className="bi bi-exclamation-triangle"></i>
                        </span>
                        <input
                          type="number"
                          min="0"
                          className={`form-control ${formErrors.stockMinimo ? 'is-invalid' : ''}`}
                          name="stockMinimo"
                          value={formData.stockMinimo}
                          onChange={handleChange}
                          placeholder="5"
                        />
                        {formErrors.stockMinimo && (
                          <div className="invalid-feedback">{formErrors.stockMinimo}</div>
                        )}
                      </div>
                      <small className="text-muted">
                        <i className="bi bi-info-circle me-1"></i>
                        Alerta cuando el stock baje de este número
                      </small>
                    </div>
                  </div>

                  {/* 🟢 CARGA DE ARCHIVO DE IMAGEN */}
                  <div className="mb-4">
                    <label className="form-label fw-bold">Imagen del Producto</label>
                    <div className="input-group">
                      <span className="input-group-text bg-light">
                        <i className="bi bi-image"></i>
                      </span>
                      <input
                        type="file"
                        className="form-control"
                        accept="image/*"
                        onChange={handleFileChange}
                      />
                    </div>
                    {isEditing && formData.imagen && !selectedFile && (
                        <div className="mt-2 text-muted small">
                            Imagen actual: {formData.imagen}
                        </div>
                    )}
                    <small className="text-muted">
                      <i className="bi bi-info-circle me-1"></i>
                      Selecciona un archivo de imagen (JPG, PNG) para el producto.
                    </small>
                  </div>

                  {/* Botones */}
                  <div className="d-grid gap-2">
                    <button 
                      type="submit" 
                      className={`btn ${isEditing ? 'btn-primary' : 'btn-success'} py-2`}
                      disabled={submitLoading}
                    >
                      {submitLoading ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2"></span>
                          Procesando...
                        </>
                      ) : (
                        <>
                          <i className={`bi ${isEditing ? 'bi-check-circle' : 'bi-save'} me-2`}></i>
                          {isEditing ? 'Actualizar Producto' : 'Guardar Producto'}
                        </>
                      )}
                    </button>
                    <button 
                      type="button" 
                      className="btn btn-outline-secondary"
                      onClick={() => {
                        setFormData({
                          nombre: '',
                          descripcion: '',
                          precio: '',
                          stock: '',
                          stockMinimo: '5',
                          categoria: '',
                          imagen: ''
                        });
                        setFormErrors({});
                      }}
                    >
                      <i className="bi bi-eraser me-2"></i>
                      Limpiar Formulario
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>

          {/* Lista de productos existentes */}
          <div className="col-lg-7 mb-4">
            <div className="card shadow border-0">
              <div className="card-header bg-success text-white py-3">
                <div className="d-flex justify-content-between align-items-center">
                  <h5 className="mb-0">
                    <i className="bi bi-list-ul me-2"></i>
                    Productos Existentes ({productos.length})
                  </h5>
                  <button 
                    className="btn btn-sm btn-light"
                    onClick={() => listarProductos()}
                    disabled={loading}
                  >
                    <i className="bi bi-arrow-repeat me-1"></i>
                    Actualizar
                  </button>
                </div>
              </div>
              <div className="card-body p-0">
                {loading && productos.length === 0 ? (
                  <div className="text-center py-5">
                    <div className="spinner-border text-success mb-3"></div>
                    <p className="text-muted">Cargando productos...</p>
                  </div>
                ) : error ? (
                  <div className="alert alert-danger m-3">
                    <i className="bi bi-exclamation-triangle me-2"></i>
                    {error}
                  </div>
                ) : (
                  <div className="table-responsive">
                    <table className="table table-hover align-middle mb-0">
                      <thead className="bg-light">
                        <tr>
                          <th className="ps-4">Producto</th>
                          <th>Categoría</th>
                          <th className="text-end">Precio</th>
                          <th className="text-center">Stock</th>
                          <th className="text-center">Stock Mínimo</th>
                          <th className="text-center">Estado</th>
                          <th className="text-center">Acciones</th>
                        </tr>
                      </thead>
                      <tbody>
                        {productos.length === 0 ? (
                          <tr>
                            <td colSpan="7" className="text-center py-4 text-muted">
                              <i className="bi bi-inbox fs-2 d-block mb-2"></i>
                              No hay productos registrados
                            </td>
                          </tr>
                        ) : (
                          productos.map(prod => (
                            <tr key={prod.id}>
                              <td className="ps-4">
                                <div className="d-flex align-items-center">
                                  {prod.imagen ? (
                                    <img 
                                      src={prod.imagen.startsWith('http') ? prod.imagen : `/img/${prod.imagen}`}
                                      alt={prod.nombre}
                                      className="rounded me-3"
                                      style={{ width: '40px', height: '40px', objectFit: 'cover' }}
                                    />
                                  ) : (
                                    <div className="bg-success-soft rounded p-2 me-3">
                                      <i className="bi bi-box text-success"></i>
                                    </div>
                                  )}
                                  <div>
                                    <strong>{prod.nombre}</strong>
                                    {prod.descripcion && (
                                      <div className="text-muted small">{prod.descripcion}</div>
                                    )}
                                  </div>
                                </div>
                              </td>
                              <td>
                                <span className="badge bg-light text-dark px-3 py-2">
                                  {prod.categoria || 'Sin categoría'}
                                </span>
                              </td>
                              <td className="text-end fw-bold">
                                {formatCurrency(prod.precio)}
                              </td>
                              <td className="text-center">
                                <span className={`badge ${
                                  prod.stock <= prod.stockMinimo ? 'bg-warning text-dark' : 
                                  prod.stock === 0 ? 'bg-danger' : 'bg-success'
                                } px-3 py-2`}>
                                  {prod.stock} uds
                                </span>
                              </td>
                              <td className="text-center">
                                <span className="badge bg-secondary px-3 py-2">
                                  {prod.stockMinimo} uds
                                </span>
                              </td>
                              <td className="text-center">
                                {prod.stock === 0 ? (
                                  <span className="badge bg-danger">Agotado</span>
                                ) : prod.stock <= prod.stockMinimo ? (
                                  <span className="badge bg-warning text-dark">Stock Bajo</span>
                                ) : (
                                  <span className="badge bg-success">Normal</span>
                                )}
                              </td>
                              <td className="text-center">
                                 <div className="btn-group">
                                    <button 
                                        className="btn btn-sm btn-outline-primary"
                                        onClick={() => navigate(`/admin/ProductRegister?id=${prod.id}`)}
                                        title="Editar"
                                    >
                                        <i className="bi bi-pencil"></i>
                                    </button>
                                    <button 
                                        className="btn btn-sm btn-outline-danger"
                                        onClick={() => handleDelete(prod.id)}
                                        title="Inactivar"
                                    >
                                        <i className="bi bi-trash"></i>
                                    </button>
                                 </div>
                               </td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Estilos adicionales */}
      <style>{`
        .bg-success-soft {
          background: rgba(40, 167, 69, 0.1);
        }
        .table th {
          font-size: 0.8rem;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }
        .table td {
          padding: 1rem 0.5rem;
        }
      `}</style>
    </>
  );
};

export default ProductRegister;