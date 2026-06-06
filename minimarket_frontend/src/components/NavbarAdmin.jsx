import { Link, useNavigate } from "react-router-dom";
import { logoutUser } from "../api/api";

export default function NavbarAdmin() {
  const navigate = useNavigate();

  const handleLogout = () => {
    logoutUser();
    navigate("/admin/login");
  };

  const user = JSON.parse(localStorage.getItem("user")) || {};

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-success mb-4">
      <div className="container-fluid">
        <Link className="navbar-brand" to="/admin">
          MiniMarket Admin
        </Link>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbarAdmin"
          aria-controls="navbarAdmin"
          aria-expanded="false"
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="navbarAdmin">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            <li className="nav-item">
              <Link className="nav-link" to="/admin">
                Dashboard
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/">
                Minimarket
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/admin/ProductRegister">
                Registrar Producto
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/admin/UserRegister">
                Registrar Usuario
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to="/admin/reportes">
                <i className="bi bi-bar-chart-line me-1"></i>
                Reportes
              </Link>
            </li>
          </ul>

          <span className="navbar-text text-white me-3">
            {user.username || "Admin"}
          </span>

          <button className="btn btn-outline-light" onClick={handleLogout}>
            Cerrar Sesión
          </button>
        </div>
      </div>
    </nav>
  );
}
