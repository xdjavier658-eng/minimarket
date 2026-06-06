import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser, getToken } from "../api/api";
import logo from "../assets/Login.png";
import "./login.css";

function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  // ✅ Redirección segura si ya está logueado
  useEffect(() => {
    if (getToken()) {
      navigate("/admin", { replace: true });
    }
  }, [navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMensaje("");
    setLoading(true);

    try {
      await loginUser(username, password);
      setMensaje("✅ Login exitoso");
      navigate("/admin", { replace: true });
    } catch {
      setMensaje("❌ Credenciales incorrectas");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container d-flex justify-content-center align-items-center min-vh-100 px-3">
      <div className="card shadow-lg p-3 p-md-4 login-card" style={{ maxWidth: "25rem", width: "100%" }}>
        <div className="card-body p-3">
          <div className="text-center">
            <img src={logo} alt="MiniMarket" className="w-100" />
          </div>

          <form onSubmit={handleSubmit}>
            <div className="mb-2 mt-5">
              <label className="fw-bold">Usuario</label>
              <input
                type="text"
                className="form-control form-control-lg bg-light"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>

            <div className="mb-2">
              <label className="fw-bold">Contraseña</label>
              <input
                type="password"
                className="form-control form-control-lg bg-light"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <button
              type="submit"
              className="btn btn-success w-75 btn-lg m-auto mt-4 d-block"
              disabled={loading}
            >
              {loading ? "Ingresando..." : "Ingresar"}
            </button>

            <button
              type="button"
              className="btn btn-outline-secondary w-75 btn-lg m-auto mt-3 d-block"
              onClick={() => navigate("/")}
            >
              Volver al catálogo
            </button>

            {mensaje && (
              <div className="alert alert-info mt-3 text-center">
                {mensaje}
              </div>
            )}
          </form>
        </div>
      </div>
    </div>
  );
}

export default Login;
