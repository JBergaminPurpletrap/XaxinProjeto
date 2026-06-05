param(
    [string]$JarPath = "target/xaxin-projeto-qrcode-1.0.0.jar",
    [string]$Profile = "h2",
    [switch]$NoStart,
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Username = "tester",
    [string]$Password = "123",
    [string]$Name = "Tester",
    [string]$ReportPath = "target/e2e-report.md"
)

Function Wait-Url {
    param($url, $timeoutSec=30)
    $i=0
    while ($i -lt $timeoutSec) {
        try { Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 2 | Out-Null; return $true } catch { Start-Sleep -Seconds 1; $i++ }
    }
    return $false
}

if (-not $NoStart) {
    if (-not (Test-Path $JarPath)) { Write-Error "Jar not found: $JarPath"; exit 1 }
    Write-Output "Starting app: java -jar $JarPath --spring.profiles.active=$Profile"
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = "java"
    $startInfo.Arguments = "-jar `"$JarPath`" --spring.profiles.active=$Profile"
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.UseShellExecute = $false
    $proc = New-Object System.Diagnostics.Process
    $proc.StartInfo = $startInfo
    $proc.Start() | Out-Null
    Start-Sleep -Seconds 1
    if (-not (Wait-Url "$BaseUrl/teste.html" 40)) { Write-Error "App did not respond in time"; $proc.Kill(); exit 1 }
    Write-Output "App is up"
} else {
    if (-not (Wait-Url "$BaseUrl/teste.html" 20)) { Write-Error "App not responding at $BaseUrl"; exit 1 }
}
# Register
$body = @{username=$Username; password=$Password; nome=$Name} | ConvertTo-Json
try { Invoke-RestMethod -Uri "$BaseUrl/api/users/cadastro" -Method Post -Body $body -ContentType 'application/json' -ErrorAction SilentlyContinue | ConvertTo-Json | Out-File target\smoke-register-windows.json } catch { }

# Login (support envelope .data.token)
try { $login = Invoke-RestMethod -Uri "$BaseUrl/api/users/login" -Method Post -Body (@{username=$Username; password=$Password} | ConvertTo-Json) -ContentType 'application/json' -ErrorAction Stop; $login | ConvertTo-Json | Out-File target\smoke-login-windows.json } catch { Write-Error 'Login failed'; if ($proc) { $proc.Kill() }; exit 1 }
$token = $null; if ($login -and $login.token) { $token = $login.token } elseif ($login.data -and $login.data.token) { $token = $login.data.token }
Write-Output "Token: $token"
if (-not $token) { Write-Error 'no token'; if ($proc) { $proc.Kill() }; exit 1 }

# Validate checkpoints
$codes = @('1001','2002','3003','4004')
foreach ($c in $codes) {
    try { Invoke-RestMethod -Uri "$BaseUrl/api/qrcode/validar-texto" -Method Post -Body (@{codigo=$c} | ConvertTo-Json) -Headers @{Authorization = "Bearer $token"} -ContentType 'application/json' | ConvertTo-Json | Out-File target\smoke-validate-$c-windows.json } catch { Add-Content -Path target\smoke-validate-$c-windows.json -Value $_.Exception.Message }
}

# Get progresso (validate)
try { $prog = Invoke-RestMethod -Uri "$BaseUrl/api/qrcode/progresso" -Headers @{Authorization = "Bearer $token"} -Method Get; $prog | ConvertTo-Json | Out-File target\smoke-progresso-windows.json } catch { Write-Error 'progresso failed'; if ($proc) { $proc.Kill() }; exit 1 }

# Prepare temp image and upload
$pngB64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII='
$path = Join-Path -Path (Get-Location) -ChildPath "temp_test_image.png"
[IO.File]::WriteAllBytes($path, [Convert]::FromBase64String($pngB64))
try { $upload = curl.exe --silent --show-error --url "$BaseUrl/api/album/upload" -X POST -H "Authorization: Bearer $token" -F "file=@$path"; $upload | Out-File target\smoke-upload-windows.json } catch { Write-Error 'upload failed'; if ($proc) { $proc.Kill() }; exit 1 }
try { $up = $upload | ConvertFrom-Json } catch { $up = $null }

# Apply frame if upload ok
if ($up -ne $null) {
    $fotoId = $up.id
    try { $apply = Invoke-RestMethod -Uri "$BaseUrl/api/album/moldura/$fotoId" -Method Post -Body (@{frame=1} | ConvertTo-Json) -Headers @{Authorization = "Bearer $token"} -ContentType 'application/json' ; $apply | ConvertTo-Json | Out-File target\smoke-frame-windows.json } catch { Add-Content -Path target\smoke-frame-windows.json -Value $_.Exception.Message }
}

# Final album
try { $album = Invoke-RestMethod -Uri "$BaseUrl/api/album" -Headers @{Authorization = "Bearer $token"} -Method Get; $album | ConvertTo-Json | Out-File target\smoke-album-windows.json } catch { Add-Content -Path target\smoke-album-windows.json -Value 'album failed' }

# Write report
$report = @()
$report += "# E2E Test Report - $(Get-Date)"
$report += "\n## Environment"
$report += "Base URL: $BaseUrl"
$report += "Profile: $Profile"
$report += "\n## Registration"
$report += (if (Test-Path target\smoke-register-windows.json) { Get-Content target\smoke-register-windows.json -Raw } else { "Register skipped or not returned" })
$report += "\n## Login Token"
$report += $token
$report += "\n## Progress"
$report += (Get-Content target\smoke-progresso-windows.json -Raw)
$report += "\n## Upload response"
$report += (if (Test-Path target\smoke-upload-windows.json) { Get-Content target\smoke-upload-windows.json -Raw } else { "Upload failed" })
$report += "\n## Apply frame response"
$report += (if (Test-Path target\smoke-frame-windows.json) { Get-Content target\smoke-frame-windows.json -Raw } else { "Apply frame failed or skipped" })
$report += "\n## Final album"
$report += (Get-Content target\smoke-album-windows.json -Raw)

$report | Out-File -FilePath $ReportPath -Encoding utf8
Write-Output "Report written to $ReportPath"

if ($proc) {
    Write-Output "Stopping app (PID $($proc.Id))"
    $proc.Kill()
}

Write-Output "Done"
