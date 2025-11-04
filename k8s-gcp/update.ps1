<#
.SYNOPSIS
  Update specific components in GKE

.DESCRIPTION
  Rebuilds, pushes, and redeploys specific components

.PARAMETER Component
  Component to update: backend, frontend, or all

.PARAMETER ProjectId
  GCP Project ID

.PARAMETER Tag
  Image tag (default: "prod")

.EXAMPLE
  .\update.ps1 -Component backend -ProjectId "muict-project-2025"

.EXAMPLE
  .\update.ps1 -Component all -ProjectId "muict-project-2025"
#>

param(
  [Parameter(Mandatory=$true)]
  [ValidateSet('backend','frontend','all')]
  [string]$Component,
  
  [Parameter(Mandatory=$true)]
  [string]$ProjectId,
  
  [string]$Tag = "prod"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Write-Section($msg) { 
  Write-Host "`n============================================" -ForegroundColor Cyan
  Write-Host "  $msg" -ForegroundColor Cyan
  Write-Host "============================================`n" -ForegroundColor Cyan
}

function Write-Success($msg) { Write-Host "✓ $msg" -ForegroundColor Green }
function Write-Info($msg) { Write-Host "→ $msg" -ForegroundColor Yellow }

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir

Write-Host "`n🔄 Updating Component(s)" -ForegroundColor Magenta
Write-Host "Component: $Component" -ForegroundColor White
Write-Host "Project ID: $ProjectId" -ForegroundColor White
Write-Host "Tag: $Tag`n" -ForegroundColor White

function Update-Component {
  param([string]$name)
  
  Write-Section "Updating $name"
  
  # Build and push
  Write-Info "Building $name image..."
  Set-Location "$repoRoot\$name"
  docker build -t apartment-$name`:$Tag -f Dockerfile.prod .
  
  Write-Info "Pushing to GCR..."
  docker tag apartment-$name`:$Tag gcr.io/$ProjectId/apartment-$name`:$Tag
  docker push gcr.io/$ProjectId/apartment-$name`:$Tag
  
  Write-Success "$name image pushed to GCR"
  
  # Restart deployment
  Write-Info "Restarting $name deployment..."
  kubectl rollout restart -n superproject-ns deployment/$name
  
  Write-Info "Waiting for rollout to complete..."
  kubectl rollout status -n superproject-ns deployment/$name --timeout=300s
  
  Write-Success "$name updated successfully"
}

if ($Component -eq 'all') {
  Update-Component 'backend'
  Update-Component 'frontend'
} else {
  Update-Component $Component
}

Set-Location $scriptDir

Write-Section "Update Complete"
Write-Host "Component(s) updated successfully!`n" -ForegroundColor Green
kubectl get pods -n superproject-ns
