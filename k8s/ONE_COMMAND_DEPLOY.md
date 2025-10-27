# One-Command K3s Deployment Guide

This guide shows you how to deploy your application to K3s with just **ONE command**.

## 🚀 Quick Start (First Time Setup)

If this is your first time, run these commands:

```bash
# 1. Make scripts executable
chmod +x library.sh start-k3s.sh quick-deploy.sh deploy.sh

# 2. Install prerequisites (Docker, K3s, kubectl, jq)
sudo ./library.sh install-all

# 3. Start K3s
sudo ./library.sh start-k3s

# 4. Deploy your application
./deploy.sh up
```

## ⚡ One-Command Deploy (After Setup)

Once K3s is installed, just run:

```bash
./quick-deploy.sh
```

This single command will:
- ✅ Check if K3s is running (start it if needed)
- ✅ Load Docker images into K3s
- ✅ Deploy all services (database, backend, frontend, ingress)
- ✅ Wait for everything to be ready
- ✅ Show access URLs

## 📋 Your Current Situation

Based on your output:
- ✅ Docker is installed and running
- ✅ K3s is installed (v1.33.5+k3s1)
- ❌ K3s service is NOT running
- ⚠️ Kind cluster is running (may conflict)

## 🔧 Fix Your Current Issue

### Problem: K3s service not running

```bash
# Start K3s service
sudo ./library.sh start-k3s

# Verify it's running
kubectl get nodes

# Now deploy
./deploy.sh up
```

### Problem: "connection refused" on port 6443

This means K3s isn't running. Start it with:

```bash
sudo systemctl start k3s
```

Or use our helper:

```bash
sudo ./library.sh start-k3s
```

## 📦 Available Scripts

### `library.sh` - Manage Prerequisites

```bash
# Install everything
sudo ./library.sh install-all

# Start K3s service
sudo ./library.sh start-k3s

# Check status
sudo ./library.sh check

# Install specific components
sudo ./library.sh install-docker
sudo ./library.sh install-k3s
sudo ./library.sh install-tools
```

### `start-k3s.sh` - Start K3s with Options

```bash
# Start K3s and load images
sudo ./start-k3s.sh --load-images

# Start K3s and stop Kind cluster
sudo ./start-k3s.sh --stop-kind

# Restart K3s
sudo ./start-k3s.sh --restart
```

### `quick-deploy.sh` - One-Command Deploy

```bash
# Deploy everything (checks K3s, loads images, deploys)
./quick-deploy.sh
```

### `deploy.sh` - Full Deployment Control

```bash
# Deploy everything
./deploy.sh up

# Deploy without building images
./deploy.sh up --skip-build

# Deploy with database reset
./deploy.sh up --reset-database

# Check status
./deploy.sh status

# View logs
./deploy.sh logs --target backend
./deploy.sh logs --target frontend

# Restart a service
./deploy.sh restart --target backend

# Tear down everything
./deploy.sh down
```

## 🎯 Recommended Workflow

### First Time Setup

```bash
# Install and configure everything
sudo ./library.sh install-all
sudo ./library.sh start-k3s
```

### Daily Development

```bash
# Make code changes...

# Build and deploy
./quick-deploy.sh

# Or if images are already built and loaded
./deploy.sh up --skip-build
```

### View Logs

```bash
./deploy.sh logs --target backend
```

### Restart After Code Changes

```bash
# Rebuild images
./build-images.sh

# Load into K3s
docker save apartment-backend:prod | sudo k3s ctr images import -
docker save apartment-frontend:prod | sudo k3s ctr images import -

# Restart deployments
kubectl rollout restart deployment/backend -n superproject-ns
kubectl rollout restart deployment/frontend -n superproject-ns
```

### Clean Slate

```bash
# Tear down everything
./deploy.sh down

# Optional: Reset database
./deploy.sh up --reset-database
```

## 🌐 Access Your Application

After successful deployment:

- **Frontend**: http://localhost/
- **API Health**: http://localhost/api/health
- **Backend Direct**: http://localhost/api/

## 📊 Useful Commands

```bash
# Check K3s status
sudo systemctl status k3s

# Check cluster
kubectl get nodes
kubectl get pods -A

# Check your application
kubectl get pods -n superproject-ns
kubectl get svc -n superproject-ns
kubectl get ingress -n superproject-ns

# K3s logs
sudo journalctl -u k3s -f

# Application logs
kubectl logs -n superproject-ns deployment/backend -f
kubectl logs -n superproject-ns deployment/frontend -f

# Database shell
kubectl exec -it -n superproject-ns mysql-0 -- mysql -u root -prootpassword apartment_rental
```

## 🐛 Troubleshooting

### K3s won't start

```bash
# Check logs
sudo journalctl -u k3s -f

# Try restarting
sudo systemctl restart k3s

# Check port conflicts
sudo netstat -tlnp | grep 6443
```

### kubectl can't connect

```bash
# Reconfigure kubeconfig
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config
kubectl get nodes
```

### Pods stuck in ImagePullBackOff

```bash
# Load images into K3s
docker save apartment-backend:prod | sudo k3s ctr images import -
docker save apartment-frontend:prod | sudo k3s ctr images import -

# Verify
sudo k3s ctr images ls | grep apartment

# Restart pods
kubectl rollout restart deployment/backend -n superproject-ns
kubectl rollout restart deployment/frontend -n superproject-ns
```

### Kind cluster conflicts

```bash
# Stop Kind cluster
docker stop apartment-cluster-control-plane
docker rm apartment-cluster-control-plane

# Or delete completely
kind delete cluster --name apartment-cluster
```

## 🔄 Complete Example

```bash
# === FIRST TIME SETUP ===
chmod +x *.sh
sudo ./library.sh install-all
sudo ./library.sh start-k3s

# === DAILY DEVELOPMENT ===
# 1. Make code changes
vim backend/src/main/java/apartment/...

# 2. Deploy with one command
./quick-deploy.sh

# 3. Check logs
./deploy.sh logs --target backend

# 4. Access application
curl http://localhost/api/health

# === CLEAN UP ===
./deploy.sh down
```

## 💡 Tips

1. **Use `quick-deploy.sh`** for fastest deployment
2. **Use `--skip-build`** if images haven't changed
3. **Check K3s status first** with `sudo systemctl status k3s`
4. **Load images** if you see ImagePullBackOff errors
5. **Use `./deploy.sh down`** before major changes

## 📝 Summary

**Fix your current issue:**
```bash
sudo ./library.sh start-k3s
./deploy.sh up
```

**For future deployments:**
```bash
./quick-deploy.sh
```

That's it! 🎉
