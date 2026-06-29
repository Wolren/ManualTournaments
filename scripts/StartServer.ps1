param(
    [Parameter(Mandatory = $true)]
    [string]$Version,
    [int]$DebugPort = 5005
)

$ErrorActionPreference = "Stop"

$moduleRoot = Split-Path -Parent $PSScriptRoot
$serverDir = Join-Path $moduleRoot "server" "versions" $Version
$pluginSource = Join-Path $moduleRoot "target" "ManualTournaments-1.3.1.jar"

Set-Location $moduleRoot

# Find Maven (check PATH first, then common install locations)
$mvn = Get-Command "mvn.cmd", "mvn" -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty Source
if (-not $mvn) {
    $mvnCandidates = @(
        "C:\tools\maven\apache-maven-3.9.16\bin\mvn.cmd",
        "$env:ProgramFiles\Maven\bin\mvn.cmd",
        "${env:ProgramFiles(x86)}\Maven\bin\mvn.cmd",
        "$env:LOCALAPPDATA\Maven\bin\mvn.cmd"
    )
    $mvn = $mvnCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
}
if (-not $mvn) {
    Write-Error "Maven not found. Install Maven or add it to your PATH."
    exit 1
}
Write-Host "Using Maven: $mvn" -ForegroundColor DarkCyan

# Find JDK 21 (required by Paper API 1.21+)
$jdk21 = "C:\Program Files\Java\jdk-21"
$javaExe = "java"
if (Test-Path $jdk21) {
    $env:JAVA_HOME = $jdk21
    $javaExe = "$jdk21\bin\java.exe"
    Write-Host "Using JDK 21 from $jdk21" -ForegroundColor DarkCyan
} else {
    Write-Warning "JDK 21 not found at $jdk21 — build/run may fail if default JDK < 21"
}

# Build
Write-Host "[1/3] Building plugin..." -ForegroundColor Cyan
& $mvn clean package -DskipTests --no-transfer-progress
if ($LASTEXITCODE -ne 0) { exit 1 }

# Deploy
Write-Host "[2/3] Deploying to server v$Version..." -ForegroundColor Cyan
$pluginsDir = Join-Path $serverDir "plugins"
if (-not (Test-Path $pluginsDir)) { New-Item -ItemType Directory -Path $pluginsDir -Force | Out-Null }
Copy-Item $pluginSource (Join-Path $pluginsDir "ManualTournaments.jar") -Force

# Find server JAR
$serverJar = Get-ChildItem -Path $serverDir -Filter "*.jar" | Where-Object { $_ -notlike "*sources*" -and $_ -notlike "*javadoc*" } | Select-Object -First 1
if (-not $serverJar) {
    Write-Error "No server JAR found in $serverDir — place a Paper/Spigot server jar there first"
    exit 1
}

# Start
Write-Host "[3/3] Starting server v$Version (debug port: $DebugPort)..." -ForegroundColor Green
Write-Host "Attach debugger at localhost:$DebugPort" -ForegroundColor Yellow
& $javaExe "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$DebugPort" -jar $serverJar.FullName nogui
