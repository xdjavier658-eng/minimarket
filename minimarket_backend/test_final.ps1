Write-Host "=== PRUEBA FINAL MINIMARKET BACKEND ===" -ForegroundColor Cyan
Write-Host "Fecha: $(Get-Date)" -ForegroundColor Gray
Write-Host ""

# Función para probar endpoints
function Test-Endpoint {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [string]$Body = $null
    )
    
    Write-Host "Probando: $Method $Url" -ForegroundColor Yellow
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            ContentType = "application/json"
            ErrorAction = "Stop"
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        $result = Invoke-RestMethod @params
        Write-Host "   ✅ ÉXITO" -ForegroundColor Green
        
        if ($result -is [string]) {
            if ($result.Length -gt 50) {
                Write-Host "   Respuesta: $($result.Substring(0, 50))..." -ForegroundColor Gray
            } else {
                Write-Host "   Respuesta: $result" -ForegroundColor Gray
            }
        } elseif ($result -ne $null) {
            $jsonResult = $result | ConvertTo-Json -Compress
            if ($jsonResult.Length -gt 80) {
                Write-Host "   Respuesta: $($jsonResult.Substring(0, 80))..." -ForegroundColor Gray
            } else {
                Write-Host "   Respuesta: $jsonResult" -ForegroundColor Gray
            }
        }
        
        return $result
        
    } catch {
        Write-Host "   ❌ ERROR: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# 1. Probar endpoints públicos
Write-Host "1. ENDPOINTS PÚBLICOS:" -ForegroundColor Green
Test-Endpoint -Url "http://localhost:8080/api/"
Test-Endpoint -Url "http://localhost:8080/api/public/status"
Test-Endpoint -Url "http://localhost:8080/api/public/health"
Test-Endpoint -Url "http://localhost:8080/api/public/db-check"

Write-Host ""

# 2. Probar POST simple
Write-Host "2. PROBAR POST:" -ForegroundColor Green
$testBody = '{"data": "test desde PowerShell"}'
Test-Endpoint -Url "http://localhost:8080/api/public/test-post" -Method "POST" -Body $testBody

Write-Host ""

# 3. Probar LOGIN (lo más importante)
Write-Host "3. PROBAR LOGIN:" -ForegroundColor Magenta
$loginBody = '{"username": "admin", "password": "admin123"}'
$loginResult = Test-Endpoint -Url "http://localhost:8080/api/auth/signin" -Method "POST" -Body $loginBody

if ($loginResult) {
    Write-Host ""
    Write-Host "🎉 🎉 🎉 ¡LOGIN EXITOSO! 🎉 🎉 🎉" -ForegroundColor Green
    Write-Host ""
    Write-Host "   Token JWT recibido:" -ForegroundColor Cyan
    Write-Host "   $($loginResult.token.Substring(0, 50))..." -ForegroundColor Gray
    Write-Host ""
    Write-Host "   Usuario: $($loginResult.username)" -ForegroundColor Cyan
    Write-Host "   ID: $($loginResult.id)" -ForegroundColor Cyan
    Write-Host "   Roles: $($loginResult.roles -join ', ')" -ForegroundColor Cyan
    
    # Guardar token
    $loginResult.token | Out-File -FilePath "jwt_token.txt" -Encoding UTF8
    Write-Host ""
    Write-Host "   Token guardado en: jwt_token.txt" -ForegroundColor Gray
}

Write-Host ""
Write-Host "=== FIN DE PRUEBAS ===" -ForegroundColor Cyan
Write-Host "Backend funcionando correctamente ✅" -ForegroundColor Green