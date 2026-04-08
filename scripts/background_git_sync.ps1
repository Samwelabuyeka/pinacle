param(
    [Parameter(Mandatory = $true)]
    [string]$RepoUrl,
    [Parameter(Mandatory = $true)]
    [string]$TargetDir,
    [Parameter(Mandatory = $true)]
    [string]$LogPrefix
)

$ErrorActionPreference = "Stop"

$targetParent = Split-Path -Parent $TargetDir
New-Item -ItemType Directory -Force -Path $targetParent | Out-Null
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $LogPrefix) | Out-Null

$stdout = "${LogPrefix}.out.log"
$stderr = "${LogPrefix}.err.log"

if (Test-Path -LiteralPath $TargetDir) {
    Set-Location -LiteralPath $TargetDir
    git pull 1> $stdout 2> $stderr
} else {
    Set-Location -LiteralPath $targetParent
    git clone $RepoUrl (Split-Path -Leaf $TargetDir) 1> $stdout 2> $stderr
}
