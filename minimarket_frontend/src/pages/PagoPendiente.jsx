import { useNavigate, useSearchParams } from 'react-router-dom';

export default function PagoPendiente() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const paymentId = searchParams.get('payment_id');

    // Recuperar datos guardados
    const carritoGuardado = JSON.parse(sessionStorage.getItem('mp_carrito') || '[]');
    const totalGuardado = parseFloat(sessionStorage.getItem('mp_total') || '0');
    const ventaIdGuardado = sessionStorage.getItem('mp_ventaId');

    const verTicket = () => {
        // Limpiar sessionStorage
        sessionStorage.removeItem('mp_carrito');
        sessionStorage.removeItem('mp_total');
        sessionStorage.removeItem('mp_ventaId');
        sessionStorage.removeItem('mp_metodoPago');

        navigate('/ticket', {
            state: {
                venta: { ventaId: ventaIdGuardado ? parseInt(ventaIdGuardado) : null, metodoPago: 'MERCADO_PAGO' },
                carrito: carritoGuardado,
                metodoPago: 'MERCADO_PAGO (Pendiente)',
                total: totalGuardado
            }
        });
    };

    return (
        <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
            <div className="card shadow text-center p-5" style={{ maxWidth: 480, width: '100%' }}>
                <div className="mb-4">
                    <span style={{ fontSize: '4rem' }}>⏳</span>
                </div>
                <h4 className="text-warning mb-2">Pago pendiente</h4>
                <p className="text-muted mb-3">
                    Tu pago está siendo procesado. Esto puede tardar unos minutos.
                    Te notificaremos cuando sea confirmado.
                </p>

                {paymentId && (
                    <p className="text-muted small mb-3">
                        ID de pago: <strong>{paymentId}</strong>
                    </p>
                )}

                <div className="alert alert-warning border mb-4 text-start small">
                    <strong>¿Qué significa esto?</strong>
                    <ul className="mb-0 mt-1">
                        <li>Si pagaste en efectivo (Rapipago, PagoFácil), el pago puede tardar hasta 2 días hábiles.</li>
                        <li>Si pagaste con tarjeta, el banco puede estar verificando la transacción.</li>
                        <li>Tu pedido está reservado mientras procesamos el pago.</li>
                    </ul>
                </div>

                <div className="d-flex gap-2 justify-content-center">
                    <button className="btn btn-warning text-white" onClick={verTicket}>
                        Ver Ticket Provisional
                    </button>
                    <button className="btn btn-outline-secondary" onClick={() => navigate('/')}>
                        Volver a la tienda
                    </button>
                </div>
            </div>
        </div>
    );
}
