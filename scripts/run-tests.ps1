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

Write-Host "Running mvn test..."
mvn test
