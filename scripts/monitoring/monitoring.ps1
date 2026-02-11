param(
  [ValidateSet("logs", "errors")]
  [string]$Mode = "errors",

  [string]$GrafanaUrl = $env:GRAFANA_URL,
  [string]$GrafanaToken = $env:GRAFANA_TOKEN,
  [string]$LokiDatasourceUid = $env:GRAFANA_LOKI_DS_UID,

  [string]$LogSelector = '{app="item-api"}',
  [string]$FilterRegex = "(timeout|timed out|connection reset|broken pipe|refused|503|504|502)",

  [datetime]$From = (Get-Date).AddHours(-2),
  [datetime]$To = (Get-Date),
  [int]$Limit = 500,
  [int]$IntervalMs = 60000,
  [int]$MaxDataPoints = 2000,

  [string]$OutFile = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Assert-Config {
  if ([string]::IsNullOrWhiteSpace($GrafanaUrl)) { throw "GRAFANA_URL missing." }
  if ([string]::IsNullOrWhiteSpace($GrafanaToken)) { throw "GRAFANA_TOKEN missing." }
  if ([string]::IsNullOrWhiteSpace($LokiDatasourceUid)) { throw "GRAFANA_LOKI_DS_UID missing." }
}

function Get-Headers {
  @{
    "Authorization" = "Bearer $GrafanaToken"
    "Content-Type"  = "application/json"
    "Accept"        = "application/json"
  }
}

function To-EpochMs([datetime]$dt) {
  [DateTimeOffset]::new($dt).ToUnixTimeMilliseconds()
}

function Invoke-GrafanaDsQuery([string]$expr) {
  $uri = "$($GrafanaUrl.TrimEnd('/'))/api/ds/query"
  $body = @{
    from    = (To-EpochMs $From).ToString()
    to      = (To-EpochMs $To).ToString()
    queries = @(
      @{
        refId         = "A"
        datasource    = @{ uid = $LokiDatasourceUid }
        expr          = $expr
        queryType     = "range"
        intervalMs    = $IntervalMs
        maxDataPoints = $MaxDataPoints
        limit         = $Limit
      }
    )
  }

  $json = $body | ConvertTo-Json -Depth 20
  Invoke-RestMethod -Method POST -Uri $uri -Headers (Get-Headers) -Body $json
}

function Write-Result($obj) {
  $json = $obj | ConvertTo-Json -Depth 30
  if ([string]::IsNullOrWhiteSpace($OutFile)) {
    $json
  } else {
    $json | Out-File -FilePath $OutFile -Encoding utf8
    Write-Host "Result written to $OutFile"
  }
}

try {
  Assert-Config

  $expr = switch ($Mode) {
    "logs"   { "$LogSelector" }
    "errors" { "$LogSelector |~ `"$FilterRegex`"" }
  }

  $result = Invoke-GrafanaDsQuery -expr $expr
  Write-Result $result
}
catch {
  Write-Error "Error: $($_.Exception.Message)"
  exit 1
}
