Param(
    [Parameter(Mandatory = $false)] [string]$MsUrl = $env:MS_URL,
    [Parameter(Mandatory = $false)] [string]$MsToken = $env:MS_TOKEN,
    [Parameter(Mandatory = $false)] [string]$ProjectId = $env:PROJECT_ID,
    [Parameter(Mandatory = $false)] [string]$ServiceName = $env:SERVICE_NAME,
    [Parameter(Mandatory = $false)] [string]$Branch = $env:BRANCH,
    [Parameter(Mandatory = $false)] [string]$Commit = $env:COMMIT,
    [Parameter(Mandatory = $false)] [string]$ReportFile = $env:REPORT_FILE
)

if (-not $MsUrl -or -not $MsToken -or -not $ProjectId -or -not $ServiceName) {
    Write-Error "Missing required parameters. Provide MS_URL, MS_TOKEN, PROJECT_ID, SERVICE_NAME."
    exit 1
}

if (-not $ReportFile) {
    $ReportFile = "messages-service/target/site/jacoco/jacoco.xml"
}

if (-not (Test-Path $ReportFile)) {
    Write-Error "Report file not found: $ReportFile"
    exit 2
}

$Headers = @{ Authorization = "Bearer $MsToken" }
$Form = @{
  projectId   = $ProjectId
  serviceName = $ServiceName
  branch      = ($Branch | ForEach-Object { if ($_ -and $_.Trim().Length -gt 0) { $_ } else { "unknown" } })
  commit      = ($Commit | ForEach-Object { if ($_ -and $_.Trim().Length -gt 0) { $_ } else { "unknown" } })
  report      = Get-Item $ReportFile
}

$url = "$MsUrl/api/whitebox/upload"
Write-Host "Uploading report to $url ..."
$resp = Invoke-RestMethod -Uri $url -Method Post -Headers $Headers -Form $Form -ErrorAction Stop
Write-Host "Upload success:" ($resp | ConvertTo-Json -Depth 5)

