<#
.SYNOPSIS
  Delete all resources from GKE

.DESCRIPTION
  Removes all application resources from the Kubernetes cluster

.EXAMPLE
  .\delete.ps1

.EXAMPLE
  .\delete.ps1 -Confirm:$false  # Skip confirmation
#>

param(
  [switch]$Force
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
function Write-Error($msg) { Write-Host "✗ $msg" -ForegroundColor Red }

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "`n🗑️  Deleting Resources from GKE" -ForegroundColor Red

if (-not $Force) {
  $confirmation = Read-Host "`nAre you sure you want to delete all resources? (yes/no)"
  if ($confirmation -ne "yes") {
    Write-Host "Deletion cancelled" -ForegroundColor Yellow
    exit 0
  }
}

Write-Section "Deleting Application Resources"

# Delete in reverse order
Write-Info "Deleting ingress..."
kubectl delete -f "$scriptDir\ingress\" --ignore-not-found=true
Write-Success "Ingress deleted"

Write-Info "Deleting frontend..."
kubectl delete -f "$scriptDir\frontend\" --ignore-not-found=true
Write-Success "Frontend deleted"

Write-Info "Deleting backend..."
kubectl delete -f "$scriptDir\backend\" --ignore-not-found=true
Write-Success "Backend deleted"

Write-Info "Deleting database..."
kubectl delete -f "$scriptDir\database\" --ignore-not-found=true
Write-Success "Database deleted"

Write-Info "Deleting namespace..."
kubectl delete -f "$scriptDir\namespace.yaml" --ignore-not-found=true
Write-Success "Namespace deleted"

Write-Section "Cleanup Complete"
Write-Host "All resources have been deleted from the cluster`n" -ForegroundColor Green

Write-Host "Note: The following were NOT deleted:" -ForegroundColor Yellow
Write-Host "  - GKE Cluster (delete manually if needed)" -ForegroundColor White
Write-Host "  - Docker images in GCR (delete manually if needed)" -ForegroundColor White
Write-Host "  - Traefik Ingress Controller (in kube-system namespace)`n" -ForegroundColor White

Write-Host "To delete the GKE cluster:" -ForegroundColor Yellow
Write-Host "  gcloud container clusters delete CLUSTER_NAME --zone ZONE`n" -ForegroundColor White
