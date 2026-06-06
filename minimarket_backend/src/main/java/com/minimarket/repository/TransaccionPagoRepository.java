package com.minimarket.repository;

import com.minimarket.entity.TransaccionPago;
import com.minimarket.entity.Venta;
import com.minimarket.entity.enums.EstadoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionPagoRepository extends JpaRepository<TransaccionPago, Long> {
    
    // Buscar por venta
    List<TransaccionPago> findByVenta(Venta venta);
    
    // Buscar última transacción de una venta
    Optional<TransaccionPago> findFirstByVentaOrderByFechaCreacionDesc(Venta venta);
    
    // Buscar por ID de transacción externo (Mercado Pago)
    Optional<TransaccionPago> findByTransactionId(String transactionId);
    
    // Buscar por preferenceId (Mercado Pago)
    Optional<TransaccionPago> findByPreferenceId(String preferenceId);
    
    // Transacciones pendientes por tiempo (para limpieza/reintentos)
    @Query("SELECT t FROM TransaccionPago t WHERE t.estadoTransaccion = :estado " +
           "AND t.fechaCreacion < :fechaLimite")
    List<TransaccionPago> findTransaccionesPendientesVencidas(
            @Param("estado") EstadoTransaccion estado,
            @Param("fechaLimite") LocalDateTime fechaLimite);
    
    // Transacciones expiradas
    @Query("SELECT t FROM TransaccionPago t WHERE t.fechaExpiracion < :ahora " +
           "AND t.estadoTransaccion NOT IN ('EXITOSO', 'FALLIDO', 'REEMBOLSADO')")
    List<TransaccionPago> findTransaccionesExpiradas(@Param("ahora") LocalDateTime ahora);
    
    // Buscar por rango de fechas (para reportes)
    List<TransaccionPago> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Contar por estado (estadísticas)
    Long countByEstadoTransaccion(EstadoTransaccion estado);
    
    // Estadísticas para dashboard
    @Query("SELECT " +
           "COUNT(t) as total, " +
           "SUM(CASE WHEN t.estadoTransaccion = 'EXITOSO' THEN 1 ELSE 0 END) as exitosos, " +
           "SUM(CASE WHEN t.estadoTransaccion = 'FALLIDO' THEN 1 ELSE 0 END) as fallidos, " +
           "SUM(CASE WHEN t.estadoTransaccion = 'INICIADO' THEN 1 ELSE 0 END) as pendientes " +
           "FROM TransaccionPago t " +
           "WHERE t.fechaCreacion BETWEEN :inicio AND :fin")
    List<Object[]> getEstadisticasPorPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
    
    // Estadísticas por método de pago
    @Query("SELECT t.metodoPago, " +
           "COUNT(t) as cantidad, " +
           "SUM(t.monto) as total, " +
           "SUM(CASE WHEN t.estadoTransaccion = 'EXITOSO' THEN 1 ELSE 0 END) as exitosos " +
           "FROM TransaccionPago t " +
           "WHERE t.fechaCreacion BETWEEN :inicio AND :fin " +
           "GROUP BY t.metodoPago")
    List<Object[]> getEstadisticasPorMetodoPago(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
    
    // Tasa de conversión (exitosos vs total)
    @Query("SELECT " +
           "COUNT(t) as total, " +
           "SUM(CASE WHEN t.estadoTransaccion = 'EXITOSO' THEN 1 ELSE 0 END) as exitosos " +
           "FROM TransaccionPago t " +
           "WHERE t.fechaCreacion BETWEEN :inicio AND :fin")
    List<Object[]> getTasaConversion(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
    
    // Buscar transacciones por email (para historial del cliente)
    List<TransaccionPago> findByPayerEmailOrderByFechaCreacionDesc(String email);
    
    // Buscar transacciones con problemas
    @Query("SELECT t FROM TransaccionPago t WHERE " +
           "t.codigoError IS NOT NULL AND " +
           "t.fechaCreacion > :fechaLimite " +
           "ORDER BY t.fechaCreacion DESC")
    List<TransaccionPago> findTransaccionesConError(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    // Verificar si existe transacción exitosa para una venta
    @Query("SELECT COUNT(t) > 0 FROM TransaccionPago t " +
           "WHERE t.venta.id = :ventaId AND t.estadoTransaccion = 'EXITOSO'")
    boolean existsTransaccionExitosaByVentaId(@Param("ventaId") Long ventaId);
    
    // Obtener última transacción por venta (cualquier estado)
    @Query("SELECT t FROM TransaccionPago t WHERE t.venta.id = :ventaId " +
           "ORDER BY t.fechaCreacion DESC LIMIT 1")
    Optional<TransaccionPago> findUltimaTransaccionByVentaId(@Param("ventaId") Long ventaId);
}