package com.minimarket.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DebugFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(DebugFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        logger.debug("🔍 Request: {} {} from {}", method, requestURI, httpRequest.getRemoteAddr());
        
        chain.doFilter(request, response);
    }
}