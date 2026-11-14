# Local development launcher. Environment variables take precedence over ignored local.properties.
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$taskProperties = @{}
Get-Content -LiteralPath (Join-Path $PSScriptRoot 'local.properties') | ForEach-Object {
    if ($_ -match '^([A-Z_]+)=(.*)$') { $taskProperties[$matches[1]] = $matches[2].Trim() }
}
foreach ($taskKey in 'DATABASE_URL', 'DATABASE_USER', 'DATABASE_PASSWORD', 'FIREBASE_SERVICE_ACCOUNT_PATH') {
    if (-not [Environment]::GetEnvironmentVariable($taskKey, 'Process')) {
        if (-not $taskProperties[$taskKey]) { throw "Set $taskKey in the environment or local.properties." }
        [Environment]::SetEnvironmentVariable($taskKey, $taskProperties[$taskKey], 'Process')
    }
}
if (-not $env:ADMIN_EMAILS -and $taskProperties['ADMIN_EMAILS']) {
    $env:ADMIN_EMAILS = $taskProperties['ADMIN_EMAILS']
}
if (-not $env:JAVA_HOME) {
    $taskJdk = Get-ChildItem -LiteralPath (Join-Path $env:USERPROFILE '.gradle/jdks') -Directory |
        Where-Object { Test-Path (Join-Path $_.FullName 'bin/java.exe') } | Select-Object -First 1
    if ($taskJdk) { $env:JAVA_HOME = $taskJdk.FullName }
}
& (Join-Path $PSScriptRoot 'gradlew.bat') --no-daemon :server:run --console=plain
exit $LASTEXITCODE
