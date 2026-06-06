package com.minimarket.Dashboard.Service;

import com.minimarket.Dashboard.dto.DashboardResumenResponse;
import com.minimarket.Dashboard.dto.ProductoStockBajoResponse;
import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final VentaRepository ventaRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;

    public DashboardService(VentaRepository ventaRepo,
            ProductoRepository productoRepo,
            UsuarioRepository usuarioRepo) {
        this.ventaRepo = ventaRepo;
        this.productoRepo = productoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    public DashboardResumenResponse obtenerResumen() {
        DashboardResumenResponse response = new DashboardResumenResponse();

        try {
            // 1. Ventas del día
            BigDecimal totalHoy = ventaRepo.totalVentasHoy();
            Long cantidadHoy = ventaRepo.cantidadVentasHoy();
            response.setVentasHoy(totalHoy != null ? totalHoy : BigDecimal.ZERO);
            response.setCantidadVentasHoy(cantidadHoy != null ? cantidadHoy : 0L);

            // 2. Estadísticas de productos usando los nuevos métodos del repositorio
            response.setTotalProductos(productoRepo.count());
            response.setProductosConStock(productoRepo.countProductosConStock());
            response.setProductosAgotados(productoRepo.countProductosAgotados());
            response.setProductosStockBajo(productoRepo.countProductosStockBajo());

            // 3. Lista detallada de productos con problemas (stock bajo o agotado)
            List<ProductoStockBajoResponse> productosProblema = productoRepo.findProductosConProblemasStock()
                    .stream()
                    .map(this::convertirAProductoStockBajoResponse)
                    .collect(Collectors.toList());
            response.setProductosStockBajoList(productosProblema);

            // 4. Usuarios activos
            response.setUsuariosActivos(usuarioRepo.count());

            // 5. Nuevos usuarios del mes (opcional - necesitarías implementar este método)
            // response.setNuevosUsuariosMes(usuarioRepo.countNuevosEsteMes());

        } catch (Exception e) {
            logger.error("Error al obtener resumen del dashboard: " + e.getMessage(), e);
            // Valores por defecto en caso de error
            response.setVentasHoy(BigDecimal.ZERO);
            response.setCantidadVentasHoy(0L);
            response.setTotalProductos(0L);
            response.setProductosConStock(0L);
            response.setProductosAgotados(0L);
            response.setProductosStockBajo(0L);
            response.setUsuariosActivos(0L);
        }

        return response;
    }

    private ProductoStockBajoResponse convertirAProductoStockBajoResponse(Producto producto) {
        ProductoStockBajoResponse dto = new ProductoStockBajoResponse();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setStock(producto.getStock());
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setCategoria(producto.getCategoria());
        dto.setPrecio(producto.getPrecio());

        // Determinar estado
        if (producto.getStock() == 0) {
            dto.setEstado("AGOTADO");
        } else if (producto.getStock() <= producto.getStockMinimo()) {
            dto.setEstado("STOCK BAJO");
        } else {
            dto.setEstado("NORMAL");
        }

        return dto;
    }

    // Método adicional para obtener solo estadísticas (sin la lista detallada)
    public Map<String, Long> obtenerEstadisticasProductos() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", productoRepo.count());
        stats.put("conStock", productoRepo.countProductosConStock());
        stats.put("stockBajo", productoRepo.countProductosStockBajo());
        stats.put("agotados", productoRepo.countProductosAgotados());
        return stats;
    }
}