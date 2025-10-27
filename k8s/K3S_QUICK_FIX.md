# K3s Deployment Quick Fix Guide

## Problem Diagnosis

You have:
- ✅ Docker installed and running
- ✅ K3s installed
- ❌ K3s service NOT running
- ⚠️ Kind cluster running (conflicting on port 6443)

## Solution: Start K3s

### Option 1: Quick Start (Recommended)

```bash
# Start K3s service
sudo ./library.sh start-k3s

# Verify it's running
sudo ./library.sh check

# Deploy your application
./deploy.sh up
```

### Option 2: Manual Start

```bash
# Start K3s service
sudo systemctl start k3s
sudo systemctl enable k3s

# Wait for it to be ready
sudo k3s kubectl get nodes

# Configure kubeconfig
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config
chmod 600 ~/.kube/config

# Verify kubectl works
kubectl get nodes

# Deploy
./deploy.sh up
```

### Option 3: Use the Dedicated Script

```bash
# Make executable
chmod +x start-k3s.sh

# Start K3s and load images
sudo ./start-k3s.sh --load-images

# Deploy
./deploy.sh up
```

## Dealing with Kind Cluster (Optional)

If you want to stop the Kind cluster that's currently running:

```bash
# Stop Kind cluster
sudo ./start-k3s.sh --stop-kind --load-images

# Or manually
docker stop apartment-cluster-control-plane
docker rm apartment-cluster-control-plane
# Or completely delete it
kind delete cluster --name apartment-cluster
```

## Verify Everything Works

```bash
# Check K3s status
sudo systemctl status k3s

# Check cluster
kubectl get nodes

# Should show something like:
# NAME       STATUS   ROLES                  AGE   VERSION
# pipatq     Ready    control-plane,master   XXm   v1.33.5+k3s1

# Check if images are loaded (optional)
sudo k3s ctr images ls | grep apartment
```

## Load Images into K3s

K3s uses its own container runtime (containerd), so you need to load your Docker images:

```bash
# Load backend image
docker save apartment-backend:prod | sudo k3s ctr images import -

# Load frontend image
docker save apartment-frontend:prod | sudo k3s ctr images import -

# Verify
sudo k3s ctr images ls | grep apartment
```

Or use the helper script:
```bash
sudo ./start-k3s.sh --load-images
```

## Complete Workflow

```bash
# 1. Install prerequisites (one-time setup)
sudo ./library.sh install-all

# 2. Start K3s
sudo ./library.sh start-k3s

# 3. Check everything is ready
sudo ./library.sh check

# 4. Build and load images (if needed)
./build-images.sh
docker save apartment-backend:prod | sudo k3s ctr images import -
docker save apartment-frontend:prod | sudo k3s ctr images import -

# 5. Deploy!
./deploy.sh up

# 6. Access your application
# Frontend: http://localhost/
# API: http://localhost/api/health
```

## Troubleshooting

### K3s won't start
```bash
# Check logs
sudo journalctl -u k3s -f

# Try restarting
sudo systemctl restart k3s

# Check if port 6443 is in use
sudo netstat -tlnp | grep 6443
```

### kubectl can't connect
```bash
# Reconfigure kubeconfig
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config

# Test connection
kubectl cluster-info
```

### Images not found
```bash
# Check Docker images
docker images | grep apartment

# Load into K3s
docker save apartment-backend:prod | sudo k3s ctr images import -
docker save apartment-frontend:prod | sudo k3s ctr images import -

# Verify in K3s
sudo k3s ctr images ls | grep apartment
```

### Pods stuck in ImagePullBackOff
```bash
# This means K3s can't find the images
# Load them into K3s:
sudo ./start-k3s.sh --load-images

# Or manually:
docker save apartment-backend:prod | sudo k3s ctr images import -
docker save apartment-frontend:prod | sudo k3s ctr images import -
```

## Quick Commands Reference

```bash
# Start K3s
sudo systemctl start k3s

# Stop K3s
sudo systemctl stop k3s

# Restart K3s
sudo systemctl restart k3s

# K3s status
sudo systemctl status k3s

# K3s logs
sudo journalctl -u k3s -f

# Check cluster
kubectl get nodes
kubectl get pods -A

# Deploy
./deploy.sh up

# Check deployment
./deploy.sh status

# View logs
./deploy.sh logs --target backend

# Tear down
./deploy.sh down
```

## Summary

**Your immediate next steps:**

1. Start K3s: `sudo ./library.sh start-k3s`
2. Deploy: `./deploy.sh up`
3. Access: `http://localhost/`

That's it! 🚀
