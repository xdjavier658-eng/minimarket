package com.minimarket.dto.venta;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class FinalizarVentaRequest {

    @NotEmpty
    private List<ItemVentaRequest> items;

    @NotNull
    private Long metodoPagoId;

    private String referencia;

    public List<ItemVentaRequest> getItems() {
        return items;
    }

    public Long getMetodoPagoId() {
        return metodoPagoId;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setItems(List<ItemVentaRequest> items) {
        this.items = items;
    }

    public void setMetodoPagoId(Long metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
