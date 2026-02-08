[CmdletBinding()]
param(
    [switch]$DryRun,
    [int]$PollIntervalSeconds = 3,
    [int]$StartupTimeoutSeconds = 300
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

$services = @(
    @{
        Name = "config-server"
        RelativePath = "config-server"
        HealthUrl = "http://localhost:8888/actuator/health"
        LogfileUrl = "http://localhost:8888/actuator/logfile"
        ReadyUrl = "http://localhost:8888/eureka-server/default"
    },
    @{
        Name = "eureka-server"
        RelativePath = "eureka-server"
        HealthUrl = "http://localhost:8761/actuator/health"
        LogfileUrl = "http://localhost:8761/actuator/logfile"
    },
    @{
        Name = "product-stock-service"
        RelativePath = "product-stock-service"
        HealthUrl = "http://localhost:8082/actuator/health"
        LogfileUrl = "http://localhost:8082/actuator/logfile"
    },
    @{
        Name = "shop-management-service"
        RelativePath = "shop-management-service"
        HealthUrl = "http://localhost:8081/actuator/health"
        LogfileUrl = "http://localhost:8081/actuator/logfile"
    },
    @{
        Name = "api-gateway"
        RelativePath = "api-gateway"
        HealthUrl = "http://localhost:8080/actuator/health"
        LogfileUrl = "http://localhost:8080/actuator/logfile"
    }
)

function Test-Maven {
    if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
        throw "Maven command 'mvn' was not found in PATH."
    }
}

function Start-ServiceWindow {
    param(
        [string]$ServiceName,
        [string]$ModulePath
    )

    $command = @"
Set-Location -LiteralPath '$ModulePath'
Write-Host '[${ServiceName}] starting with mvn spring-boot:run' -ForegroundColor Cyan
mvn spring-boot:run
"@

    Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-Command", $command
    ) | Out-Null
}

function Wait-ForHealth {
    param(
        [string]$ServiceName,
        [string]$HealthUrl,
        [int]$TimeoutSeconds,
        [int]$PollSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-RestMethod -Uri $HealthUrl -Method Get -TimeoutSec 5
            if ($null -ne $response.status -and $response.status -eq "UP") {
                Write-Host "[$ServiceName] is UP ($HealthUrl)" -ForegroundColor Green
                return
            }
        } catch {
            # service still starting
        }
        Start-Sleep -Seconds $PollSeconds
    }

    throw "[$ServiceName] did not become healthy within $TimeoutSeconds seconds. Check its terminal window."
}

function Wait-ForHttpSuccess {
    param(
        [string]$Name,
        [string]$Url,
        [int]$TimeoutSeconds,
        [int]$PollSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $Url -Method Get -TimeoutSec 5
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300) {
                Write-Host "[$Name] ready check passed ($Url)" -ForegroundColor Green
                return
            }
        } catch {
            # endpoint not ready yet
        }
        Start-Sleep -Seconds $PollSeconds
    }

    throw "[$Name] ready check failed for $Url within $TimeoutSeconds seconds."
}

function Get-HealthStatus {
    param(
        [string]$HealthUrl
    )

    try {
        $response = Invoke-RestMethod -Uri $HealthUrl -Method Get -TimeoutSec 5
        if ($null -ne $response.status) {
            return [string]$response.status
        }
        return "UNKNOWN"
    } catch {
        return "DOWN"
    }
}

Test-Maven

Write-Host "Repo root: $repoRoot"
Write-Host "Starting services one-by-one in separate terminals..."

foreach ($service in $services) {
    $modulePath = Join-Path $repoRoot $service.RelativePath
    if (-not (Test-Path $modulePath)) {
        throw "Module path not found: $modulePath"
    }

    Write-Host "Launching [$($service.Name)] from [$modulePath]"
    if ($DryRun) {
        Write-Host "DryRun enabled: skipping terminal launch and health check."
        continue
    }

    Start-ServiceWindow -ServiceName $service.Name -ModulePath $modulePath
    Wait-ForHealth -ServiceName $service.Name -HealthUrl $service.HealthUrl -TimeoutSeconds $StartupTimeoutSeconds -PollSeconds $PollIntervalSeconds
    if ($service.ContainsKey("ReadyUrl")) {
        Wait-ForHttpSuccess -Name $service.Name -Url $service.ReadyUrl -TimeoutSeconds $StartupTimeoutSeconds -PollSeconds $PollIntervalSeconds
    }
}

if (-not $DryRun) {
    Write-Host "All services launched successfully." -ForegroundColor Green

    $rows = foreach ($service in $services) {
        [PSCustomObject]@{
            Service    = $service.Name
            Health     = (Get-HealthStatus -HealthUrl $service.HealthUrl)
            HealthUrl  = $service.HealthUrl
            LogfileUrl = $service.LogfileUrl
        }
    }

    Write-Host ""
    Write-Host "Service Access Table" -ForegroundColor Cyan
    $rows | Format-Table -AutoSize
}
