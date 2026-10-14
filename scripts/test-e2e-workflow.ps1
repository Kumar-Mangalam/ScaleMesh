<#
.SYNOPSIS
    ScaleMesh Automated End-to-End Integration Test Suite
.DESCRIPTION
    Validates the complete microservice coordination lifecycle against running Kubernetes services:
    1. Platform Health & Gateway Topology (/api/system/health)
    2. Fleet Telemetry Ingestion & Latest Position Query (/api/telemetry)
    3. Spatial Route Engine Distance & Waypoints Calculation (/api/routes)
    4. Trip Lifecycle: Create -> Dispatch -> Complete (/api/trips)
    5. Demand Engine Spatial Matrix Convolution (/api/demand)
    6. Incident Management: Report -> Resolve (/api/incidents)
    7. Prometheus & Grafana Observability Metrics Propagation
    8. MySQL Multi-Schema Persistence Verification

.PARAMETER GatewayUrl
    Base URL of API Gateway (Default: http://localhost:30081)
.PARAMETER PrometheusUrl
    Base URL of Prometheus (Default: http://localhost:30090)
.PARAMETER GrafanaUrl
    Base URL of Grafana (Default: http://localhost:30300)
.PARAMETER Cleanup
    Optional switch to safely delete ONLY test-created records (TEST-VEH-*, TEST-PASS-*) after verification.
#>

[CmdletBinding()]
param (
    [string]$GatewayUrl = "http://localhost:30081",
    [string]$PrometheusUrl = "http://localhost:30090",
    [string]$GrafanaUrl = "http://localhost:30300",
    [switch]$Cleanup = $false
)

$ErrorActionPreference = "Continue"

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "   ScaleMesh Automated End-to-End Integration Test Suite        " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "Gateway Target:    $GatewayUrl" -ForegroundColor Gray
Write-Host "Prometheus Target: $PrometheusUrl" -ForegroundColor Gray
Write-Host "Grafana Target:    $GrafanaUrl" -ForegroundColor Gray
Write-Host "Cleanup Mode:      $(if ($Cleanup) { 'Enabled (Targeted test records will be pruned)' } else { 'Disabled (Records preserved)' })" -ForegroundColor Gray
Write-Host ""

$testsPassed = 0
$testsFailed = 0
$testResults = [System.Collections.Generic.List[PSCustomObject]]::new()

function Assert-Step {
    param (
        [string]$Name,
        [bool]$Condition,
        [string]$Details
    )
    if ($Condition) {
        Write-Host "  [PASS] $Name" -ForegroundColor Green
        if ($Details) { Write-Host "         $Details" -ForegroundColor DarkGray }
        $script:testsPassed++
        $script:testResults.Add([PSCustomObject]@{ Step = $Name; Status = "PASS"; Details = $Details })
    } else {
        Write-Host "  [FAIL] $Name" -ForegroundColor Red
        if ($Details) { Write-Host "         ERROR: $Details" -ForegroundColor Yellow }
        $script:testsFailed++
        $script:testResults.Add([PSCustomObject]@{ Step = $Name; Status = "FAIL"; Details = $Details })
    }
}

# -----------------------------------------------------------------
# STAGE 1: System Readiness & Gateway Topology Verification
# -----------------------------------------------------------------
Write-Host "[STAGE 1] System Health & Gateway Topology" -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$GatewayUrl/api/system/health" -Method Get -TimeoutSec 10
    $isHealthy = ($healthResponse.data.status -eq "HEALTHY")
    $services = $healthResponse.data.services
    $allUp = ($services.'api-gateway' -eq "UP" -and
              $services.'fleet-telemetry-service' -eq "UP" -and
              $services.'trip-dispatch-service' -eq "UP" -and
              $services.'route-engine-service' -eq "UP" -and
              $services.'demand-analysis-service' -eq "UP" -and
              $services.'incident-manager-service' -eq "UP")

    Assert-Step -Name "Gateway Health Probe" -Condition $isHealthy -Details "Status: $($healthResponse.data.status)"
    Assert-Step -Name "All 6 Microservices UP" -Condition $allUp -Details "Services: $($services | ConvertTo-Json -Compress)"
} catch {
    Assert-Step -Name "Gateway Health Probe" -Condition $false -Details $_.Exception.Message
}

# -----------------------------------------------------------------
# STAGE 2: Fleet Telemetry Ingestion & Query Lifecycle
# -----------------------------------------------------------------
Write-Host "`n[STAGE 2] Fleet Telemetry Ingestion & Query" -ForegroundColor Yellow
$testVehicleId = "TEST-VEH-901"
$telemetryPayload = @{
    vehicleId          = $testVehicleId
    latitude           = 12.9716
    longitude          = 77.5946
    speedKmh           = 48.5
    batteryPercent     = 93.0
    odometerKm         = 14250.0
    status             = "EN_ROUTE"
    tirePressurePsi    = 33.0
    engineTemperatureC = 82.0
} | ConvertTo-Json

try {
    $ingestRes = Invoke-RestMethod -Uri "$GatewayUrl/api/telemetry" -Method Post -Body $telemetryPayload -ContentType "application/json" -TimeoutSec 10
    $ingestSuccess = ($ingestRes.status -eq 201 -and $ingestRes.data.vehicleId -eq $testVehicleId)
    Assert-Step -Name "Ingest Telemetry Event ($testVehicleId)" -Condition $ingestSuccess -Details "Record ID: $($ingestRes.data.id)"

    # Query latest position
    $latestRes = Invoke-RestMethod -Uri "$GatewayUrl/api/telemetry/vehicles/$testVehicleId/latest" -Method Get -TimeoutSec 10
    $latestMatches = ($latestRes.data.vehicleId -eq $testVehicleId -and $latestRes.data.batteryPercent -eq 93.0)
    Assert-Step -Name "Query Latest Telemetry for $testVehicleId" -Condition $latestMatches -Details "Speed: $($latestRes.data.speedKmh) km/h, Battery: $($latestRes.data.batteryPercent)%"

    # Query fleet stats
    $statsRes = Invoke-RestMethod -Uri "$GatewayUrl/api/telemetry/stats" -Method Get -TimeoutSec 10
    $statsValid = ($statsRes.data.totalEvents -ge 1 -and $statsRes.data.activeVehicles -ge 1)
    Assert-Step -Name "Fleet Statistics Aggregation" -Condition $statsValid -Details "Total Events: $($statsRes.data.totalEvents), Active Vehicles: $($statsRes.data.activeVehicles)"
} catch {
    Assert-Step -Name "Telemetry Lifecycle" -Condition $false -Details $_.Exception.Message
}

# -----------------------------------------------------------------
# STAGE 3: Spatial Route Engine Calculation
# -----------------------------------------------------------------
Write-Host "`n[STAGE 3] Spatial Route Engine Calculation" -ForegroundColor Yellow
$routePayload = @{
    originLat = 12.9716
    originLng = 77.5946
    destLat   = 12.9352
    destLng   = 77.6245
} | ConvertTo-Json

try {
    $routeRes = Invoke-RestMethod -Uri "$GatewayUrl/api/routes/calculate" -Method Post -Body $routePayload -ContentType "application/json" -TimeoutSec 10
    $routeValid = ($routeRes.status -eq 200 -and 
                   $routeRes.data.distanceKm -gt 0 -and 
                   $routeRes.data.waypoints.Count -ge 2)
    Assert-Step -Name "Calculate Route & Waypoints" -Condition $routeValid -Details "Distance: $($routeRes.data.distanceKm) km, Duration: $($routeRes.data.estimatedDurationMin) min, Waypoints: $($routeRes.data.waypoints.Count)"
} catch {
    Assert-Step -Name "Calculate Route & Waypoints" -Condition $false -Details $_.Exception.Message
}

# -----------------------------------------------------------------
# STAGE 4: Trip Lifecycle (Create -> Dispatch -> Complete)
# -----------------------------------------------------------------
Write-Host "`n[STAGE 4] Trip Lifecycle Management" -ForegroundColor Yellow
$createdTripId = $null
$tripPayload = @{
    passengerId    = "TEST-PASS-101"
    pickupLat      = 12.9716
    pickupLng      = 77.5946
    dropoffLat     = 12.9352
    dropoffLng     = 77.6245
    priority       = "HIGH"
    passengerCount = 1
} | ConvertTo-Json

try {
    # 4A. Create Trip
    $tripCreateRes = Invoke-RestMethod -Uri "$GatewayUrl/api/trips" -Method Post -Body $tripPayload -ContentType "application/json" -TimeoutSec 10
    $createdTripId = $tripCreateRes.data.tripId
    $tripCreated = ($tripCreateRes.status -eq 201 -and $createdTripId -ne $null -and $tripCreateRes.data.status -eq "REQUESTED")
    Assert-Step -Name "Create Trip Request" -Condition $tripCreated -Details "Trip ID: $createdTripId, Status: $($tripCreateRes.data.status)"

    # 4B. Dispatch Trip to TEST-VEH-901
    $dispatchUrl = "$GatewayUrl/api/trips/$createdTripId/dispatch?vehicleId=$testVehicleId"
    $dispatchRes = Invoke-RestMethod -Uri $dispatchUrl -Method Put -TimeoutSec 10
    $tripDispatched = ($dispatchRes.data.status -eq "DISPATCHED" -and $dispatchRes.data.vehicleId -eq $testVehicleId)
    Assert-Step -Name "Dispatch Trip to $testVehicleId" -Condition $tripDispatched -Details "Status: $($dispatchRes.data.status), Assigned: $($dispatchRes.data.vehicleId)"

    if ($tripDispatched) {
        # 4C. Transition Trip to IN_TRANSIT
        $inTransitUrl = "$GatewayUrl/api/trips/$createdTripId/status?status=IN_TRANSIT"
        $inTransitRes = Invoke-RestMethod -Uri $inTransitUrl -Method Put -TimeoutSec 10
        $tripInTransit = ($inTransitRes.data.status -eq "IN_TRANSIT")
        Assert-Step -Name "Transition Trip to IN_TRANSIT" -Condition $tripInTransit -Details "Trip ID: $createdTripId, Status: $($inTransitRes.data.status)"

        if ($tripInTransit) {
            # 4D. Complete Trip
            $completeUrl = "$GatewayUrl/api/trips/$createdTripId/status?status=COMPLETED"
            $completeRes = Invoke-RestMethod -Uri $completeUrl -Method Put -TimeoutSec 10
            $tripCompleted = ($completeRes.data.status -eq "COMPLETED" -and $completeRes.data.completedAt -ne $null)
            Assert-Step -Name "Complete Trip ($createdTripId)" -Condition $tripCompleted -Details "Status: $($completeRes.data.status), CompletedAt: $($completeRes.data.completedAt)"
        }
    }
} catch {
    Assert-Step -Name "Trip Lifecycle" -Condition $false -Details $_.Exception.Message
}

# -----------------------------------------------------------------
# STAGE 5: Spatial Demand Matrix Computation
# -----------------------------------------------------------------
Write-Host "`n[STAGE 5] Spatial Demand Analysis" -ForegroundColor Yellow
$demandPayload = @{
    regionId       = "test-metro-grid"
    gridSize       = 10
    iterationCount = 500
    cpuBurnMs      = 20
    intensityLevel = 1
} | ConvertTo-Json

try {
    $demandRes = Invoke-RestMethod -Uri "$GatewayUrl/api/demand/compute" -Method Post -Body $demandPayload -ContentType "application/json" -TimeoutSec 15
    $demandValid = ($demandRes.status -eq 200 -and 
                    $demandRes.data.computationId -ne $null -and 
                    $demandRes.data.processedByHost -ne $null)
    Assert-Step -Name "Execute Spatial Demand Convolution" -Condition $demandValid -Details "Host: $($demandRes.data.processedByHost), Fleet Needed: $($demandRes.data.activeVehiclesNeeded), Index: $($demandRes.data.overallDemandIndex)"

    $currentDemandRes = Invoke-RestMethod -Uri "$GatewayUrl/api/demand/current" -Method Get -TimeoutSec 10
    $currentValid = ($currentDemandRes.data.computationId -ne $null)
    Assert-Step -Name "Fetch Latest Demand State" -Condition $currentValid -Details "Region: $($currentDemandRes.data.regionId), Host: $($currentDemandRes.data.processedByHost)"
} catch {
    Assert-Step -Name "Demand Analysis" -Condition $false -Details $_.Exception.Message
}

# -----------------------------------------------------------------
# STAGE 6: Incident Management Lifecycle (Report -> Resolve)
# -----------------------------------------------------------------
Write-Host "`n[STAGE 6] Incident Lifecycle Management" -ForegroundColor Yellow
$createdIncidentId = $null
$incidentPayload = @{
    vehicleId    = $testVehicleId
    incidentType = "SENSOR_FAULT"
    severity     = "LOW"
    description  = "Automated E2E Test - Tire pressure warning detected"
    latitude     = 12.9716
    longitude    = 77.5946
} | ConvertTo-Json

try {
    # 6A. Report Incident
    $incCreateRes = Invoke-RestMethod -Uri "$GatewayUrl/api/incidents" -Method Post -Body $incidentPayload -ContentType "application/json" -TimeoutSec 10
    $createdIncidentId = $incCreateRes.data.incidentId
    $incCreated = ($incCreateRes.status -eq 201 -and $createdIncidentId -ne $null -and $incCreateRes.data.status -eq "OPEN")
    Assert-Step -Name "Report Incident for $testVehicleId" -Condition $incCreated -Details "Incident ID: $createdIncidentId, Severity: $($incCreateRes.data.severity)"

    # 6B. Resolve Incident
    $resolveNotes = "E2E Test Resolution: Depot technician inspected and cleared"
    $resolveUrl = "$GatewayUrl/api/incidents/$createdIncidentId/status?status=RESOLVED&resolutionNotes=" + [System.Uri]::EscapeDataString($resolveNotes)
    $incResolveRes = Invoke-RestMethod -Uri $resolveUrl -Method Put -TimeoutSec 10
    $incResolved = ($incResolveRes.data.status -eq "RESOLVED" -and $incResolveRes.data.resolvedAt -ne $null)
    Assert-Step -Name "Resolve Incident ($createdIncidentId)" -Condition $incResolved -Details "Status: $($incResolveRes.data.status), ResolvedAt: $($incResolveRes.data.resolvedAt)"
} catch {
    Assert-Step -Name "Incident Lifecycle" -Condition $false -Details $_.Exception.Message
}

# -----------------------------------------------------------------
# STAGE 7: Observability Metrics Propagation (Prometheus & Grafana)
# -----------------------------------------------------------------
Write-Host "`n[STAGE 7] Prometheus & Grafana Observability" -ForegroundColor Yellow
try {
    # 7A. Prometheus Target Up Verification
    $promTargets = (Invoke-RestMethod -Uri "$PrometheusUrl/api/v1/targets" -TimeoutSec 10).data.activeTargets
    $allTargetsUp = ($promTargets.Count -eq 6 -and ($promTargets | Where-Object { $_.health -ne "up" }).Count -eq 0)
    Assert-Step -Name "Prometheus Scrapes 6/6 Targets UP" -Condition $allTargetsUp -Details "Active Targets: $($promTargets.Count)/6 UP"

    # 7B. Query Micrometer Metric from Prometheus
    $promQueryRes = Invoke-RestMethod -Uri "$PrometheusUrl/api/v1/query?query=up" -TimeoutSec 10
    $querySuccess = ($promQueryRes.status -eq "success" -and $promQueryRes.data.result.Count -ge 6)
    Assert-Step -Name "Prometheus TSDB Query Execution (up metric)" -Condition $querySuccess -Details "TSDB Metric Vectors: $($promQueryRes.data.result.Count)"

    # 7C. Grafana Datasource Proxy Query Verification
    $grafanaQueryRes = Invoke-RestMethod -Uri "$GrafanaUrl/api/datasources/proxy/1/api/v1/query?query=up" -TimeoutSec 10
    $grafanaSuccess = ($grafanaQueryRes.status -eq "success" -and $grafanaQueryRes.data.result.Count -ge 6)
    Assert-Step -Name "Grafana Datasource Proxy Query to Prometheus" -Condition $grafanaSuccess -Details "Grafana Proxy Result Count: $($grafanaQueryRes.data.result.Count)"
} catch {
    Assert-Step -Name "Observability Stack Query" -Condition $false -Details $_.Exception.Message
}

# -----------------------------------------------------------------
# STAGE 8: MySQL Multi-Schema InnoDB Persistence Check
# -----------------------------------------------------------------
Write-Host "`n[STAGE 8] MySQL Multi-Schema InnoDB Persistence" -ForegroundColor Yellow
try {
    # Verify Telemetry Record in scalemesh_telemetry
    $telemetryDbCheck = kubectl exec -n scalemesh deploy/mysql -- mysql -uroot -pscalemesh_secure_pass_2026 -sN -e "SELECT count(*) FROM scalemesh_telemetry.vehicle_telemetry WHERE vehicle_id = '$testVehicleId';" 2>$null
    $telemetryPersisted = ([int]($telemetryDbCheck.Trim()) -ge 1)
    Assert-Step -Name "Telemetry Persisted to MySQL (scalemesh_telemetry)" -Condition $telemetryPersisted -Details "Records Found for $testVehicleId : $($telemetryDbCheck.Trim())"

    # Verify Trip Record in scalemesh_trips
    if ($createdTripId) {
        $tripDbCheck = kubectl exec -n scalemesh deploy/mysql -- mysql -uroot -pscalemesh_secure_pass_2026 -sN -e "SELECT status FROM scalemesh_trips.trips WHERE id = '$createdTripId';" 2>$null
        $tripPersisted = ($tripDbCheck.Trim() -eq "COMPLETED")
        Assert-Step -Name "Trip Persisted to MySQL (scalemesh_trips)" -Condition $tripPersisted -Details "DB Trip ID: $createdTripId, Persisted Status: $($tripDbCheck.Trim())"
    }

    # Verify Incident Record in scalemesh_incidents
    if ($createdIncidentId) {
        $incDbCheck = kubectl exec -n scalemesh deploy/mysql -- mysql -uroot -pscalemesh_secure_pass_2026 -sN -e "SELECT status FROM scalemesh_incidents.incidents WHERE id = '$createdIncidentId';" 2>$null
        $incPersisted = ($incDbCheck.Trim() -eq "RESOLVED")
        Assert-Step -Name "Incident Persisted to MySQL (scalemesh_incidents)" -Condition $incPersisted -Details "DB Incident ID: $createdIncidentId, Persisted Status: $($incDbCheck.Trim())"
    }
} catch {
    Assert-Step -Name "MySQL Persistence Query" -Condition $false -Details $_.Exception.Message
}

# -----------------------------------------------------------------
# OPTIONAL CLEANUP: Prune ONLY test-created records
# -----------------------------------------------------------------
if ($Cleanup) {
    Write-Host "`n[CLEANUP] Pruning Test Records (Preserving all demonstration data)..." -ForegroundColor Magenta
    if ($testVehicleId) {
        kubectl exec -n scalemesh deploy/mysql -- mysql -uroot -pscalemesh_secure_pass_2026 -e "DELETE FROM scalemesh_telemetry.vehicle_telemetry WHERE vehicle_id = '$testVehicleId';" 2>$null
        Write-Host "  [-] Pruned test telemetry for $testVehicleId" -ForegroundColor Gray
    }
    if ($createdTripId) {
        kubectl exec -n scalemesh deploy/mysql -- mysql -uroot -pscalemesh_secure_pass_2026 -e "DELETE FROM scalemesh_trips.trips WHERE id = '$createdTripId';" 2>$null
        Write-Host "  [-] Pruned test trip $createdTripId" -ForegroundColor Gray
    }
    if ($createdIncidentId) {
        kubectl exec -n scalemesh deploy/mysql -- mysql -uroot -pscalemesh_secure_pass_2026 -e "DELETE FROM scalemesh_incidents.incidents WHERE id = '$createdIncidentId';" 2>$null
        Write-Host "  [-] Pruned test incident $createdIncidentId" -ForegroundColor Gray
    }
} else {
    Write-Host "`n[INFO] Cleanup flag not set. Test records ($testVehicleId, $createdTripId, $createdIncidentId) preserved in MySQL." -ForegroundColor Gray
}

# -----------------------------------------------------------------
# FINAL SUMMARY
# -----------------------------------------------------------------
Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host "                       TEST EXECUTION SUMMARY                    " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "Total Tests: $($testsPassed + $testsFailed) | Passed: $testsPassed | Failed: $testsFailed" -ForegroundColor $(if ($testsFailed -eq 0) { "Green" } else { "Red" })
Write-Host ""
$testResults | Format-Table -AutoSize

if ($testsFailed -eq 0) {
    Write-Host "[SUCCESS] All ScaleMesh end-to-end integration contracts verified successfully!`n" -ForegroundColor Green
    exit 0
} else {
    Write-Host "[FAILURE] $testsFailed test assertion(s) failed. Inspect details above.`n" -ForegroundColor Red
    exit 1
}
