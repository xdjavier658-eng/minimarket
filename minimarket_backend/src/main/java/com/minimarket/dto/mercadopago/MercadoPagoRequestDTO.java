package com.minimarket.dto.mercadopago;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para solicitar pago con Mercado Pago
 */
public class MercadoPagoRequestDTO {
    
    @NotNull(message = "El ID de venta es requerido")
    private Long ventaId;
    
    @NotBlank(message = "El email del comprador es requerido")
    @Email(message = "Email inválido")
    private String emailComprador;
    
    private String nombreComprador;
    
    @Size(max = 20, message = "El tipo de documento no puede exceder los 20 caracteres")
    private String documentoType; // DNI, RUC, etc.
    
    @Size(max = 20, message = "El número de documento no puede exceder los 20 caracteres")
    private String documentoNumero;
    
    private List<ItemMPDTO> items;
    
    // Información adicional
    private String telefono;
    private String direccion;
    
    // Getters y Setters
    public Long getVentaId() {
        return ventaId;
    }
    
    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }
    
    public String getEmailComprador() {
        return emailComprador;
    }
    
    public void setEmailComprador(String emailComprador) {
        this.emailComprador = emailComprador;
    }
    
    public String getNombreComprador() {
        return nombreComprador;
    }
    
    public void setNombreComprador(String nombreComprador) {
        this.nombreComprador = nombreComprador;
    }
    
    public String getDocumentoType() {
        return documentoType;
    }
    
    public void setDocumentoType(String documentoType) {
        this.documentoType = documentoType;
    }
    
    public String getDocumentoNumero() {
        return documentoNumero;
    }
    
    public void setDocumentoNumero(String documentoNumero) {
        this.documentoNumero = documentoNumero;
    }
    
    public List<ItemMPDTO> getItems() {
        return items;
    }
    
    public void setItems(List<ItemMPDTO> items) {
        this.items = items;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}