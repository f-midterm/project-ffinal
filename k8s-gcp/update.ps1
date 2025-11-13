<#
.SYNOPSIS
  Update specific components in GKE

.DESCRIPTION
  Rebuilds, pushes, and redeploys specific components

.PARAMETER Component
  Component to update: backend, frontend, or all

.PARAMETER ProjectId
  GCP Project ID (optional - auto-detected from gcloud if not provided)

.PARAMETER Tag
  Image tag (default: "prod")

.EXAMPLE
  .\update.ps1 -Component backend

.EXAMPLE
  .\update.ps1 -Component all -Tag "v1.0.1"
#>

param(
  [Parameter(Mandatory=$true)]
  [ValidateSet('backend','frontend','all')]
  [string]$Component,
  
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

function Write-Success($msg) { Write-Host "[OK] $msg" -ForegroundColor Green }
function Write-Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Yellow }

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir

# Auto-detect Project ID if not provided
if (-not $ProjectId) {
  Write-Host "Detecting Project ID from gcloud config..." -ForegroundColor Cyan
  $ProjectId = gcloud config get-value project 2>$null
  if (-not $ProjectId) {
    Write-Host "[ERROR] No Project ID found! Run: gcloud config set project YOUR_PROJECT_ID" -ForegroundColor Red
    exit 1
  }
  Write-Success "Auto-detected Project ID: $ProjectId"
}

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
  kubectl rollout restart -n beliv-apartment deployment/$name
  
  Write-Info "Waiting for rollout to complete..."
  kubectl rollout status -n beliv-apartment deployment/$name --timeout=300s
  
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
kubectl get pods -n beliv-apartment
