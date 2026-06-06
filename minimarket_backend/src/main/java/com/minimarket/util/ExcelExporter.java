package com.minimarket.util;

import com.minimarket.dto.reporte.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Exportar reporte de ventas diarias a Excel
     */
    public static byte[] exportarVentasDiarias(
            List<ReporteVentaDiariaDTO> reporte,
            String titulo,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ventas Diarias");

            // Estilos
            CellStyle titleStyle = crearEstiloTitulo(workbook);
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);
            CellStyle currencyStyle = crearEstiloMoneda(workbook);
            CellStyle numberStyle = crearEstiloNumero(workbook);

            // Título del reporte
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titulo);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            // Subtítulo con fechas
            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue(
                    "Período: " + fechaInicio.format(DATE_ONLY_FORMATTER) +
                            " - " + fechaFin.format(DATE_ONLY_FORMATTER));
            subtitleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

            // Fecha de generación
            Row generationRow = sheet.createRow(2);
            Cell generationCell = generationRow.createCell(0);
            generationCell.setCellValue(
                    "Generado: " + LocalDateTime.now().format(DATE_FORMATTER));
            generationCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 3));

            // Espacio
            sheet.createRow(3);

            // Cabeceras
            Row headerRow = sheet.createRow(4);
            String[] headers = { "Fecha", "Cantidad Ventas", "Total Ventas (S/)", "Promedio por Venta" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 5;
            BigDecimal totalGeneral = BigDecimal.ZERO;
            Long cantidadTotal = 0L;

            for (ReporteVentaDiariaDTO venta : reporte) {
                Row row = sheet.createRow(rowNum++);

                // Fecha
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(venta.getFecha().format(DATE_ONLY_FORMATTER));
                dateCell.setCellStyle(dateStyle);

                // Cantidad
                Cell cantidadCell = row.createCell(1);
                cantidadCell.setCellValue(venta.getCantidadVentas());
                cantidadCell.setCellStyle(numberStyle);
                cantidadTotal += venta.getCantidadVentas();

                // Total
                Cell totalCell = row.createCell(2);
                totalCell.setCellValue(venta.getTotalVentas().doubleValue());
                totalCell.setCellStyle(currencyStyle);
                totalGeneral = totalGeneral.add(venta.getTotalVentas());

                // Promedio
                Cell promedioCell = row.createCell(3);
                if (venta.getCantidadVentas() > 0) {
                    BigDecimal promedio = venta.getTotalVentas()
                            .divide(BigDecimal.valueOf(venta.getCantidadVentas()), 2, RoundingMode.HALF_UP);
                    promedioCell.setCellValue(promedio.doubleValue());
                } else {
                    promedioCell.setCellValue(0);
                }
                promedioCell.setCellStyle(currencyStyle);
            }

            // Fila de totales
            Row totalRow = sheet.createRow(rowNum + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("TOTALES:");
            totalLabelCell.setCellStyle(headerStyle);

            Cell totalCantidadCell = totalRow.createCell(1);
            totalCantidadCell.setCellValue(cantidadTotal);
            totalCantidadCell.setCellStyle(headerStyle);

            Cell totalGeneralCell = totalRow.createCell(2);
            totalGeneralCell.setCellValue(totalGeneral.doubleValue());
            totalGeneralCell.setCellStyle(headerStyle);

            // Ajustar ancho de columnas
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escribir a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Exportar top productos a Excel
     */
    public static byte[] exportarTopProductos(
            List<TopProductoDTO> productos,
            String titulo,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Top Productos");

            CellStyle titleStyle = crearEstiloTitulo(workbook);
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle currencyStyle = crearEstiloMoneda(workbook);
            CellStyle numberStyle = crearEstiloNumero(workbook);

            // Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titulo);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            // Período
            Row periodRow = sheet.createRow(1);
            Cell periodCell = periodRow.createCell(0);
            periodCell.setCellValue(
                    "Período: " + fechaInicio.format(DATE_ONLY_FORMATTER) +
                            " - " + fechaFin.format(DATE_ONLY_FORMATTER));
            periodCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

            // Espacio
            sheet.createRow(2);

            // Cabeceras
            Row headerRow = sheet.createRow(3);
            String[] headers = { "ID", "Producto", "Cantidad Vendida", "Total Ventas (S/)", "Precio Promedio" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 4;
            for (TopProductoDTO prod : productos) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(prod.getProductoId());
                row.createCell(1).setCellValue(prod.getNombre());

                Cell cantidadCell = row.createCell(2);
                cantidadCell.setCellValue(prod.getCantidadVendida());
                cantidadCell.setCellStyle(numberStyle);

                Cell totalCell = row.createCell(3);
                totalCell.setCellValue(prod.getTotalVentas().doubleValue());
                totalCell.setCellStyle(currencyStyle);

                Cell promedioCell = row.createCell(4);
                if (prod.getCantidadVendida() > 0) {
                    BigDecimal promedio = prod.getTotalVentas()
                            .divide(BigDecimal.valueOf(prod.getCantidadVendida()), 2, RoundingMode.HALF_UP);
                    promedioCell.setCellValue(promedio.doubleValue());
                } else {
                    promedioCell.setCellValue(0);
                }
                promedioCell.setCellStyle(currencyStyle);
            }

            // Ajustar columnas
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Exportar reporte de inventario a Excel
     */
    public static byte[] exportarInventario(
            List<?> productos,
            Map<String, Object> estadisticas,
            String titulo) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inventario");

            CellStyle titleStyle = crearEstiloTitulo(workbook);
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle numberStyle = crearEstiloNumero(workbook);

            // Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titulo);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // Estadísticas
            Row statsRow1 = sheet.createRow(2);
            statsRow1.createCell(0).setCellValue("Total Productos:");
            statsRow1.createCell(1).setCellValue(estadisticas.get("totalProductos").toString());
            statsRow1.createCell(2).setCellValue("Con Stock:");
            statsRow1.createCell(3).setCellValue(estadisticas.get("productosConStock").toString());
            statsRow1.createCell(4).setCellValue("Stock Bajo:");
            statsRow1.createCell(5).setCellValue(estadisticas.get("productosStockBajo").toString());

            Row statsRow2 = sheet.createRow(3);
            statsRow2.createCell(0).setCellValue("Agotados:");
            statsRow2.createCell(1).setCellValue(estadisticas.get("productosAgotados").toString());

            // Espacio
            sheet.createRow(4);

            // Cabeceras
            Row headerRow = sheet.createRow(5);
            String[] headers = { "ID", "Producto", "Stock", "Stock Mínimo", "Estado", "Precio" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Ajustar columnas
            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // ========== MÉTODOS DE UTILIDAD PARA ESTILOS ==========

    private static CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle crearEstiloCabecera(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("S/ #,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private static CellStyle crearEstiloNumero(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
}