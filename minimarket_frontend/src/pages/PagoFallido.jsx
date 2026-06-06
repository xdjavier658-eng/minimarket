import { useNavigate, useSearchParams } from 'react-router-dom';

export default function PagoFallido() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const paymentId = searchParams.get('payment_id');
    const statusDetail = searchParams.get('status_detail');

    // Limpiar sessionStorage
    sessionStorage.removeItem('mp_carrito');
    sessionStorage.removeItem('mp_total');
    sessionStorage.removeItem('mp_ventaId');
    sessionStorage.removeItem('mp_metodoPago');

    const mensajeError = () => {
        switch (statusDetail) {
            case 'cc_rejected_insufficient_amount': return 'Fondos insuficientes en la tarjeta.';
            case 'cc_rejected_bad_filled_card_number': return 'Número de tarjeta incorrecto.';
            case 'cc_rejected_bad_filled_date': return 'Fecha de vencimiento incorrecta.';
            case 'cc_rejected_bad_filled_security_code': return 'Código de seguridad incorrecto.';
            case 'cc_rejected_call_for_authorize': return 'Tu banco requiere autorización. Llama a tu banco.';
            case 'cc_rejected_duplicated_payment': return 'Pago duplicado detectado.';
            default: return 'El pago no pudo completarse. Por favor intenta con otro método de pago.';
        }
    };

    return (
        <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
            <div className="card shadow text-center p-5" style={{ maxWidth: 480, width: '100%' }}>
                <div className="mb-4">
                    <span style={{ fontSize: '4rem' }}>❌</span>
                </div>
                <h4 className="text-danger mb-2">Pago rechazado</h4>
                <p className="text-muted mb-3">{mensajeError()}</p>

                {paymentId && (
                    <p className="text-muted small mb-3">
                        ID de referencia: <strong>{paymentId}</strong>
                    </p>
                )}

                <div className="alert alert-light border mb-4 text-start small">
                    <strong>¿Qué puedo hacer?</strong>
                    <ul className="mb-0 mt-1">
                        <li>Verifica los datos de tu tarjeta</li>
                        <li>Prueba con un método de pago diferente</li>
                        <li>Contacta a tu banco si el problema persiste</li>
                    </ul>
                </div>

                <div className="d-flex gap-2 justify-content-center">
                    <button className="btn btn-success" onClick={() => navigate('/')}>
                        Intentar nuevamente
                    </button>
                    <button className="btn btn-outline-secondary" onClick={() => navigate('/')}>
                        Volver a la tienda
                    </button>
                </div>
            </div>
        </div>
    );
}
