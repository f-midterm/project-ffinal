<#
.SYNOPSIS
  Deploy application to GKE (Google Kubernetes Engine)

.DESCRIPTION
  This script:
  1. Updates deployment files with correct Project ID and Domain
  2. Applies all Kubernetes manifests to GKE
  3. Waits for pods to be ready
  4. Displays access information

.PARAMETER ProjectId
  GCP Project ID (e.g., "muict-project-2025")

.PARAMETER Domain
  Domain name for the application (e.g., "beliv.muict.app")

.PARAMETER Tag
  Image tag (default: "prod")

.PARAMETER SkipImageUpdate
  Skip updating image paths in deployment files

.EXAMPLE
  .\deploy.ps1 -ProjectId "muict-project-2025" -Domain "beliv.muict.app"

.EXAMPLE
  .\deploy.ps1 -ProjectId "muict-project-2025" -Domain "beliv.muict.app" -Tag "v1.0.0"
#>

param(
  [Parameter(Mandatory=$true)]
  [string]$ProjectId,
  
  [Parameter(Mandatory=$true)]
  [string]$Domain,
  
  [string]$Tag = "prod",
  
  [switch]$SkipImageUpdate
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

Write-Host "`n🚀 Deploying to GKE" -ForegroundColor Magenta
Write-Host "Project ID: $ProjectId" -ForegroundColor White
Write-Host "Domain: $Domain" -ForegroundColor White
Write-Host "Tag: $Tag`n" -ForegroundColor White

# Verify kubectl is configured for GKE
Write-Section "Verifying Kubernetes Connection"
try {
  $nodes = kubectl get nodes --no-headers 2>&1
  if ($LASTEXITCODE -ne 0) {
    throw "kubectl not connected to cluster"
  }
  Write-Success "Connected to Kubernetes cluster"
  Write-Host $nodes -ForegroundColor Gray
} catch {
  Write-Error "Not connected to GKE cluster!"
  Write-Host "`nPlease run:" -ForegroundColor Yellow
  Write-Host "  gcloud container clusters get-credentials CLUSTER_NAME --zone ZONE`n" -ForegroundColor White
  exit 1
}

# Update deployment files with Project ID and Domain
if (-not $SkipImageUpdate) {
  Write-Section "Updating Deployment Configurations"
  
  # Update frontend deployment
  $frontendDeploy = "$scriptDir\frontend\deployment.yaml"
  Write-Info "Updating frontend deployment..."
  (Get-Content $frontendDeploy) -replace 'gcr.io/YOUR_PROJECT_ID', "gcr.io/$ProjectId" | Set-Content $frontendDeploy
  Write-Success "Frontend deployment updated"
  
  # Update backend deployment
  $backendDeploy = "$scriptDir\backend\deployment.yaml"
  Write-Info "Updating backend deployment..."
  $content = Get-Content $backendDeploy
  $content = $content -replace 'gcr.io/YOUR_PROJECT_ID', "gcr.io/$ProjectId"
  $content = $content -replace 'value: "\*"  # Allow all origins', "value: `"https://$Domain`""
  $content | Set-Content $backendDeploy
  Write-Success "Backend deployment updated"
  
  # Update ingress
  $ingressFile = "$scriptDir\ingress\ingress-traefik.yaml"
  Write-Info "Updating ingress configuration..."
  (Get-Content $ingressFile) -replace 'beliv.muict.app', $Domain | Set-Content $ingressFile
  Write-Success "Ingress configuration updated"
  
  # Update certificate
  $certFile = "$scriptDir\ingress\certificate.yaml"
  Write-Info "Updating certificate configuration..."
  (Get-Content $certFile) -replace 'beliv.muict.app', $Domain | Set-Content $certFile
  Write-Success "Certificate configuration updated"
}

# Apply namespace
Write-Section "Creating Namespace"
kubectl apply -f "$scriptDir\namespace.yaml"
if ($LASTEXITCODE -ne 0) {
  Write-Error "Failed to create namespace"
  exit 1
}
Write-Success "Namespace created/updated"

# Apply database resources
Write-Section "Deploying Database"
kubectl apply -f "$scriptDir\database\"
if ($LASTEXITCODE -ne 0) {
  Write-Error "Failed to deploy database"
  exit 1
}
Write-Success "Database resources applied"

Write-Info "Waiting for MySQL to be ready..."
kubectl wait --for=condition=ready pod -l component=database -n superproject-ns --timeout=300s
Write-Success "MySQL is ready"

# Apply backend resources
Write-Section "Deploying Backend"
kubectl apply -f "$scriptDir\backend\"
if ($LASTEXITCODE -ne 0) {
  Write-Error "Failed to deploy backend"
  exit 1
}
Write-Success "Backend resources applied"

# Apply frontend resources
Write-Section "Deploying Frontend"
kubectl apply -f "$scriptDir\frontend\"
if ($LASTEXITCODE -ne 0) {
  Write-Error "Failed to deploy frontend"
  exit 1
}
Write-Success "Frontend resources applied"

# Wait for application pods to be ready
Write-Info "Waiting for application pods to be ready..."
kubectl wait --for=condition=ready pod -l component=backend -n superproject-ns --timeout=300s
kubectl wait --for=condition=ready pod -l component=frontend -n superproject-ns --timeout=300s
Write-Success "Application pods are ready"

# Apply ingress resources
Write-Section "Deploying Ingress"
kubectl apply -f "$scriptDir\ingress\"
if ($LASTEXITCODE -ne 0) {
  Write-Error "Failed to deploy ingress"
  exit 1
}
Write-Success "Ingress resources applied"

# Get external IP from Traefik service
Write-Section "Getting External IP"
Write-Info "Fetching Traefik LoadBalancer external IP..."
$maxAttempts = 20
$attempt = 0
$externalIP = ""

while ($attempt -lt $maxAttempts) {
  $externalIP = kubectl get svc -n kube-system traefik -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>$null
  if ($externalIP) {
    break
  }
  $attempt++
  Write-Host "  Waiting for External IP... ($attempt/$maxAttempts)" -ForegroundColor Gray
  Start-Sleep -Seconds 10
}

if ($externalIP) {
  Write-Success "External IP: $externalIP"
} else {
  Write-Error "Failed to get External IP after $maxAttempts attempts"
  Write-Host "Check Traefik service status:" -ForegroundColor Yellow
  Write-Host "  kubectl get svc -n kube-system traefik`n" -ForegroundColor White
}

# Display deployment status
Write-Section "Deployment Status"
kubectl get all -n superproject-ns

# Display access information
Write-Section "Access Information"
Write-Host "Application deployed successfully!`n" -ForegroundColor Green

if ($externalIP) {
  Write-Host "External IP: $externalIP`n" -ForegroundColor White
  
  Write-Host "⚠️  DNS Configuration Required:" -ForegroundColor Yellow
  Write-Host "  Add this A record to your DNS provider:`n" -ForegroundColor White
  Write-Host "  Type: A" -ForegroundColor Gray
  Write-Host "  Name: $Domain" -ForegroundColor Gray
  Write-Host "  Value: $externalIP" -ForegroundColor Gray
  Write-Host "  TTL: 300`n" -ForegroundColor Gray
  
  Write-Host "After DNS is configured, access your application at:" -ForegroundColor Yellow
  Write-Host "  https://$Domain`n" -ForegroundColor White
  
  Write-Host "SSL Certificate Status:" -ForegroundColor Yellow
  Write-Host "  Check with: kubectl describe certificate tls-certificate -n superproject-ns" -ForegroundColor White
  Write-Host "  It may take 2-5 minutes for Let's Encrypt to issue the certificate`n" -ForegroundColor Gray
} else {
  Write-Host "⚠️  External IP not yet assigned" -ForegroundColor Yellow
  Write-Host "  Check status with: kubectl get svc -n kube-system traefik`n" -ForegroundColor White
}

Write-Host "Monitoring commands:" -ForegroundColor Yellow
Write-Host "  kubectl get pods -n superproject-ns" -ForegroundColor White
Write-Host "  kubectl logs -n superproject-ns deployment/backend -f" -ForegroundColor White
Write-Host "  kubectl logs -n superproject-ns deployment/frontend -f`n" -ForegroundColor White
