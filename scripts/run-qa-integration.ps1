param()
Set-StrictMode -Version Latest

# Load .env into environment if present
if (Test-Path -Path .env) {
    Get-Content .env | ForEach-Object {
        if ($_ -match '^\s*([^=]+)=(.*)$') {
            $name = $matches[1]
            $val = $matches[2].Trim()
            Set-Item -Path Env:\$name -Value $val
        }
    }
}

$acct = $env:CLEVERTAP_ACCOUNT_ID
$token = $env:CLEVERTAP_TOKEN
if ([string]::IsNullOrEmpty($acct) -or [string]::IsNullOrEmpty($token)) {
    Write-Host "CLEVERTAP credentials not set; skipping QA integration test."
    exit 0
}

if ($acct.StartsWith('TEST-') -or $token.StartsWith('TEST-')) {
    Write-Host "Detected placeholder CLEVERTAP credentials; skipping QA integration test."
    exit 0
}

Write-Host "Running QA integration test against CleverTap sandbox..."
mvn -Dtest=com.example.clevertap.integration.QAIntegrationTest test
