// src/api/api.js

const API_URL = "/api";

/**
 * Login de usuario
 * Guarda el token JWT en localStorage
 */
export async function loginUser(username, password) {
  const response = await fetch(`${API_URL}/auth/signin`, {
    method: "POST",
    credentials: "omit",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ username, password }),
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || "Usuario o contraseña incorrectos");
  }

  // Guardar token
  localStorage.setItem("token", data.token);
  localStorage.setItem("user", JSON.stringify(data));

  return data;
}

/**
 * Obtiene el token JWT
 */
export function getToken() {
  return localStorage.getItem("token");
}

/**
 * Logout
 */
export function logoutUser() {
  localStorage.removeItem("token");
  localStorage.removeItem("user");
}

/**
 * Fetch con JWT para endpoints protegidos
 */
export async function fetchWithAuth(endpoint, options = {}) {
  const token = getToken();

  if (!token) {
    throw new Error("No hay token. Debes iniciar sesión.");
  }

  try {
    const response = await fetch(`${API_URL}${endpoint}`, {
      ...options,
      credentials: "omit",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
        ...(options.headers || {}),
      },
    });

    // Manejar error 401 (no autorizado)
    if (response.status === 401) {
      logoutUser();
      window.location.href = "/admin/login";
      throw new Error("Sesión expirada. Por favor, inicia sesión nuevamente.");
    }

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || `Error ${response.status}: ${response.statusText}`);
    }

    return data;
  } catch (error) {
    console.error("Error en fetchWithAuth:", error);
    throw error;
  }
}

/**
 * Fetch público (sin autenticación)
 */
export async function fetchPublic(endpoint, options = {}) {
  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    credentials: "omit",
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || "Error en petición pública");
  }

  return data;
}

// Obtener métodos de pago (con autenticación - para admin)
export async function getMetodosPago() {
  return fetchWithAuth('/test/pagos/metodos');
}

// Obtener métodos de pago (público - para clientes en el modal de compra)
export async function getMetodosPagoPublic() {
  return fetchPublic('/test/pagos/metodos');
}

// Finalizar venta (admin/vendedor autenticado)
export async function finalizarVenta(data) {
  return fetchWithAuth('/ventas/finalizar', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

// Finalizar venta desde el catálogo público (sin JWT)
// Usa el usuario genérico "kiosko" en el backend
export async function finalizarVentaPublico(data) {
  return fetchPublic('/ventas/finalizar-publico', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/**
 * Confirmar estado de pago de Mercado Pago por ventaId
 * El endpoint es público (no requiere JWT) - el cliente regresa de MP sin sesión
 */
export async function confirmarPagoMercadoPago(ventaId) {
  return fetchPublic(`/mercadopago/confirmar-pago/${ventaId}`);
}

/**
 * SUBIDA DE IMÁGENES
 */
export async function uploadImage(file) {
  const formData = new FormData();
  formData.append("file", file);

  const user = JSON.parse(localStorage.getItem("user"));
  const token = user?.token;

  const response = await fetch(`${API_URL}/media/upload`, {
    method: "POST",
    headers: {
      "Authorization": token ? `Bearer ${token}` : ""
    },
    body: formData,
  });

  if (response.status === 401) {
    throw new Error("Token inválido o no enviado");
  }

  const data = await response.json();
  if (!response.ok) throw new Error(data.message || "Error al subir imagen");
  return data; // { filename, url }
}

/**
 * GESTIÓN DE PRODUCTOS (ADMIN)
 */
export async function updateProducto(id, producto) {
  return fetchWithAuth(`/productos/${id}`, {
    method: "PUT",
    body: JSON.stringify(producto),
  });
}

export async function deleteProducto(id) {
  return fetchWithAuth(`/productos/${id}`, {
    method: "DELETE",
  });
}

/**
 * GESTIÓN DE USUARIOS (ADMIN)
 */
export async function getUsuariosAdmin() {
  return fetchWithAuth("/admin/usuarios");
}

export async function deleteUsuarioAdmin(id) {
  return fetchWithAuth(`/admin/usuarios/${id}`, {
    method: "DELETE",
  });
}

export default API_URL;