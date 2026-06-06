package com.minimarket.repository;

import com.minimarket.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // ========== REPORTES EXISTENTES (NO MODIFICAR) ==========

    @Query("""
            SELECT COALESCE(SUM(v.total),0)
            FROM Venta v
            WHERE FUNCTION('DATE', v.fecha) = CURRENT_DATE
            AND v.estado = 'PAGADO'
            """)
    BigDecimal totalVentasHoy();

    @Query("""
            SELECT COUNT(v)
            FROM Venta v
            WHERE FUNCTION('DATE', v.fecha) = CURRENT_DATE
            AND v.estado = 'PAGADO'
            """)
    Long cantidadVentasHoy();

    @Query("SELECT v FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado = 'PAGADO'")
    List<Venta> findVentasDelDia(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(v.total),0) FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado = 'PAGADO'")
    BigDecimal totalVentasDelDia(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    // ========== NUEVAS CONSULTAS PARA REPORTES AVANZADOS ==========

    /**
     * 1. REPORTE SEMANAL - Ventas agrupadas por día
     * Retorna: [fecha, cantidadVentas, totalVentas]
     */
    @Query("""
            SELECT FUNCTION('DATE', v.fecha) as fecha,
                   COUNT(v) as cantidad,
                   SUM(v.total) as total
            FROM Venta v
            WHERE v.fecha BETWEEN :inicio AND :fin
            AND v.estado = 'PAGADO'
            GROUP BY FUNCTION('DATE', v.fecha)
            ORDER BY fecha DESC
            """)
    List<Object[]> findVentasAgrupadasPorDia(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * 2. REPORTE MENSUAL - Ventas agrupadas por mes
     * Retorna: [año, mes, cantidadVentas, totalVentas]
     */
    @Query("""
            SELECT FUNCTION('YEAR', v.fecha) as año,
                   FUNCTION('MONTH', v.fecha) as mes,
                   COUNT(v) as cantidad,
                   SUM(v.total) as total
            FROM Venta v
            WHERE v.estado = 'PAGADO'
            GROUP BY FUNCTION('YEAR', v.fecha), FUNCTION('MONTH', v.fecha)
            ORDER BY año DESC, mes DESC
            """)
    List<Object[]> findVentasAgrupadasPorMes();

    /**
     * 3. REPORTE POR VENDEDOR - Ventas por usuario
     * Retorna: [username, cantidadVentas, totalVentas]
     */
    @Query("""
            SELECT u.username,
                   COUNT(v) as cantidad,
                   SUM(v.total) as total
            FROM Venta v
            JOIN v.vendedor u
            WHERE v.fecha BETWEEN :inicio AND :fin
            AND v.estado = 'PAGADO'
            GROUP BY u.username
            ORDER BY total DESC
            """)
    List<Object[]> findVentasPorVendedor(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * 4. MÉTODOS DE PAGO MÁS USADOS
     * Retorna: [nombreMetodo, cantidadPagos, totalMonto]
     */
    @Query("""
            SELECT mp.nombre,
                   COUNT(p) as cantidad,
                   SUM(p.monto) as total
            FROM Pago p
            JOIN p.metodoPago mp
            WHERE p.fecha BETWEEN :inicio AND :fin
            GROUP BY mp.nombre
            ORDER BY total DESC
            """)
    List<Object[]> findPagosPorMetodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * 5. TOP PRODUCTOS MÁS VENDIDOS
     * Retorna: [productoId, nombre, cantidadVendida, totalVentas]
     */
    @Query("""
            SELECT p.id,
                   p.nombre,
                   SUM(dv.cantidad) as cantidad,
                   SUM(dv.cantidad * dv.precioUnitario) as total
            FROM DetalleVenta dv
            JOIN dv.producto p
            JOIN dv.venta v
            WHERE v.fecha BETWEEN :inicio AND :fin
            AND v.estado = 'PAGADO'
            GROUP BY p.id, p.nombre
            ORDER BY cantidad DESC
            """)
    List<Object[]> findTopProductos(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * 6. RESUMEN GENERAL POR PERÍODO
     * Retorna: [totalVentas, cantidadVentas, ticketPromedio, productoMasVendido]
     */
    @Query("""
            SELECT COALESCE(SUM(v.total), 0),
                   COUNT(v),
                   COALESCE(AVG(v.total), 0)
            FROM Venta v
            WHERE v.fecha BETWEEN :inicio AND :fin
            AND v.estado = 'PAGADO'
            """)
    List<Object[]> findResumenPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * 7. VENTAS POR HORA (para análisis de horarios pico)
     * Retorna: [hora, cantidadVentas, totalVentas]
     */
    @Query("""
            SELECT FUNCTION('HOUR', v.fecha) as hora,
                   COUNT(v) as cantidad,
                   SUM(v.total) as total
            FROM Venta v
            WHERE v.fecha BETWEEN :inicio AND :fin
            AND v.estado = 'PAGADO'
            GROUP BY FUNCTION('HOUR', v.fecha)
            ORDER BY hora
            """)
    List<Object[]> findVentasPorHora(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    /**
     * 8. COMPARATIVA CON PERÍODO ANTERIOR
     * Retorna: [totalActual, totalAnterior, variacion]
     */
    @Query("""
            SELECT COALESCE(SUM(v.total), 0)
            FROM Venta v
            WHERE v.fecha BETWEEN :inicio AND :fin
            AND v.estado = 'PAGADO'
            """)
    BigDecimal findTotalEnPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}