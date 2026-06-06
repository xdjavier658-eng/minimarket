import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { fetchWithAuth, getUsuariosAdmin, deleteUsuarioAdmin } from "../api/api";

function UserRegister() {

    const navigate = useNavigate();

    // Estados de formulario
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [roles, setRoles] = useState([]);
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);

    const [usuarios, setUsuarios] = useState([]);

    useEffect(() => {
        cargarUsuarios();
    }, []);

    const cargarUsuarios = async () => {
        try {
            const data = await getUsuariosAdmin();
            setUsuarios(data);
        } catch (error) {
            console.error("Error al cargar usuarios:", error);
        }
    };

    // Manejo de submit
    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage("");

        try {
            await fetchWithAuth(
                "/auth/signup",
                {
                    method: "POST",
                    body: JSON.stringify({
                        username,
                        password,
                        roles, // ["ADMIN"], ["VENDEDOR"], ["ALMACENERO"]
                    })
                }
            );

            setMessage("✅ Usuario registrado exitosamente");
            setUsername("");
            setPassword("");
            setRoles([]);
            cargarUsuarios();
        } catch (error) {
            setMessage(error.message || "Error al registrar usuario");
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm("¿Estás seguro de desactivar a este usuario?")) {
            try {
                await deleteUsuarioAdmin(id);
                setMessage("✅ Usuario desactivado correctamente");
                cargarUsuarios();
                setTimeout(() => setMessage(""), 3000);
            } catch (error) {
                alert("Error al desactivar usuario: " + error.message);
            }
        }
    };

    // Manejo de roles (checkboxes)
    const handleRoleChange = (e) => {
        const value = e.target.value;
        setRoles((prev) =>
            prev.includes(value) ? prev.filter((r) => r !== value) : [...prev, value]
        );
    };

    return (
        <div className="d-flex justify-content-center align-items-start min-vh-100 py-3 py-md-5 px-2 px-md-3">
            <div className="mt-2 mt-md-4 card shadow-lg p-3 p-md-5 user-register-card w-100" style={{ maxWidth: '600px' }}>
                <h2 className="text-center text-success mb-2 mb-md-3" style={{ fontSize: 'clamp(1.3rem, 4vw, 1.75rem)' }}>Minimarket</h2>
                <h5  className="text-center text-success mb-3 mb-md-4">Registro de Usuario</h5>
                {message && <div className={`alert ${message.includes("✅") ? "alert-success" : "alert-danger"}`}>{message}</div>}
                <form onSubmit={handleSubmit} style={{ width: "100%", maxWidth: "400px", margin: "0 auto" }}>
                    {/* Username */}
                    <div className="mb-3">
                        <label htmlFor="username" className="form-label fw-bold">
                            Usuario
                        </label>
                        <input
                            type="text"
                            className="form-control bg-light border-0"
                            id="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>

                    {/* Password */}
                    <div className="mb-3">
                        <label htmlFor="password" className="form-label fw-bold">
                            Contraseña
                        </label>
                        <input
                            type="password"
                            className="form-control bg-light border-0"
                            id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    {/* Roles */}
                    <div className="mb-3">
                        <label className="form-label fw-bold">Roles</label>
                        <div className="d-flex gap-3 flex-wrap">
                            <div className="form-check">
                                <input
                                    type="checkbox"
                                    className="form-check-input"
                                    id="admin"
                                    value="ADMIN"
                                    checked={roles.includes("ADMIN")}
                                    onChange={handleRoleChange}
                                />
                                <label className="form-check-label" htmlFor="admin">Admin</label>
                            </div>
                            <div className="form-check">
                                <input
                                    type="checkbox"
                                    className="form-check-input"
                                    id="vendedor"
                                    value="VENDEDOR"
                                    checked={roles.includes("VENDEDOR")}
                                    onChange={handleRoleChange}
                                />
                                <label className="form-check-label" htmlFor="vendedor">Vendedor</label>
                            </div>
                            <div className="form-check">
                                <input
                                    type="checkbox"
                                    className="form-check-input"
                                    id="almacenero"
                                    value="ALMACENERO"
                                    checked={roles.includes("ALMACENERO")}
                                    onChange={handleRoleChange}
                                />
                                <label className="form-check-label" htmlFor="almacenero">Almacenero</label>
                            </div>
                        </div>
                    </div>

                    {/* Submit */}
                    <div className="d-grid gap-2">
                        <button
                            type="submit"
                            className="btn btn-success"
                            disabled={loading}
                        >
                            {loading ? "Registrando..." : "Registrar Nuevo Usuario"}
                        </button>

                        <button
                            type="button"
                            className="btn btn-outline-secondary"
                            onClick={() => navigate("/admin")}
                        >
                            <i className="bi bi-arrow-left me-2"></i>
                            Volver al Panel
                        </button>
                    </div>
                </form>

                {/* TABLA DE USUARIOS */}
                <div className="mt-5 pt-4 border-top">
                    <h5 className="text-success mb-3"><i className="bi bi-people me-2"></i>Usuarios Registrados</h5>
                    <div className="table-responsive">
                        <table className="table table-hover align-middle">
                            <thead className="table-light">
                                <tr>
                                    <th>Usuario</th>
                                    <th>Roles</th>
                                    <th>Estado</th>
                                    <th className="text-center">Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                {usuarios.map(user => (
                                    <tr key={user.id}>
                                        <td>{user.username}</td>
                                        <td>
                                            {user.roles.map(r => (
                                                <span key={r.id} className="badge bg-info-subtle text-info-emphasis me-1">
                                                    {r.nombre.replace('ROLE_', '')}
                                                </span>
                                            ))}
                                        </td>
                                        <td>
                                            {user.activo ? 
                                                <span className="badge bg-success">Activo</span> : 
                                                <span className="badge bg-secondary">Inactivo</span>
                                            }
                                        </td>
                                        <td className="text-center">
                                            {user.activo && user.username !== 'admin' && (
                                                <button 
                                                    className="btn btn-sm btn-outline-danger"
                                                    onClick={() => handleDelete(user.id)}
                                                    title="Desactivar"
                                                >
                                                    <i className="bi bi-person-x"></i>
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default UserRegister;
