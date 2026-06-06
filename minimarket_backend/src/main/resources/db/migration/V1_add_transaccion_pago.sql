-- =====================================================
-- MIGRACIÓN: Agregar tabla transaccion_pago
-- Fecha: 2026-02-26
-- Descripción: Crea la tabla para tracking de pagos
-- =====================================================

-- 1. Crear la tabla transaccion_pago
CREATE TABLE IF NOT EXISTS transaccion_pago (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(20) NOT NULL,
    estado_transaccion VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(255) UNIQUE,
    preference_id VARCHAR(255),
    init_point VARCHAR(500),
    payment_method_id VARCHAR(50),
    payment_type_id VARCHAR(50),
    payer_email VARCHAR(100),
    payer_identification_type VARCHAR(20),
    payer_identification_number VARCHAR(20),
    external_reference VARCHAR(100),
    metadata TEXT,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP,
    fecha_confirmacion TIMESTAMP,
    fecha_expiracion TIMESTAMP,
    pago_id BIGINT UNIQUE,
    ip_cliente VARCHAR(45),
    user_agent VARCHAR(500),
    dispositivo VARCHAR(50),
    intento_numero INTEGER DEFAULT 1,
    codigo_error VARCHAR(50),
    mensaje_error VARCHAR(500),
    detalle_error TEXT,
    creado_por VARCHAR(50),
    actualizado_por VARCHAR(50),
    
    -- Constraints
    CONSTRAINT fk_transaccion_venta FOREIGN KEY (venta_id) 
        REFERENCES venta(id) ON DELETE CASCADE,
    CONSTRAINT fk_transaccion_pago FOREIGN KEY (pago_id) 
        REFERENCES pago(id) ON DELETE SET NULL,
    CONSTRAINT chk_estado_transaccion CHECK (
        estado_transaccion IN ('INICIADO', 'EN_PROCESO', 'EXITOSO', 'FALLIDO', 'EXPIRADO', 'REEMBOLSADO')
    ),
    CONSTRAINT chk_metodo_pago CHECK (
        metodo_pago IN ('EFECTIVO', 'YAPE', 'PLIN', 'MERCADO_PAGO')
    )
);

-- 2. Crear índices para mejorar performance
CREATE INDEX IF NOT EXISTS idx_transaccion_venta ON transaccion_pago(venta_id);
CREATE INDEX IF NOT EXISTS idx_transaccion_estado ON transaccion_pago(estado_transaccion);
CREATE INDEX IF NOT EXISTS idx_transaccion_fecha ON transaccion_pago(fecha_creacion);
CREATE INDEX IF NOT EXISTS idx_transaccion_transaction_id ON transaccion_pago(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaccion_preference_id ON transaccion_pago(preference_id);
CREATE INDEX IF NOT EXISTS idx_transaccion_payer_email ON transaccion_pago(payer_email);
CREATE INDEX IF NOT EXISTS idx_transaccion_expiracion ON transaccion_pago(fecha_expiracion);

-- 3. Migrar datos existentes (pagos actuales)
-- Esto crea una transacción por cada pago existente
INSERT INTO transaccion_pago (
    venta_id,
    monto,
    metodo_pago,
    estado_transaccion,
    fecha_creacion,
    fecha_confirmacion,
    pago_id,
    creado_por
)
SELECT 
    v.id as venta_id,
    p.monto,
    UPPER(mp.nombre) as metodo_pago, -- Convertir a mayúsculas para el enum
    'EXITOSO' as estado_transaccion,
    p.fecha as fecha_creacion,
    p.fecha as fecha_confirmacion,
    p.id as pago_id,
    'SYSTEM_MIGRATION' as creado_por
FROM pago p
INNER JOIN venta v ON v.id = p.venta_id
INNER JOIN metodo_pago mp ON mp.id = p.metodo_pago_id
WHERE v.estado = 'PAGADO';

-- 4. Actualizar estadísticas (opcional - para verificar migración)
DO $$
DECLARE
    total_migrados INTEGER;
    total_pagos INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_migrados FROM transaccion_pago;
    SELECT COUNT(*) INTO total_pagos FROM pago;
    
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'MIGRACIÓN COMPLETADA';
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Total pagos en sistema: %', total_pagos;
    RAISE NOTICE 'Total transacciones migradas: %', total_migrados;
    RAISE NOTICE 'Pagos sin migrar (debería ser 0): %', (total_pagos - total_migrados);
    RAISE NOTICE '==========================================';
END $$;

-- 5. Verificación de integridad
-- Listar pagos que no tienen transacción (debería ser 0)
SELECT p.id, p.monto, v.estado 
FROM pago p
LEFT JOIN transaccion_pago tp ON tp.pago_id = p.id
INNER JOIN venta v ON v.id = p.venta_id
WHERE tp.id IS NULL;

-- 6. Nota sobre la migración
COMMENT ON TABLE transaccion_pago IS 'Tabla para tracking de transacciones de pago. Migración ejecutada el 2026-02-26';
