package com.minimarket.Dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResumenResponse {
    private BigDecimal ventasHoy;
    private Long cantidadVentasHoy;
    
    // Estadísticas de productos
    private Long totalProductos;
    private Long productosConStock;
    private Long productosStockBajo;
    private Long productosAgotados;
    
    // Lista detallada de productos con problemas
    private List<ProductoStockBajoResponse> productosStockBajoList;
    
    // Usuarios
    private Long usuariosActivos;
    private Long nuevosUsuariosMes;

    public DashboardResumenResponse() {
        this.ventasHoy = BigDecimal.ZERO;
        this.cantidadVentasHoy = 0L;
        this.totalProductos = 0L;
        this.productosConStock = 0L;
        this.productosStockBajo = 0L;
        this.productosAgotados = 0L;
        this.usuariosActivos = 0L;
        this.nuevosUsuariosMes = 0L;
    }

    // Getters y Setters
    public BigDecimal getVentasHoy() { return ventasHoy; }
    public void setVentasHoy(BigDecimal ventasHoy) { this.ventasHoy = ventasHoy; }

    public Long getCantidadVentasHoy() { return cantidadVentasHoy; }
    public void setCantidadVentasHoy(Long cantidadVentasHoy) { this.cantidadVentasHoy = cantidadVentasHoy; }

    public Long getTotalProductos() { return totalProductos; }
    public void setTotalProductos(Long totalProductos) { this.totalProductos = totalProductos; }

    public Long getProductosConStock() { return productosConStock; }
    public void setProductosConStock(Long productosConStock) { this.productosConStock = productosConStock; }

    public Long getProductosStockBajo() { return productosStockBajo; }
    public void setProductosStockBajo(Long productosStockBajo) { this.productosStockBajo = productosStockBajo; }

    public Long getProductosAgotados() { return productosAgotados; }
    public void setProductosAgotados(Long productosAgotados) { this.productosAgotados = productosAgotados; }

    public List<ProductoStockBajoResponse> getProductosStockBajoList() { return productosStockBajoList; }
    public void setProductosStockBajoList(List<ProductoStockBajoResponse> productosStockBajoList) { 
        this.productosStockBajoList = productosStockBajoList; 
    }

    public Long getUsuariosActivos() { return usuariosActivos; }
    public void setUsuariosActivos(Long usuariosActivos) { this.usuariosActivos = usuariosActivos; }

    public Long getNuevosUsuariosMes() { return nuevosUsuariosMes; }
    public void setNuevosUsuariosMes(Long nuevosUsuariosMes) { this.nuevosUsuariosMes = nuevosUsuariosMes; }
}