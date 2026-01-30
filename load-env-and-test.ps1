# Load biến môi trường từ file .env
Get-Content .env | ForEach-Object {
    if ($_ -match "^\s*([^#=]+)=(.+)$") {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
    }
}

# In ra thử một biến xem đã có chưa
Write-Host "`n[DEBUG] SPRING_DATASOURCE_URL = $env:SPRING_DATASOURCE_URL"

# Chạy build
mvn clean install
