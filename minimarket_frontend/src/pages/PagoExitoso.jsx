import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { confirmarPagoMercadoPago } from '../api/api';

export default function PagoExitoso() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    // Parámetros que devuelve Mercado Pago en la URL
    const paymentId = searchParams.get('payment_id');
    const status = searchParams.get('status');
    const externalReference = searchParams.get('external_reference'); // ventaId

    // Recuperar datos guardados antes de redirigir a MP
    const carritoGuardado = JSON.parse(sessionStorage.getItem('mp_carrito') || '[]');
    const totalGuardado = parseFloat(sessionStorage.getItem('mp_total') || '0');
    const ventaIdGuardado = sessionStorage.getItem('mp_ventaId');
    const [estadoPago, setEstadoPago] = useState(() => {
        const ventaId = externalReference || ventaIdGuardado;
        return ventaId ? 'procesando' : 'error';
    });

    useEffect(() => {
        const ventaId = externalReference || ventaIdGuardado;

        if (!ventaId) {
            // Ya se setea en el estado inicial
            return;
        }

        // Consultar al backend el estado real del pago
        confirmarPagoMercadoPago(ventaId)
            .then(response => {
                if (response.estado === 'PAGADO' || response.estado === 'approved') {
                    setEstadoPago('confirmado');
                } else if (response.estado === 'PENDIENTE' || response.estado === 'pending') {
                    setEstadoPago('pendiente');
                } else {
                    setEstadoPago('confirmado'); // MP devolvió success, asumimos OK
                }
            })
            .catch(() => {
                // Si falla la consulta, confiar en lo que dijo MP en la URL
                if (status === 'approved') {
                    setEstadoPago('confirmado');
                } else {
                    setEstadoPago('pendiente');
                }
            })
            .finally(() => {
                // Limpiar sessionStorage
                sessionStorage.removeItem('mp_carrito');
                sessionStorage.removeItem('mp_total');
                sessionStorage.removeItem('mp_ventaId');
                sessionStorage.removeItem('mp_metodoPago');
            });
    }, [externalReference, status, ventaIdGuardado]);

    const verTicket = () => {
        const ventaId = externalReference || ventaIdGuardado;
        navigate('/ticket', {
            state: {
                venta: { ventaId: ventaId ? parseInt(ventaId) : null, metodoPago: 'MERCADO_PAGO' },
                carrito: carritoGuardado,
                metodoPago: 'MERCADO_PAGO',
                total: totalGuardado
            }
        });
    };

    return (
        <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
            <div className="card shadow text-center p-5" style={{ maxWidth: 480, width: '100%' }}>

                {estadoPago === 'procesando' && (
                    <>
                        <div className="spinner-border text-success mb-4" role="status" style={{ width: '3rem', height: '3rem' }}>
                            <span className="visually-hidden">Verificando...</span>
                        </div>
                        <h5 className="text-muted">Verificando tu pago...</h5>
                    </>
                )}

                {estadoPago === 'confirmado' && (
                    <>
                        <div className="mb-4">
                            <span style={{ fontSize: '4rem' }}>✅</span>
                        </div>
                        <h4 className="text-success mb-2">¡Pago confirmado!</h4>
                        <p className="text-muted mb-1">
                            Tu pago con Mercado Pago fue procesado exitosamente.
                        </p>
                        {paymentId && (
                            <p className="text-muted small">
                                ID de pago: <strong>{paymentId}</strong>
                            </p>
                        )}
                        <div className="d-flex gap-2 justify-content-center mt-4">
                            <button className="btn btn-success" onClick={verTicket}>
                                Ver Ticket
                            </button>
                            <button className="btn btn-outline-secondary" onClick={() => navigate('/')}>
                                Volver a la tienda
                            </button>
                        </div>
                    </>
                )}

                {estadoPago === 'pendiente' && (
                    <>
                        <div className="mb-4">
                            <span style={{ fontSize: '4rem' }}>⏳</span>
                        </div>
                        <h4 className="text-warning mb-2">Pago en revisión</h4>
                        <p className="text-muted mb-1">
                            Tu pago está siendo procesado. Te confirmaremos cuando esté aprobado.
                        </p>
                        {paymentId && (
                            <p className="text-muted small">
                                ID de pago: <strong>{paymentId}</strong>
                            </p>
                        )}
                        <div className="d-flex gap-2 justify-content-center mt-4">
                            <button className="btn btn-warning text-white" onClick={verTicket}>
                                Ver Ticket
                            </button>
                            <button className="btn btn-outline-secondary" onClick={() => navigate('/')}>
                                Volver a la tienda
                            </button>
                        </div>
                    </>
                )}

                {estadoPago === 'error' && (
                    <>
                        <div className="mb-4">
                            <span style={{ fontSize: '4rem' }}>⚠️</span>
                        </div>
                        <h4 className="text-danger mb-2">No pudimos verificar tu pago</h4>
                        <p className="text-muted">
                            Comunícate con nosotros indicando tu ID de pago para confirmar el estado.
                        </p>
                        {paymentId && (
                            <p className="text-muted small">
                                ID de pago: <strong>{paymentId}</strong>
                            </p>
                        )}
                        <button className="btn btn-outline-secondary mt-3" onClick={() => navigate('/')}>
                            Volver a la tienda
                        </button>
                    </>
                )}
            </div>
        </div>
    );
}
