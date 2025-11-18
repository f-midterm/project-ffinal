<#
.SYNOPSIS
  One-command deployment to GKE with automatic HTTPS setup

.DESCRIPTION
  Automatically:
  1. Deploy application with HTTP Ingress
  2. Wait for External IP
  3. Create Managed Certificate
  4. Upgrade Ingress to HTTPS
  All in one command!

.PARAMETER ProjectId
  GCP Project ID (optional - auto-detected from gcloud if not provided)

.EXAMPLE
  .\deploy.ps1
  # Auto-detect Project ID from gcloud config
  
.EXAMPLE
  .\deploy.ps1 -ProjectId "my-project-123456"
#>

param(
  [string]$ProjectId
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$Domain = "beliv.pipatpongpri.dev"
$Namespace = "beliv-apartment"

$Namespace = "beliv-apartment"

function Write-Section($msg) { 
  Write-Host "`n============================================" -ForegroundColor Cyan
  Write-Host "  $msg" -ForegroundColor Cyan
  Write-Host "============================================`n" -ForegroundColor Cyan
}

function Write-Success($msg) { Write-Host "[OK] $msg" -ForegroundColor Green }
function Write-Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Yellow }
function Write-Error($msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red }

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Auto-detect Project ID if not provided
if (-not $ProjectId) {
  Write-Host "Detecting Project ID from gcloud config..." -ForegroundColor Cyan
  $ProjectId = gcloud config get-value project 2>$null
  if (-not $ProjectId) {
    Write-Error "No Project ID found! Run: gcloud config set project YOUR_PROJECT_ID"
    exit 1
  }
  Write-Success "Auto-detected Project ID: $ProjectId"
}

Write-Host "`n🚀 One-Command GKE Deployment" -ForegroundColor Magenta
Write-Host "Project: $ProjectId" -ForegroundColor White
Write-Host "Domain: $Domain`n" -ForegroundColor White

# Verify kubectl
Write-Section "Step 1: Verifying Kubernetes Connection"
try {
  $clusterInfo = kubectl cluster-info 2>&1
  if ($LASTEXITCODE -ne 0) { throw "Not connected" }
  Write-Success "Connected to GKE cluster"
} catch {
  Write-Error "Not connected to GKE!"
  Write-Host "Run: gcloud container clusters get-credentials CLUSTER_NAME --region REGION" -ForegroundColor Yellow
  exit 1
}

# Update image paths
Write-Section "Step 2: Updating Image Paths"
$frontendDeploy = "$scriptDir\frontend\deployment.yaml"
$backendDeploy = "$scriptDir\backend\deployment.yaml"

(Get-Content $frontendDeploy) -replace 'gcr.io/[^/]+/', "gcr.io/$ProjectId/" | Set-Content $frontendDeploy
(Get-Content $backendDeploy) -replace 'gcr.io/[^/]+/', "gcr.io/$ProjectId/" | Set-Content $backendDeploy
Write-Success "Images updated to gcr.io/$ProjectId"

# Create namespace
Write-Section "Step 3: Creating Namespace"
kubectl apply -f "$scriptDir\namespace.yaml" | Out-Null
Write-Success "Namespace ready"

# Deploy database
Write-Section "Step 4: Deploying Database"
kubectl apply -f "$scriptDir\database\" | Out-Null
Write-Info "Waiting for MySQL pod..."
kubectl wait --for=condition=ready pod -l component=database -n $Namespace --timeout=300s | Out-Null
Write-Success "Database ready"

# Deploy backend
Write-Section "Step 5: Deploying Backend"
kubectl apply -f "$scriptDir\backend\" | Out-Null
Write-Info "Waiting for backend pods..."
kubectl wait --for=condition=ready pod -l component=backend -n $Namespace --timeout=300s | Out-Null
Write-Success "Backend ready"

# Deploy frontend
Write-Section "Step 6: Deploying Frontend"
kubectl apply -f "$scriptDir\frontend\" | Out-Null
Write-Info "Waiting for frontend pods..."
kubectl wait --for=condition=ready pod -l component=frontend -n $Namespace --timeout=300s | Out-Null
Write-Success "Frontend ready"

# Deploy HTTP-only Ingress first
Write-Section "Step 7: Deploying HTTP Ingress (Getting IP)"
kubectl apply -f "$scriptDir\ingress\ingress-stage1.yaml" | Out-Null
Write-Success "HTTP Ingress deployed"

Write-Info "Waiting for External IP (this may take 5-10 minutes)..."
$maxAttempts = 60
$attempt = 0
$externalIP = ""

while ($attempt -lt $maxAttempts) {
  $externalIP = kubectl get ingress apartment-ingress -n $Namespace -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>$null
  if ($externalIP) { break }
  $attempt++
  if ($attempt % 6 -eq 0) {
    Write-Host "  Still waiting... ($attempt/$maxAttempts)" -ForegroundColor Gray
  }
  Start-Sleep -Seconds 10
}

if (-not $externalIP) {
  Write-Error "Failed to get External IP after 10 minutes"
  Write-Host "Check manually: kubectl get ingress apartment-ingress -n $Namespace" -ForegroundColor Yellow
  exit 1
}

Write-Success "External IP obtained: $externalIP"

# Prompt user to configure DNS
Write-Host "`n" -NoNewline
Write-Host "-------------------------------------------" -ForegroundColor Yellow
Write-Host "  ACTION REQUIRED: Configure DNS" -ForegroundColor Yellow
Write-Host "-------------------------------------------" -ForegroundColor Yellow
Write-Host "Go to Cloudflare (or your DNS provider) and add:" -ForegroundColor White
Write-Host "  Type: A" -ForegroundColor Cyan
Write-Host "  Name: $Domain" -ForegroundColor Cyan  
Write-Host "  Value: $externalIP" -ForegroundColor Cyan
Write-Host "  Proxy: DNS only (gray cloud)" -ForegroundColor Cyan
Write-Host "`nPress Enter after DNS is configured..." -ForegroundColor Yellow
$null = Read-Host

# Verify DNS
Write-Section "Step 8: Verifying DNS Configuration"
Write-Info "Checking DNS propagation..."
$dnsResolved = $false
for ($i = 0; $i -lt 10; $i++) {
  try {
    $resolvedIP = [System.Net.Dns]::GetHostAddresses($Domain)[0].IPAddressToString
    if ($resolvedIP -eq $externalIP) {
      $dnsResolved = $true
      break
    }
  } catch { }
  Start-Sleep -Seconds 3
}

if ($dnsResolved) {
  Write-Success "DNS correctly points to $externalIP"
} else {
  Write-Host "⚠️  DNS not fully propagated yet (this is OK)" -ForegroundColor Yellow
  Write-Host "   Continuing anyway - certificate may take longer to provision" -ForegroundColor Gray
}

# Deploy Managed Certificate
Write-Section "Step 9: Creating SSL Certificate"
kubectl apply -f "$scriptDir\ingress\certificate.yaml" | Out-Null
Write-Success "Managed Certificate created"

# Wait a bit for certificate to initialize
Write-Info "Waiting 30 seconds for certificate initialization..."
Start-Sleep -Seconds 30

# Upgrade to HTTPS Ingress
Write-Section "Step 10: Upgrading to HTTPS Ingress"
kubectl apply -f "$scriptDir\ingress\ingress.yaml" | Out-Null
Write-Success "HTTPS Ingress deployed"

# Check certificate status
Write-Section "Certificate Provisioning Status"
$certStatus = kubectl get managedcertificate apartment-certificate -n $Namespace -o jsonpath='{.status.certificateStatus}' 2>$null

if ($certStatus -eq "Active") {
  Write-Success "Certificate is ACTIVE! 🎉"
  Write-Host "`nYour application is ready at:" -ForegroundColor Green
  Write-Host "  https://$Domain`n" -ForegroundColor White
} else {
  Write-Info "Certificate Status: $certStatus"
  Write-Host "`nCertificate is provisioning (15-60 minutes)" -ForegroundColor Yellow
  Write-Host "Check status:" -ForegroundColor White
  Write-Host "  kubectl describe managedcertificate apartment-certificate -n $Namespace`n" -ForegroundColor Gray
  
  Write-Host "When Status=Active, access:" -ForegroundColor White
  Write-Host "  https://$Domain`n" -ForegroundColor Cyan
}

Write-Section "Deployment Complete!"
Write-Host "HTTP:  http://$Domain (available now)" -ForegroundColor White
Write-Host "HTTPS: https://$Domain (available when cert is Active)`n" -ForegroundColor White

Write-Host "Useful commands:" -ForegroundColor Yellow
Write-Host "  kubectl get pods -n $Namespace" -ForegroundColor Gray
Write-Host "  kubectl get managedcertificate -n $Namespace" -ForegroundColor Gray
Write-Host "  kubectl logs -n $Namespace deployment/backend -f" -ForegroundColor Gray


