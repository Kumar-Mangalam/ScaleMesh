param(
    [int]$DurationSeconds = 50,
    [int]$Workers = 3
)

$targetUrl = "http://localhost:30081/api/demand/compute"
$bodyJson = '{"regionId":"metro-hpa-test","gridSize":35,"iterationCount":8000,"cpuBurnMs":150,"intensityLevel":3}'

Write-Host "Starting bounded load test: $Workers workers for $DurationSeconds seconds..."
$stopTime = (Get-Date).AddSeconds($DurationSeconds)

$jobs = @()
for ($i = 0; $i -lt $Workers; $i++) {
    $jobs += Start-Job -ScriptBlock {
        param($url, $body, $end)
        $count = 0
        while ((Get-Date) -lt $end) {
            try {
                $res = Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType "application/json" -TimeoutSec 5
                $count++
            } catch {
                # ignore transient errors
            }
        }
        return $count
    } -ArgumentList $targetUrl, $bodyJson, $stopTime
}

$totalCompleted = 0
foreach ($job in $jobs) {
    $c = Receive-Job -Job $job -Wait
    $totalCompleted += $c
    Remove-Job -Job $job
}

Write-Host "Load test finished. Total requests completed: $totalCompleted"
