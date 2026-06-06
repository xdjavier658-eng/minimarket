import { useState, useEffect } from 'react';
import { QRCodeCanvas } from 'qrcode.react';
import { getMetodosPagoPublic, finalizarVentaPublico } from '../api/api';
import { useCart } from '../hooks/useCart';
import { useNavigate } from 'react-router-dom';
import './MetodoPagoModal.css'; // Estilos modernos

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
                sessionStorage.setItem('mp_carrito', JSON.stringify(carrito));
                sessionStorage.setItem('mp_total', String(total));
                sessionStorage.setItem('mp_ventaId', String(response.ventaId));
                sessionStorage.setItem('mp_metodoPago', selectedMetodo.nombre);

                limpiarCarrito();
                onClose();
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

    const getMetodoIcon = (nombre) => {
        switch(nombre) {
            case 'MERCADO_PAGO': return '💳';
            case 'YAPE': return '📱';
            case 'PLIN': return '📲';
            case 'EFECTIVO': return '💵';
            default: return '💰';
        }
    };

    const getMetodoLabel = (nombre) => {
        switch(nombre) {
            case 'MERCADO_PAGO': return 'Mercado Pago';
            case 'YAPE': return 'Yape';
            case 'PLIN': return 'Plin';
            case 'EFECTIVO': return 'Efectivo';
            default: return nombre;
        }
    };

    return (
        <div className="payment-modal-overlay" onClick={onClose}>
            <div className="payment-modal-container" onClick={(e) => e.stopPropagation()}>
                <style>{`
                    @import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700;800&family=Archivo+Black&display=swap');
                `}</style>

                {/* Encabezado */}
                <div className="payment-modal-header">
                    <h2 className="payment-modal-title">Caja Rápida de Pago</h2>
                    <button className="payment-modal-close" onClick={onClose}>
                        <span>&times;</span>
                    </button>
                </div>

                {/* Contenido Principal */}
                <div className="payment-modal-content">
                    {loading && (
                        <div className="payment-loading">
                            <div className="spinner"></div>
                            <p>Cargando métodos de pago...</p>
                        </div>
                    )}

                    {error && (
                        <div className="payment-error-banner">
                            <span>⚠️ {error}</span>
                        </div>
                    )}

                    {!loading && metodos.length > 0 && (
                        <div className="payment-modal-grid">
                            {/* COLUMNA IZQUIERDA: Métodos de Pago */}
                            <div className="payment-methods-section">
                                <h3 className="section-title">Elige tu método</h3>
                                <div className="payment-methods-grid">
                                    {metodos.map(metodo => (
                                        <div
                                            key={metodo.id}
                                            className={`payment-method-card ${selectedMetodo?.id === metodo.id ? 'active' : ''}`}
                                            onClick={() => {
                                                setSelectedMetodo(metodo);
                                                setReferencia('');
                                                setError('');
                                            }}
                                        >
                                            <input
                                                type="radio"
                                                name="metodoPago"
                                                id={`metodo-${metodo.id}`}
                                                value={metodo.id}
                                                checked={selectedMetodo?.id === metodo.id}
                                                style={{ display: 'none' }}
                                            />
                                            <span className="method-icon">{getMetodoIcon(metodo.nombre)}</span>
                                            <span className="method-label">{getMetodoLabel(metodo.nombre)}</span>
                                            {selectedMetodo?.id === metodo.id && (
                                                <span className="checkmark">✓</span>
                                            )}
                                        </div>
                                    ))}
                                </div>

                                {/* Referencia para YAPE / PLIN */}
                                {esOfflineConRef && (
                                    <div className="payment-reference-section">
                                        <label htmlFor="referencia" className="reference-label">
                                            Comprobante de pago
                                        </label>
                                        <input
                                            type="text"
                                            className="reference-input"
                                            id="referencia"
                                            value={referencia}
                                            onChange={(e) => setReferencia(e.target.value)}
                                            placeholder="Ej: 123456789"
                                        />
                                        <div className="qr-code-wrapper">
                                            <QRCodeCanvas
                                                value={`Pago ${selectedMetodo.nombre} - S/ ${total.toFixed(2)} - Ref: ${referencia || 'N/A'}`}
                                                size={140}
                                                bgColor="#ffffff"
                                                fgColor="#003366"
                                                level="L"
                                                includeMargin={false}
                                            />
                                            <p className="qr-hint">Escanea para pagar</p>
                                        </div>
                                    </div>
                                )}

                                {/* Aviso de Mercado Pago */}
                                {esMercadoPago && (
                                    <div className="payment-info-banner">
                                        <strong>✓ Transacción segura</strong>
                                        <p>Serás redirigido a Mercado Pago. Podrás pagar con tarjeta, efectivo o código QR.</p>
                                    </div>
                                )}
                            </div>

                            {/* COLUMNA DERECHA: Resumen de Pago */}
                            <div className="payment-summary-section">
                                <h3 className="section-title">Resumen</h3>
                                <div className="payment-summary-card">
                                    <div className="summary-items">
                                        {carrito.map((item, idx) => (
                                            <div key={idx} className="summary-item">
                                                <span className="item-name">{item.nombre}</span>
                                                <span className="item-qty">x{item.cantidad}</span>
                                                <span className="item-price">
                                                    S/ {(item.precio * item.cantidad).toFixed(2)}
                                                </span>
                                            </div>
                                        ))}
                                    </div>

                                    <div className="summary-divider"></div>

                                    <div className="summary-footer">
                                        <div className="summary-row">
                                            <span>Subtotal:</span>
                                            <span>S/ {(total / 1.18).toFixed(2)}</span>
                                        </div>
                                        <div className="summary-row">
                                            <span>IGV (18%):</span>
                                            <span>S/ {(total - total / 1.18).toFixed(2)}</span>
                                        </div>
                                        <div className="summary-total">
                                            <span>TOTAL A PAGAR</span>
                                            <span className="total-amount">S/ {total.toFixed(2)}</span>
                                        </div>
                                    </div>

                                    <button
                                        className={`payment-confirm-btn ${loading ? 'loading' : ''}`}
                                        onClick={handleConfirmar}
                                        disabled={loading || !selectedMetodo}
                                    >
                                        {loading ? (
                                            <>
                                                <span className="spinner-small"></span> Procesando...
                                            </>
                                        ) : (
                                            <>
                                                🔒 Confirmar Pago
                                            </>
                                        )}
                                    </button>

                                    <button
                                        className="payment-cancel-btn"
                                        onClick={onClose}
                                        disabled={loading}
                                    >
                                        Cancelar
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default MetodoPagoModal;