package com.minimarket.dto.mercadopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para notificaciones webhook de Mercado Pago
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookNotificationDTO {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("live_mode")
    private Boolean liveMode;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("date_created")
    private String dateCreated;
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("api_version")
    private String apiVersion;
    
    @JsonProperty("action")
    private String action;
    
    @JsonProperty("data")
    private DataDTO data;
    
    public static class DataDTO {
        @JsonProperty("id")
        private String id;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Boolean getLiveMode() {
        return liveMode;
    }
    
    public void setLiveMode(Boolean liveMode) {
        this.liveMode = liveMode;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getApiVersion() {
        return apiVersion;
    }
    
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public DataDTO getData() {
        return data;
    }
    
    public void setData(DataDTO data) {
        this.data = data;
    }
}