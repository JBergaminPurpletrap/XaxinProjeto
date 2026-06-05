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
try { $reg = Invoke-RestMethod -Uri "$BaseUrl/api/users/cadastro" -Method Post -Body $body -ContentType 'application/json' } catch { $reg = $null }

# Login
$login = @{username=$Username; password=$Password} | ConvertTo-Json
try { $resp = Invoke-RestMethod -Uri "$BaseUrl/api/users/login" -Method Post -Body $login -ContentType 'application/json'; $token = $resp.token } catch { Write-Error "Login failed"; if ($proc) { $proc.Kill() }; exit 1 }

Write-Output "Token: $token"

# Validate checkpoints
$codes = @('1001','2002','3003','4004')
$results = @()
foreach ($c in $codes) {
    $b = @{codigo=$c} | ConvertTo-Json
    try { $r = Invoke-RestMethod -Uri "$BaseUrl/api/qrcode/validar-texto" -Method Post -Body $b -Headers @{Authorization = "Bearer $token"} -ContentType 'application/json'; $results += @{code=$c; result=$r} } catch { $results += @{code=$c; error=$_.Exception.Message} }
}

# Get progresso
$prog = Invoke-RestMethod -Uri "$BaseUrl/api/qrcode/progresso" -Headers @{Authorization = "Bearer $token"} -Method Get

# Prepare temp image
$pngB64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGNgYAAAAAMAASsJTYQAAAAASUVORK5CYII='
$path = Join-Path -Path (Get-Location) -ChildPath "temp_test_image.png"
[IO.File]::WriteAllBytes($path, [Convert]::FromBase64String($pngB64))

# Upload
$upload = curl.exe --silent --show-error --url "$BaseUrl/api/album/upload" -X POST -H "Authorization: Bearer $token" -F "file=@$path"
try { $up = $upload | ConvertFrom-Json } catch { $up = $null }

# Apply frame if upload ok
if ($up -ne $null) {
    $fotoId = $up.id
    $frameBody = @{frame=1} | ConvertTo-Json
    try { $apply = Invoke-RestMethod -Uri "$BaseUrl/api/album/moldura/$fotoId" -Method Post -Body $frameBody -Headers @{Authorization = "Bearer $token"} -ContentType 'application/json' } catch { $apply = $null }
}

# Final album
$album = Invoke-RestMethod -Uri "$BaseUrl/api/album" -Headers @{Authorization = "Bearer $token"} -Method Get

# Write report
$report = @()
$report += "# E2E Test Report - $(Get-Date)"
$report += "\n## Environment"
$report += "Base URL: $BaseUrl"
$report += "Profile: $Profile"
$report += "\n## Registration"
$report += (if ($reg) { "Registered: $($reg.username)" } else { "Register skipped or already existed" })
$report += "\n## Login Token"
$report += $token
$report += "\n## Checkpoint validation results"
foreach ($r in $results) { $report += "$($r.code): $($r.result -join '')" }
$report += "\n## Progress"
$report += (ConvertTo-Json $prog -Depth 5)
$report += "\n## Upload response"
$report += (if ($up) { ConvertTo-Json $up -Depth 5 } else { "Upload failed or returned non-json: $upload" })
$report += "\n## Apply frame response"
$report += (if ($apply) { ConvertTo-Json $apply -Depth 5 } else { "Apply frame failed or skipped" })
$report += "\n## Final album"
$report += (ConvertTo-Json $album -Depth 5)

$report | Out-File -FilePath $ReportPath -Encoding utf8
Write-Output "Report written to $ReportPath"

if ($proc) {
    Write-Output "Stopping app (PID $($proc.Id))"
    $proc.Kill()
}

Write-Output "Done"
