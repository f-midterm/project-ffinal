# 🔧 Fix: kubectl Cannot Connect (Connection Refused)

## Your Exact Problem

```
error: error validating "./namespace.yaml": error validating data: 
failed to download openapi: Get "https://127.0.0.1:6443/openapi/v2?timeout=32s": 
dial tcp 127.0.0.1:6443: connect: connection refused
```

**But you have:**
- ✅ K3s is running
- ✅ Kind cluster is running (on port 45345)
- ❌ kubectl is trying to connect to port 6443 (K3s) but using wrong config

## The Problem Explained

You have **TWO** Kubernetes clusters running:
1. **K3s** - listening on `127.0.0.1:6443`
2. **Kind** - listening on `127.0.0.1:45345`

Your `kubectl` is configured to use Kind's context, but trying to reach K3s. This causes the connection refused error.

## 🚀 Quick Fix (Choose One)

### Option 1: Switch kubectl to K3s (Recommended)

```bash
# Switch to K3s cluster
sudo ./switch-cluster.sh k3s

# Verify
kubectl get nodes
# Should show: pipatq   Ready   control-plane,master

# Now deploy
./deploy.sh up
```

### Option 2: Use quick-deploy.sh (Automatic)

```bash
# This script automatically switches to K3s and deploys
./quick-deploy.sh
```

### Option 3: Manual Fix

```bash
# Copy K3s config to kubectl
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config
chmod 600 ~/.kube/config

# Verify
kubectl get nodes

# Deploy
./deploy.sh up
```

## 📋 Detailed Diagnosis

### Check Current kubectl Context

```bash
# Check which cluster kubectl is using
kubectl config current-context

# Check cluster info
kubectl cluster-info

# If you see port 45345, you're pointing to Kind
# K3s uses port 6443
```

### Check Running Clusters

```bash
# Check K3s
sudo systemctl status k3s
sudo k3s kubectl get nodes

# Check Kind
docker ps | grep kindest
kind get clusters
```

### Use the Diagnostic Script

```bash
chmod +x switch-cluster.sh
./switch-cluster.sh check
```

Output will show:
- Current kubectl context
- Which clusters are running
- How to switch between them

## 🎯 Step-by-Step Solution

### 1. Stop or Choose Your Cluster

**To use K3s (recommended for this project):**

```bash
# Switch kubectl to K3s
sudo ./switch-cluster.sh k3s

# Optional: Stop Kind to avoid confusion
docker stop apartment-cluster-control-plane
docker rm apartment-cluster-control-plane
```

**To use Kind instead:**

```bash
# Switch kubectl to Kind
./switch-cluster.sh kind

# Load images into Kind
kind load docker-image apartment-backend:prod --name apartment-cluster
kind load docker-image apartment-frontend:prod --name apartment-cluster
```

### 2. Verify Connection

```bash
kubectl get nodes
kubectl cluster-info
```

Should show your cluster is accessible.

### 3. Load Images (if using K3s)

```bash
# K3s uses containerd, not Docker
docker save apartment-backend:prod | sudo k3s ctr images import -
docker save apartment-frontend:prod | sudo k3s ctr images import -

# Verify
sudo k3s ctr images ls | grep apartment
```

### 4. Deploy

```bash
./deploy.sh up
```

## 🔄 Switching Between Clusters

### Switch to K3s

```bash
sudo ./switch-cluster.sh k3s
```

### Switch to Kind

```bash
./switch-cluster.sh kind
```

### Check Current Context

```bash
./switch-cluster.sh check
```

## 📊 Understanding the Ports

| Cluster | API Server Port | How to Connect |
|---------|----------------|----------------|
| K3s     | 6443          | Uses `/etc/rancher/k3s/k3s.yaml` |
| Kind    | 45345 (random) | Uses kind context in `~/.kube/config` |

## 🐛 Common Issues

### Issue: "connection refused" on 6443

**Cause:** kubectl config points to K3s but cluster isn't accessible

**Fix:**
```bash
# Check if K3s is actually running
sudo systemctl status k3s

# Reconfigure kubectl
sudo ./switch-cluster.sh k3s
```

### Issue: "connection refused" on 45345

**Cause:** kubectl points to Kind but you want K3s

**Fix:**
```bash
sudo ./switch-cluster.sh k3s
```

### Issue: Can't decide which cluster to use

**For this project, use K3s:**
- ✅ Lighter weight
- ✅ Better for single-node deployments
- ✅ Scripts are optimized for K3s
- ✅ Easier to manage with systemctl

**Use Kind only if:**
- You need multiple clusters
- Testing specific Kubernetes versions
- Need complete isolation

## ✅ Recommended Workflow

```bash
# 1. Always use K3s for this project
sudo ./switch-cluster.sh k3s

# 2. Stop Kind to avoid confusion (optional)
kind delete cluster --name apartment-cluster

# 3. Use quick-deploy for deployments
./quick-deploy.sh

# 4. Check status
./check-system.sh
```

## 🎉 Summary

**Your immediate fix:**

```bash
# 1. Switch kubectl to K3s
sudo ./switch-cluster.sh k3s

# 2. Deploy
./deploy.sh up
```

**For all future deployments:**

```bash
./quick-deploy.sh
```

This will automatically:
- Ensure K3s is running
- Configure kubectl correctly
- Load images
- Deploy everything

Done! 🚀
