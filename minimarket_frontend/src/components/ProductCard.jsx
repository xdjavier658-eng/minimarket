function ProductCard({ producto, onAdd }) {
  return (
    <div className="col d-flex">
      <div className="card shadow-sm flex-fill">
        <img
          src={`/img/${producto.imagen}`}
          className="card-img-top"
          style={{ height: "200px", objectFit: "cover" }}
          alt={producto.nombre}
        />

        <div className="card-body d-flex flex-column">
          <h5>{producto.nombre}</h5>

          <p className="text-muted small flex-grow-1">
            {producto.descripcion}
          </p>

          <div className="d-flex justify-content-between align-items-center">
            <span className="fw-bold">
              S/ {producto.precio.toFixed(2)}
            </span>

            <button
              className="btn btn-success btn-sm"
              onClick={() => onAdd(producto)}
            >
              Agregar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductCard;
