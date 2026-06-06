import { useState, useEffect } from 'react';
import { QRCodeCanvas } from 'qrcode.react';
import { getMetodosPagoPublic, finalizarVentaPublico } from '../api/api';
import { useCart } from '../hooks/useCart';
import { useNavigate } from 'react-router-dom';

const MetodoPagoModal = ({ show, onClose }) => {
    const { carrito, total, limpiarCarrito } = useCart();
    const navigate = useNavigate();

    const [metodos, setMetodos] = useState([]);
    const [selectedMetodo, setSelectedMetodo] = useState(null);
    const [referencia, setReferencia] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (show) {
            setLoading(true);
            setError('');
            setReferencia('');
            getMetodosPagoPublic()
                .then(data => {
                    const activos = data.filter(m => m.activo);
                    setMetodos(activos);
                    const efectivo = activos.find(m => m.nombre === 'EFECTIVO');
                    if (efectivo) setSelectedMetodo(efectivo);
                })
                .catch(() => setError('Error al cargar métodos de pago'))
                .finally(() => setLoading(false));
        }
    }, [show]);

    const esMercadoPago = selectedMetodo?.nombre === 'MERCADO_PAGO';
    const esOfflineConRef = selectedMetodo && selectedMetodo.nombre !== 'EFECTIVO' && !esMercadoPago;

    const handleConfirmar = async () => {
        if (!selectedMetodo) {
            setError('Selecciona un método de pago');
            return;
        }

        // Para YAPE/PLIN se requiere referencia
        if (esOfflineConRef && !referencia.trim()) {
            setError('Referencia obligatoria para ' + selectedMetodo.nombre);
            return;
        }

        setLoading(true);
        setError('');

        try {
            const items = carrito.map(item => ({
                productoId: item.id,
                cantidad: item.cantidad
            }));

            const response = await finalizarVentaPublico({
                items,
                metodoPagoId: selectedMetodo.id,
                referencia: esOfflineConRef ? referencia : ''
            });

            // =====================================================
            // CASO 1: Mercado Pago → redirigir al checkout externo
            // =====================================================
            if (response.estado === 'REDIRECCION' && response.redirectUrl) {
                // Guardar el carrito y datos de la venta en sessionStorage
                // para recuperarlos cuando el usuario vuelva del flujo de MP
                sessionStorage.setItem('mp_carrito', JSON.stringify(carrito));
                sessionStorage.setItem('mp_total', String(total));
                sessionStorage.setItem('mp_ventaId', String(response.ventaId));
                sessionStorage.setItem('mp_metodoPago', selectedMetodo.nombre);

                // Limpiar carrito del contexto y cerrar modal
                limpiarCarrito();
                onClose();

                // Redirigir al checkout de Mercado Pago
                window.location.href = response.redirectUrl;
                return;
            }

            // =====================================================
            // CASO 2: Pago offline (EFECTIVO, YAPE, PLIN) → ticket
            // =====================================================
            navigate('/ticket', {
                state: {
                    venta: response,
                    carrito: carrito,
                    metodoPago: selectedMetodo.nombre,
                    total: total
                }
            });

            limpiarCarrito();
            onClose();
        } catch (err) {
            setError(err.message || 'Error al procesar el pago. Inténtalo de nuevo.');
        } finally {
            setLoading(false);
        }
    };

    if (!show) return null;

    return (
        <div className="modal show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
            <div className="modal-dialog">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">Seleccionar Método de Pago</h5>
                        <button type="button" className="btn-close" onClick={onClose}></button>
                    </div>
                    <div className="modal-body">
                        {loading && <div className="text-center">Cargando...</div>}
                        {error && <div className="alert alert-danger">{error}</div>}

                        {!loading && metodos.length > 0 && (
                            <>
                                <div className="mb-3">
                                    {metodos.map(metodo => (
                                        <div key={metodo.id} className="form-check mb-2">
                                            <input
                                                className="form-check-input"
                                                type="radio"
                                                name="metodoPago"
                                                id={`metodo-${metodo.id}`}
                                                value={metodo.id}
                                                checked={selectedMetodo?.id === metodo.id}
                                                onChange={() => {
                                                    setSelectedMetodo(metodo);
                                                    setReferencia('');
                                                    setError('');
                                                }}
                                            />
                                            <label className="form-check-label" htmlFor={`metodo-${metodo.id}`}>
                                                {metodo.nombre === 'MERCADO_PAGO' ? '💳 Mercado Pago (tarjeta / QR)' : metodo.nombre}
                                            </label>
                                        </div>
                                    ))}
                                </div>

                                {/* Aviso informativo para Mercado Pago */}
                                {esMercadoPago && (
                                    <div className="alert alert-info mb-3">
                                        <strong>💳 Pago con Mercado Pago</strong><br />
                                        Serás redirigido a la plataforma de Mercado Pago para completar el pago de forma segura.
                                        Podrás pagar con tarjeta de crédito, débito, efectivo o código QR.
                                    </div>
                                )}

                                {/* Referencia para YAPE / PLIN */}
                                {esOfflineConRef && (
                                    <div className="mb-3">
                                        <label htmlFor="referencia" className="form-label">Referencia del pago</label>
                                        <input
                                            type="text"
                                            className="form-control"
                                            id="referencia"
                                            value={referencia}
                                            onChange={(e) => setReferencia(e.target.value)}
                                            placeholder="Número de operación"
                                        />
                                        <div className="mt-3 text-center">
                                            <QRCodeCanvas
                                                value={`Pago ${selectedMetodo.nombre} - S/ ${total.toFixed(2)} - Ref: ${referencia || 'N/A'}`}
                                                size={180}
                                                bgColor="#ffffff"
                                                fgColor="#000000"
                                                level="L"
                                                includeMargin={false}
                                            />
                                            <p className="text-muted small mt-2">
                                                Escanea para pagar con {selectedMetodo.nombre}
                                            </p>
                                        </div>
                                    </div>
                                )}

                                <div className="d-flex justify-content-between">
                                    <strong>Total a pagar:</strong>
                                    <strong className="text-success">S/ {total.toFixed(2)}</strong>
                                </div>
                            </>
                        )}
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
                            Cancelar
                        </button>
                        <button
                            type="button"
                            className="btn btn-success"
                            onClick={handleConfirmar}
                            disabled={loading || carrito.length === 0}
                        >
                            {loading
                                ? 'Procesando...'
                                : esMercadoPago
                                    ? '💳 Pagar con Mercado Pago'
                                    : 'Confirmar Pago'
                            }
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default MetodoPagoModal;