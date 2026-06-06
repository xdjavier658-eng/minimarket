package com.minimarket.controller;

import com.minimarket.dto.reporte.*;
import com.minimarket.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "http://localhost:8080")
@PreAuthorize("hasAuthority('ADMIN')") // Todos los reportes requieren rol ADMIN
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // ========== REPORTES POR PERÍODO ==========

    /**
     * GET /api/reportes/semanal
     * Reporte de ventas de los últimos 7 días
     */
    @GetMapping("/semanal")
    public ResponseEntity<List<ReporteVentaDiariaDTO>> reporteSemanal() {
        List<ReporteVentaDiariaDTO> reporte = reporteService.generarReporteSemanal();
        return ResponseEntity.ok(reporte);
    }

    /**
     * GET /api/reportes/mensual
     * Reporte de ventas de los últimos 30 días
     */
    @GetMapping("/mensual")
    public ResponseEntity<List<ReporteVentaDiariaDTO>> reporteMensual() {
        List<ReporteVentaDiariaDTO> reporte = reporteService.generarReporteMensual();
        return ResponseEntity.ok(reporte);
    }

    /**
     * GET /api/reportes/rango?inicio=2026-01-01T00:00:00&fin=2026-01-31T23:59:59
     * Reporte de ventas en rango personalizado
     */
    @GetMapping("/rango")
    public ResponseEntity<List<ReporteVentaDiariaDTO>> reportePorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<ReporteVentaDiariaDTO> reporte = reporteService.generarReportePorRango(inicio, fin);
        return ResponseEntity.ok(reporte);
    }

    // ========== REPORTES MENSUALES ==========

    /**
     * GET /api/reportes/mensual-acumulado
     * Todos los meses con sus totales
     */
    @GetMapping("/mensual-acumulado")
    public ResponseEntity<List<ReporteVentaMensualDTO>> reporteMensualAcumulado() {
        List<ReporteVentaMensualDTO> reporte = reporteService.generarReporteMensualAcumulado();
        return ResponseEntity.ok(reporte);
    }

    /**
     * GET /api/reportes/mes/{año}/{mes}
     * Reporte de un mes específico
     * Ejemplo: /api/reportes/mes/2026/2
     */
    @GetMapping("/mes/{año}/{mes}")
    public ResponseEntity<ReporteVentaMensualDTO> reporteMesEspecifico(
            @PathVariable int año,
            @PathVariable int mes) {
        ReporteVentaMensualDTO reporte = reporteService.generarReporteMesEspecifico(año, mes);
        return ResponseEntity.ok(reporte);
    }

    // ========== REPORTES POR VENDEDOR ==========

    /**
     * GET /api/reportes/vendedores?inicio=...&fin=...
     * Ventas por vendedor en un rango
     */
    @GetMapping("/vendedores")
    public ResponseEntity<List<VentaPorVendedorDTO>> reporteVentasPorVendedor(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<VentaPorVendedorDTO> reporte = reporteService.generarReporteVentasPorVendedor(inicio, fin);
        return ResponseEntity.ok(reporte);
    }

    /**
     * GET /api/reportes/vendedores/top?limite=5
     * Top vendedores del mes
     */
    @GetMapping("/vendedores/top")
    public ResponseEntity<List<VentaPorVendedorDTO>> topVendedores(
            @RequestParam(defaultValue = "5") int limite) {
        List<VentaPorVendedorDTO> reporte = reporteService.generarTopVendedores(limite);
        return ResponseEntity.ok(reporte);
    }

    // ========== REPORTES POR MÉTODO DE PAGO ==========

    /**
     * GET /api/reportes/metodos-pago?inicio=...&fin=...
     * Distribución de pagos por método
     */
    @GetMapping("/metodos-pago")
    public ResponseEntity<List<PagoPorMetodoDTO>> reportePagosPorMetodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<PagoPorMetodoDTO> reporte = reporteService.generarReportePagosPorMetodo(inicio, fin);
        return ResponseEntity.ok(reporte);
    }

    // ========== TOP PRODUCTOS ==========

    /**
     * GET /api/reportes/productos/top?inicio=...&fin=...&limite=10
     * Productos más vendidos en un rango
     */
    @GetMapping("/productos/top")
    public ResponseEntity<List<TopProductoDTO>> topProductos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(defaultValue = "10") int limite) {
        List<TopProductoDTO> reporte = reporteService.generarTopProductos(inicio, fin, limite);
        return ResponseEntity.ok(reporte);
    }

    /**
     * GET /api/reportes/productos/top-mes?limite=10
     * Productos más vendidos del mes actual
     */
    @GetMapping("/productos/top-mes")
    public ResponseEntity<List<TopProductoDTO>> topProductosDelMes(
            @RequestParam(defaultValue = "10") int limite) {
        List<TopProductoDTO> reporte = reporteService.generarTopProductosDelMes(limite);
        return ResponseEntity.ok(reporte);
    }

    // ========== ANÁLISIS POR HORA ==========

    /**
     * GET /api/reportes/por-hora?inicio=...&fin=...
     * Análisis de ventas por hora (horarios pico)
     */
    @GetMapping("/por-hora")
    public ResponseEntity<Map<Integer, Map<String, Object>>> reporteVentasPorHora(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        Map<Integer, Map<String, Object>> reporte = reporteService.generarReporteVentasPorHora(inicio, fin);
        return ResponseEntity.ok(reporte);
    }

    // ========== RESUMEN GENERAL ==========

    /**
     * GET /api/reportes/resumen?inicio=...&fin=...
     * Resumen completo de un período
     */
    @GetMapping("/resumen")
    public ResponseEntity<ResumenPeriodoDTO> resumenPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        ResumenPeriodoDTO resumen = reporteService.generarResumenPeriodo(inicio, fin);
        return ResponseEntity.ok(resumen);
    }

    /**
     * GET /api/reportes/resumen/hoy
     * Resumen del día actual
     */
    @GetMapping("/resumen/hoy")
    public ResponseEntity<ResumenPeriodoDTO> resumenHoy() {
        ResumenPeriodoDTO resumen = reporteService.generarResumenDiaActual();
        return ResponseEntity.ok(resumen);
    }

    // ========== COMPARATIVAS ==========

    /**
     * GET /api/reportes/comparativa?inicio1=...&fin1=...&inicio2=...&fin2=...
     * Comparativa entre dos períodos
     */
    @GetMapping("/comparativa")
    public ResponseEntity<Map<String, Object>> comparativaPeriodos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio1,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin1,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio2,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin2) {
        Map<String, Object> comparativa = reporteService.generarComparativaPeriodos(inicio1, fin1, inicio2, fin2);
        return ResponseEntity.ok(comparativa);
    }

    /**
     * GET /api/reportes/comparativa/semanal
     * Comparativa semana actual vs semana anterior
     */
    @GetMapping("/comparativa/semanal")
    public ResponseEntity<Map<String, Object>> comparativaSemanal() {
        Map<String, Object> comparativa = reporteService.generarComparativaSemanal();
        return ResponseEntity.ok(comparativa);
    }

    // ========== ESTADÍSTICAS DE INVENTARIO ==========

    /**
     * GET /api/reportes/inventario/estadisticas
     * Estadísticas completas del inventario
     */
    @GetMapping("/inventario/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticasInventario() {
        Map<String, Object> stats = reporteService.generarEstadisticasInventario();
        return ResponseEntity.ok(stats);
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * GET /api/reportes/periodos/disponibles
     * Lista los períodos predefinidos disponibles
     */
    @GetMapping("/periodos/disponibles")
    public ResponseEntity<Map<String, String>> periodosDisponibles() {
        return ResponseEntity.ok(Map.of(
                "hoy", "Día actual",
                "semana", "Últimos 7 días",
                "mes", "Mes actual (hasta hoy)",
                "mes-completo", "Mes completo actual",
                "año", "Año actual (hasta hoy)"));
    }

    /**
     * POST /api/reportes/periodo/{nombre}
     * Obtener rango de fechas para un período predefinido
     */
    @GetMapping("/periodo/{nombre}")
    public ResponseEntity<Map<String, LocalDateTime>> obtenerRangoPeriodo(
            @PathVariable String nombre) {
        Map<String, LocalDateTime> rango = reporteService.obtenerRangoFechas(nombre);
        return ResponseEntity.ok(rango);
    }

    /**
     * GET /api/reportes/health
     * Verificar que el módulo de reportes está funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "modulo", "Reportes",
                "mensaje", "Módulo de reportes funcionando correctamente"));
    }

    // ========== EXPORTACIÓN A EXCEL ==========

    /**
     * GET /api/reportes/exportar/semanal
     * Exportar reporte semanal a Excel
     */
    @GetMapping("/exportar/semanal")
    public ResponseEntity<byte[]> exportarReporteSemanal() throws IOException {
        byte[] excelBytes = reporteService.generarExcelReporteSemanal();

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=reporte_semanal.xlsx")
                .body(excelBytes);
    }

    /**
     * GET /api/reportes/exportar/mensual
     * Exportar reporte mensual a Excel
     */
    @GetMapping("/exportar/mensual")
    public ResponseEntity<byte[]> exportarReporteMensual() throws IOException {
        byte[] excelBytes = reporteService.generarExcelReporteMensual();

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=reporte_mensual.xlsx")
                .body(excelBytes);
    }

    /**
     * GET /api/reportes/exportar/rango?inicio=...&fin=...
     * Exportar reporte por rango a Excel
     */
    @GetMapping("/exportar/rango")
    public ResponseEntity<byte[]> exportarReportePorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) throws IOException {

        byte[] excelBytes = reporteService.generarExcelReportePorRango(inicio, fin);

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=reporte_rango.xlsx")
                .body(excelBytes);
    }

    /**
     * GET /api/reportes/exportar/top-productos?inicio=...&fin=...&limite=10
     * Exportar top productos a Excel
     */
    @GetMapping("/exportar/top-productos")
    public ResponseEntity<byte[]> exportarTopProductos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(defaultValue = "10") int limite) throws IOException {

        byte[] excelBytes = reporteService.generarExcelTopProductos(inicio, fin, limite);

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=top_productos.xlsx")
                .body(excelBytes);
    }

    /**
     * GET /api/reportes/exportar/inventario
     * Exportar estadísticas de inventario a Excel
     */
    @GetMapping("/exportar/inventario")
    public ResponseEntity<byte[]> exportarInventario() throws IOException {
        byte[] excelBytes = reporteService.generarExcelInventario();

        return ResponseEntity.ok()
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "attachment; filename=inventario.xlsx")
                .body(excelBytes);
    }
}
