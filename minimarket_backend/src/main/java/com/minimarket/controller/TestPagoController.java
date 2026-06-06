package com.minimarket.controller;

import com.minimarket.entity.*;
import com.minimarket.repository.*;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/test/pagos")
public class TestPagoController {

    private final MetodoPagoRepository metodoPagoRepo;
    private final VentaRepository ventaRepo;
    private final PagoRepository pagoRepo;

    public TestPagoController(
            MetodoPagoRepository metodoPagoRepo,
            VentaRepository ventaRepo,
            PagoRepository pagoRepo) {
        this.metodoPagoRepo = metodoPagoRepo;
        this.ventaRepo = ventaRepo;
        this.pagoRepo = pagoRepo;
    }

    // 1️⃣ Listar métodos de pago
    @GetMapping("/metodos")
    public List<MetodoPago> listarMetodos() {
        return metodoPagoRepo.findAll();
    }

    // 2️⃣ Crear venta simple (sin detalles)
    @PostMapping("/venta")
    public Venta crearVenta(@RequestParam @NonNull Long vendedorId) {
        Venta venta = new Venta();
        venta.setTotal(new BigDecimal("10.00"));
        venta.setEstado("PENDIENTE_PAGO");
        venta.setFecha(LocalDateTime.now());

        Usuario vendedor = new Usuario();
        vendedor.setId(vendedorId);
        venta.setVendedor(vendedor);

        return ventaRepo.save(venta);
    }

    // 3️⃣ Registrar pago
    @PostMapping("/pago")
    public Pago registrarPago(
            @RequestParam @NonNull Long ventaId,
            @RequestParam @NonNull Long metodoPagoId) {
        Venta venta = ventaRepo.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + ventaId));

        MetodoPago metodo = metodoPagoRepo.findById(metodoPagoId)
                .orElseThrow(() -> new RuntimeException("Método de pago no encontrado con ID: " + metodoPagoId));

        Pago pago = new Pago();
        pago.setVenta(venta);
        pago.setMetodoPago(metodo);
        pago.setMonto(venta.getTotal());
        pago.setEstado("CONFIRMADO");
        pago.setFecha(LocalDateTime.now());

        venta.setEstado("PAGADO");

        ventaRepo.save(venta);
        return pagoRepo.save(pago);
    }
}
