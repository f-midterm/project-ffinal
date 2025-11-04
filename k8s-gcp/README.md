# 🌐 Kubernetes Manifests for GCP Deployment

This directory contains Kubernetes manifests specifically configured for **Google Cloud Platform (GCP)** deployment.

## 📁 Directory Structure

```
k8s-gcp/
├── README.md                      # This file
├── namespace.yaml                 # Namespace definition
├── build-and-push.ps1            # Build Docker images and push to GCR
├── deploy.ps1                    # Deploy everything to GKE
├── delete.ps1                    # Delete all resources
├── update.ps1                    # Update specific components
├── database/                      # Database manifests (GCP storage)
│   ├── secret.yaml               # MySQL credentials
│   ├── configmap.yaml            # init.sql script
│   ├── pvc.yaml                  # PVC with GCP Persistent Disk
│   ├── statefulset.yaml          # MySQL StatefulSet
│   └── service.yaml              # MySQL Service
├── backend/                       # Backend manifests (GCR images)
│   ├── deployment.yaml           # Backend Deployment
│   └── service.yaml              # Backend Service
├── frontend/                      # Frontend manifests (GCR images)
│   ├── deployment.yaml           # Frontend Deployment
│   └── service.yaml              # Frontend Service
├── ingress/                       # Ingress with SSL/TLS
│   ├── ingress-traefik.yaml      # Traefik Ingress with real domain
│   └── certificate.yaml          # Let's Encrypt SSL Certificate
└── monitoring/                    # Monitoring stack (optional)
    ├── prometheus-ingress.yaml   # Prometheus Ingress
    └── grafana-ingress.yaml      # Grafana Ingress
```

---

## 🔑 Key Differences from `k8s/` (Localhost)

| Feature | `k8s/` (Localhost) | `k8s-gcp/` (GCP) |
|---------|-------------------|------------------|
| **Images** | `apartment-frontend:prod` | `gcr.io/PROJECT_ID/apartment-frontend:prod` |
| **Image Pull Policy** | `Never` | `Always` or `IfNotPresent` |
| **Domain** | `apartment.local` | `beliv.muict.app` |
| **Storage Class** | `hostPath` or default | `standard-rwo` (GCP Persistent Disk) |
| **SSL/TLS** | None | Let's Encrypt (cert-manager) |
| **Load Balancer** | NodePort or Port Forward | GCP Load Balancer (External IP) |
| **Ingress Controller** | Local Traefik | GKE Traefik with LoadBalancer |

---

## 📋 Prerequisites

### 1. **GCP Project Setup**
```powershell
# Set your GCP Project ID
$PROJECT_ID = "muict-project-2025"  # Replace with actual Project ID

# Login to GCP
gcloud auth login

# Set project
gcloud config set project $PROJECT_ID

# Enable required APIs
gcloud services enable container.googleapis.com
gcloud services enable containerregistry.googleapis.com
```

### 2. **Tools Required**
- ✅ `gcloud` CLI - [Install](https://cloud.google.com/sdk/docs/install)
- ✅ `kubectl` - [Install](https://kubernetes.io/docs/tasks/tools/)
- ✅ `helm` - [Install](https://helm.sh/docs/intro/install/)
- ✅ `docker` - Already installed

### 3. **Domain Name**
- Wait for instructor to provide: `beliv.muict.app`
- You'll need to update this in `ingress/` files

---

## 🚀 Quick Start

### **Step 1: Update Configuration**

Edit these variables in the files:
```powershell
# In all deployment files
$PROJECT_ID = "YOUR_GCP_PROJECT_ID"

# In ingress files
$DOMAIN = "beliv.muict.app"  # Or your assigned domain
```

### **Step 2: Build and Push Docker Images**

```powershell
# Build and push to Google Container Registry
.\k8s-gcp\build-and-push.ps1 -ProjectId "muict-project-2025"
```

### **Step 3: Create GKE Cluster**

```powershell
# Create a GKE cluster (one-time setup)
gcloud container clusters create beliv-cluster `
  --zone asia-southeast1-a `
  --num-nodes 3 `
  --machine-type e2-medium `
  --disk-size 30 `
  --enable-autorepair `
  --enable-autoupgrade

# Connect kubectl to the cluster
gcloud container clusters get-credentials beliv-cluster `
  --zone asia-southeast1-a
```

### **Step 4: Install Traefik Ingress Controller**

```powershell
# Add Traefik Helm repository
helm repo add traefik https://traefik.github.io/charts
helm repo update

# Install Traefik with LoadBalancer
helm install traefik traefik/traefik `
  --namespace kube-system `
  --set service.type=LoadBalancer
```

### **Step 5: Deploy Application**

```powershell
# Deploy everything
.\k8s-gcp\deploy.ps1 -ProjectId "muict-project-2025" -Domain "beliv.muict.app"
```

### **Step 6: Get External IP**

```powershell
# Get the external IP from Traefik service
kubectl get svc -n kube-system traefik

# Example output:
# NAME      TYPE           EXTERNAL-IP      PORT(S)
# traefik   LoadBalancer   34.87.123.45     80:30080/TCP, 443:30443/TCP
```

### **Step 7: Configure DNS**

Point your domain to the External IP:
```
Type: A Record
Name: beliv.muict.app
Value: 34.87.123.45  (Your External IP)
TTL: 300
```

### **Step 8: Install cert-manager (SSL)**

```powershell
# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# Wait for cert-manager to be ready
kubectl wait --for=condition=ready pod -l app.kubernetes.io/instance=cert-manager -n cert-manager --timeout=300s

# Apply certificate configuration
kubectl apply -f k8s-gcp/ingress/certificate.yaml
```

---

## 🛠️ Management Commands

### **Check Status**
```powershell
# Check all resources
kubectl get all -n superproject-ns

# Check pods status
kubectl get pods -n superproject-ns

# Check ingress
kubectl get ingress -n superproject-ns

# Check SSL certificate status
kubectl get certificate -n superproject-ns
```

### **View Logs**
```powershell
# Backend logs
kubectl logs -n superproject-ns deployment/backend -f

# Frontend logs
kubectl logs -n superproject-ns deployment/frontend -f

# Database logs
kubectl logs -n superproject-ns statefulset/mysql -f
```

### **Update Components**
```powershell
# Update backend
.\k8s-gcp\update.ps1 -Component backend -ProjectId "muict-project-2025"

# Update frontend
.\k8s-gcp\update.ps1 -Component frontend -ProjectId "muict-project-2025"

# Restart a deployment
kubectl rollout restart -n superproject-ns deployment/backend
```

### **Delete Everything**
```powershell
# Delete all resources
.\k8s-gcp\delete.ps1

# Delete GKE cluster (careful!)
gcloud container clusters delete beliv-cluster --zone asia-southeast1-a
```

---

## 🔐 SSL/TLS Certificate

### Automatic SSL with Let's Encrypt

The SSL certificate is automatically managed by cert-manager:

1. **ClusterIssuer** (`certificate.yaml`) configured for Let's Encrypt
2. **Certificate** resource automatically requests SSL cert
3. **Ingress** annotation tells cert-manager to manage SSL
4. Certificate auto-renews before expiry

Check certificate status:
```powershell
kubectl describe certificate tls-certificate -n superproject-ns
```

---

## 🌐 Access Your Application

After deployment:

- **Frontend**: `https://beliv.muict.app`
- **Backend API**: `https://beliv.muict.app/api`
- **Prometheus**: `https://prometheus.beliv.muict.app` (if monitoring enabled)
- **Grafana**: `https://grafana.beliv.muict.app` (if monitoring enabled)

---

## 📊 Monitoring (Optional)

If you want to deploy Prometheus and Grafana:

```powershell
# Deploy monitoring stack
cd monitor
.\deploy-monitoring.ps1

# Apply monitoring ingress
kubectl apply -f k8s-gcp/monitoring/prometheus-ingress.yaml
kubectl apply -f k8s-gcp/monitoring/grafana-ingress.yaml
```

---

## 💰 Cost Estimation

### GKE Cluster (3 nodes, e2-medium)
- **VM Cost**: ~$73/month
- **Persistent Disk**: ~$2/month (20GB)
- **Load Balancer**: ~$18/month
- **Network Egress**: ~$5-10/month

**Total**: ~$98-103/month

### Free Tier
- GCP offers $300 credit for 90 days for new accounts
- Use this for your project deployment

---

## 🆘 Troubleshooting

### Pods not starting
```powershell
kubectl describe pod -n superproject-ns <pod-name>
kubectl logs -n superproject-ns <pod-name>
```

### Image pull errors
```powershell
# Check if images exist in GCR
gcloud container images list --project=PROJECT_ID

# Re-push images
.\k8s-gcp\build-and-push.ps1 -ProjectId "PROJECT_ID"
```

### External IP pending
```powershell
# Check service status
kubectl describe svc -n kube-system traefik

# Check quota
gcloud compute project-info describe --project=PROJECT_ID | Select-String -Pattern "EXTERNAL"
```

### Domain not working
```powershell
# Check DNS propagation
nslookup beliv.muict.app

# Check ingress
kubectl describe ingress -n superproject-ns
```

### SSL Certificate not issuing
```powershell
# Check certificate status
kubectl describe certificate tls-certificate -n superproject-ns

# Check cert-manager logs
kubectl logs -n cert-manager deployment/cert-manager -f
```

---

## 📚 Additional Resources

- [GCP Documentation](https://cloud.google.com/docs)
- [GKE Quickstart](https://cloud.google.com/kubernetes-engine/docs/quickstart)
- [Traefik Ingress](https://doc.traefik.io/traefik/providers/kubernetes-ingress/)
- [cert-manager Documentation](https://cert-manager.io/docs/)

---

## ⚠️ Important Notes

1. **Do NOT commit** sensitive data (passwords, API keys) to Git
2. **Always use Secrets** for sensitive information
3. **Monitor costs** in GCP Console regularly
4. **Backup database** before making changes
5. **Test locally** (`k8s/`) before deploying to GCP

---

## 🎯 Deployment Checklist

Before deploying to GCP:

- [ ] Received GCP Project ID from instructor
- [ ] Received Domain name (beliv.muict.app)
- [ ] Updated Project ID in all files
- [ ] Updated Domain in ingress files
- [ ] Built and pushed Docker images to GCR
- [ ] Created GKE cluster
- [ ] Installed Traefik Ingress Controller
- [ ] Deployed application
- [ ] Got External IP
- [ ] Configured DNS A record
- [ ] Installed cert-manager
- [ ] SSL certificate issued successfully
- [ ] Tested application access via HTTPS

---

**For questions or issues, refer to `DEPLOYMENT_GUIDE.md` in the root directory.**
