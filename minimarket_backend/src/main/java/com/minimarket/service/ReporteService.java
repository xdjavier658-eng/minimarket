package com.minimarket.service;

import com.minimarket.dto.reporte.*;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.util.ExcelExporter;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final VentaRepository ventaRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;

    public ReporteService(VentaRepository ventaRepo,
            ProductoRepository productoRepo,
            UsuarioRepository usuarioRepo) {
        this.ventaRepo = ventaRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    // ========== REPORTES POR PERÍODO ==========

    /**
     * 1. REPORTE SEMANAL - Ventas de los últimos 7 días
     */
    public List<ReporteVentaDiariaDTO> generarReporteSemanal() {
        LocalDateTime fin = LocalDateTime.now();
        LocalDateTime inicio = fin.minusDays(7);

        List<Object[]> resultados = ventaRepo.findVentasAgrupadasPorDia(inicio, fin);

        return resultados.stream()
                .map(ReporteVentaDiariaDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 2. REPORTE MENSUAL - Ventas de los últimos 30 días
     */
    public List<ReporteVentaDiariaDTO> generarReporteMensual() {
        LocalDateTime fin = LocalDateTime.now();
        LocalDateTime inicio = fin.minusDays(30);

        List<Object[]> resultados = ventaRepo.findVentasAgrupadasPorDia(inicio, fin);

        return resultados.stream()
                .map(ReporteVentaDiariaDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 3. REPORTE POR RANGO PERSONALIZADO
     */
    public List<ReporteVentaDiariaDTO> generarReportePorRango(
            LocalDateTime inicio,
            LocalDateTime fin) {

        List<Object[]> resultados = ventaRepo.findVentasAgrupadasPorDia(inicio, fin);

        return resultados.stream()
                .map(ReporteVentaDiariaDTO::new)
                .collect(Collectors.toList());
    }

    // ========== REPORTES MENSUALES ACUMULADOS ==========

    /**
     * 4. REPORTE MENSUAL ACUMULADO (todos los meses)
     */
    public List<ReporteVentaMensualDTO> generarReporteMensualAcumulado() {
        List<Object[]> resultados = ventaRepo.findVentasAgrupadasPorMes();

        return resultados.stream()
                .map(ReporteVentaMensualDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 5. REPORTE DE UN MES ESPECÍFICO
     */
    public ReporteVentaMensualDTO generarReporteMesEspecifico(int año, int mes) {
        LocalDateTime inicio = LocalDateTime.of(año, mes, 1, 0, 0);
        LocalDateTime fin = inicio.plusMonths(1).minusSeconds(1);

        BigDecimal total = ventaRepo.totalVentasDelDia(inicio, fin);
        Long cantidad = (long) ventaRepo.findVentasDelDia(inicio, fin).size();

        return new ReporteVentaMensualDTO(año, mes, cantidad, total);
    }

    // ========== REPORTES POR VENDEDOR ==========

    /**
     * 6. VENTAS POR VENDEDOR EN UN RANGO
     */
    public List<VentaPorVendedorDTO> generarReporteVentasPorVendedor(
            LocalDateTime inicio,
            LocalDateTime fin) {

        List<Object[]> resultados = ventaRepo.findVentasPorVendedor(inicio, fin);

        return resultados.stream()
                .map(VentaPorVendedorDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 7. TOP VENDEDORES DEL MES
     */
    public List<VentaPorVendedorDTO> generarTopVendedores(int limite) {
        LocalDateTime inicio = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.now();

        List<Object[]> resultados = ventaRepo.findVentasPorVendedor(inicio, fin);

        return resultados.stream()
                .map(VentaPorVendedorDTO::new)
                .limit(limite)
                .collect(Collectors.toList());
    }

    // ========== REPORTES POR MÉTODO DE PAGO ==========

    /**
     * 8. PAGOS POR MÉTODO EN UN RANGO
     */
    public List<PagoPorMetodoDTO> generarReportePagosPorMetodo(
            LocalDateTime inicio,
            LocalDateTime fin) {

        List<Object[]> resultados = ventaRepo.findPagosPorMetodo(inicio, fin);

        // Calcular total general para porcentajes
        BigDecimal totalGeneral = resultados.stream()
                .map(row -> (BigDecimal) row[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return resultados.stream()
                .map(row -> {
                    PagoPorMetodoDTO dto = new PagoPorMetodoDTO(row);
                    if (totalGeneral.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal porcentaje = ((BigDecimal) row[2])
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalGeneral, 2, RoundingMode.HALF_UP);
                        dto.setPorcentaje(porcentaje);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ========== TOP PRODUCTOS ==========

    /**
     * 9. PRODUCTOS MÁS VENDIDOS EN UN RANGO
     */
    public List<TopProductoDTO> generarTopProductos(
            LocalDateTime inicio,
            LocalDateTime fin,
            int limite) {

        // Ejecutar consulta
        List<Object[]> resultados = ventaRepo.findTopProductos(inicio, fin);

        // Convertir a DTO y limitar en Java (no en la BD)
        return resultados.stream()
                .map(TopProductoDTO::new)
                .limit(limite)
                .collect(Collectors.toList());
    }

    /**
     * 10. PRODUCTOS MÁS VENDIDOS DEL MES
     */
    public List<TopProductoDTO> generarTopProductosDelMes(int limite) {
        LocalDateTime inicio = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.now();

        return generarTopProductos(inicio, fin, limite);
    }

    // ========== ANÁLISIS POR HORA ==========

    /**
     * 11. VENTAS POR HORA (horarios pico)
     */
    public Map<Integer, Map<String, Object>> generarReporteVentasPorHora(
            LocalDateTime inicio,
            LocalDateTime fin) {

        List<Object[]> resultados = ventaRepo.findVentasPorHora(inicio, fin);

        Map<Integer, Map<String, Object>> reporte = new HashMap<>();

        for (Object[] row : resultados) {
            Integer hora = (Integer) row[0];
            Long cantidad = (Long) row[1];
            BigDecimal total = (BigDecimal) row[2];

            Map<String, Object> datos = new HashMap<>();
            datos.put("hora", hora);
            datos.put("cantidad", cantidad);
            datos.put("total", total);
            datos.put("rango", String.format("%02d:00 - %02d:00", hora, hora + 1));

            reporte.put(hora, datos);
        }

        return reporte;
    }

    // ========== RESUMEN GENERAL ==========

    /**
     * 12. RESUMEN COMPLETO DE UN PERÍODO
     */
    public ResumenPeriodoDTO generarResumenPeriodo(
            LocalDateTime inicio,
            LocalDateTime fin) {

        ResumenPeriodoDTO resumen = new ResumenPeriodoDTO(inicio, fin);

        // Obtener resumen básico
        List<Object[]> resumenBasico = ventaRepo.findResumenPeriodo(inicio, fin);
        if (!resumenBasico.isEmpty()) {
            Object[] row = resumenBasico.get(0);
            resumen.setTotalVentas((BigDecimal) row[0]);
            resumen.setCantidadVentas((Long) row[1]);
            resumen.setTicketPromedio((BigDecimal) row[2]);
        }

        // Obtener top producto del período
        List<TopProductoDTO> topProductos = generarTopProductos(inicio, fin, 1);
        if (!topProductos.isEmpty()) {
            TopProductoDTO top = topProductos.get(0);
            resumen.setProductoMasVendido(top.getNombre());
            resumen.setCantidadProductosVendidos(top.getCantidadVendida());
        }

        return resumen;
    }

    /**
     * 13. RESUMEN DEL DÍA ACTUAL
     */
    public ResumenPeriodoDTO generarResumenDiaActual() {
        LocalDateTime inicio = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime fin = LocalDateTime.now().with(LocalTime.MAX);

        return generarResumenPeriodo(inicio, fin);
    }

    // ========== COMPARATIVAS ==========

    /**
     * 14. COMPARATIVA CON PERÍODO ANTERIOR
     */
    public Map<String, Object> generarComparativaPeriodos(
            LocalDateTime inicio1,
            LocalDateTime fin1,
            LocalDateTime inicio2,
            LocalDateTime fin2) {

        Map<String, Object> comparativa = new HashMap<>();

        BigDecimal total1 = ventaRepo.findTotalEnPeriodo(inicio1, fin1);
        BigDecimal total2 = ventaRepo.findTotalEnPeriodo(inicio2, fin2);

        comparativa.put("periodo1", Map.of(
                "inicio", inicio1,
                "fin", fin1,
                "total", total1));

        comparativa.put("periodo2", Map.of(
                "inicio", inicio2,
                "fin", fin2,
                "total", total2));

        // Calcular variación
        if (total2.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal variacion = total1.subtract(total2)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(total2, 2, RoundingMode.HALF_UP);
            comparativa.put("variacion", variacion);
            comparativa.put("tendencia", variacion.compareTo(BigDecimal.ZERO) > 0 ? "POSITIVA" : "NEGATIVA");
        } else {
            comparativa.put("variacion", BigDecimal.ZERO);
            comparativa.put("tendencia", "SIN DATOS");
        }

        return comparativa;
    }

    /**
     * 15. COMPARATIVA SEMANA ACTUAL VS SEMANA ANTERIOR
     */
    public Map<String, Object> generarComparativaSemanal() {
        LocalDateTime finActual = LocalDateTime.now();
        LocalDateTime inicioActual = finActual.minusDays(7);

        LocalDateTime finAnterior = inicioActual.minusSeconds(1);
        LocalDateTime inicioAnterior = finAnterior.minusDays(7);

        return generarComparativaPeriodos(inicioActual, finActual, inicioAnterior, finAnterior);
    }

    // ========== ESTADÍSTICAS DE INVENTARIO ==========

    /**
     * 16. ESTADÍSTICAS COMPLETAS DE INVENTARIO
     */
    public Map<String, Object> generarEstadisticasInventario() {
        Map<String, Object> stats = new HashMap<>();

        long totalProductos = productoRepo.count();
        long conStock = productoRepo.countProductosConStock();
        long agotados = productoRepo.countProductosAgotados();
        long stockBajo = productoRepo.countProductosStockBajo();

        stats.put("totalProductos", totalProductos);
        stats.put("productosConStock", conStock);
        stats.put("productosAgotados", agotados);
        stats.put("productosStockBajo", stockBajo);

        // Calcular porcentajes
        if (totalProductos > 0) {
            stats.put("porcentajeConStock",
                    BigDecimal.valueOf(conStock * 100).divide(BigDecimal.valueOf(totalProductos), 2,
                            RoundingMode.HALF_UP));
            stats.put("porcentajeAgotados",
                    BigDecimal.valueOf(agotados * 100).divide(BigDecimal.valueOf(totalProductos), 2,
                            RoundingMode.HALF_UP));
            stats.put("porcentajeStockBajo",
                    BigDecimal.valueOf(stockBajo * 100).divide(BigDecimal.valueOf(totalProductos), 2,
                            RoundingMode.HALF_UP));
        }

        return stats;
    }

    // ========== MÉTODOS DE UTILIDAD ==========

    /**
     * Obtener rango de fechas para diferentes períodos
     */
    public Map<String, LocalDateTime> obtenerRangoFechas(String periodo) {
        Map<String, LocalDateTime> rango = new HashMap<>();
        LocalDateTime ahora = LocalDateTime.now();

        switch (periodo.toLowerCase()) {
            case "hoy":
                rango.put("inicio", ahora.with(LocalTime.MIN));
                rango.put("fin", ahora.with(LocalTime.MAX));
                break;

            case "semana":
                rango.put("inicio", ahora.minusDays(7));
                rango.put("fin", ahora);
                break;

            case "mes":
                rango.put("inicio", ahora.withDayOfMonth(1).with(LocalTime.MIN));
                rango.put("fin", ahora);
                break;

            case "mes-completo":
                rango.put("inicio", ahora.withDayOfMonth(1).with(LocalTime.MIN));
                rango.put("fin", ahora.withDayOfMonth(ahora.toLocalDate().lengthOfMonth())
                        .with(LocalTime.MAX));
                break;

            case "año":
                rango.put("inicio", ahora.withDayOfYear(1).with(LocalTime.MIN));
                rango.put("fin", ahora);
                break;

            default:
                throw new IllegalArgumentException("Período no válido: " + periodo);
        }

        return rango;
    }

    // ========== MÉTODOS PARA EXPORTACIÓN ==========

    /**
     * Generar reporte semanal en Excel
     */
    public byte[] generarExcelReporteSemanal() throws IOException {
        List<ReporteVentaDiariaDTO> reporte = generarReporteSemanal();
        LocalDateTime fin = LocalDateTime.now();
        LocalDateTime inicio = fin.minusDays(7);

        return ExcelExporter.exportarVentasDiarias(
                reporte,
                "REPORTE SEMANAL DE VENTAS",
                inicio,
                fin);
    }

    /**
     * Generar reporte mensual en Excel
     */
    public byte[] generarExcelReporteMensual() throws IOException {
        List<ReporteVentaDiariaDTO> reporte = generarReporteMensual();
        LocalDateTime fin = LocalDateTime.now();
        LocalDateTime inicio = fin.minusDays(30);

        return ExcelExporter.exportarVentasDiarias(
                reporte,
                "REPORTE MENSUAL DE VENTAS",
                inicio,
                fin);
    }

    /**
     * Generar reporte por rango en Excel
     */
    public byte[] generarExcelReportePorRango(
            LocalDateTime inicio,
            LocalDateTime fin) throws IOException {
        List<ReporteVentaDiariaDTO> reporte = generarReportePorRango(inicio, fin);

        return ExcelExporter.exportarVentasDiarias(
                reporte,
                "REPORTE DE VENTAS POR RANGO",
                inicio,
                fin);
    }

    /**
     * Generar top productos en Excel
     */
    public byte[] generarExcelTopProductos(
            LocalDateTime inicio,
            LocalDateTime fin,
            int limite) throws IOException {
        List<TopProductoDTO> top = generarTopProductos(inicio, fin, limite);

        return ExcelExporter.exportarTopProductos(
                top,
                "TOP " + limite + " PRODUCTOS MÁS VENDIDOS",
                inicio,
                fin);
    }

    /**
     * Generar estadísticas de inventario en Excel
     */
    public byte[] generarExcelInventario() throws IOException {
        Map<String, Object> stats = generarEstadisticasInventario();
        List<?> productosConProblemas = productoRepo.findProductosConProblemasStock();

        return ExcelExporter.exportarInventario(
                productosConProblemas,
                stats,
                "REPORTE DE INVENTARIO");
    }

}