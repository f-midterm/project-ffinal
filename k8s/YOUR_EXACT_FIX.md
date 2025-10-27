# 🎯 YOUR EXACT PROBLEM & SOLUTION

## What's Happening Right Now

You ran `./deploy.sh up` and got:
```
error: dial tcp 127.0.0.1:6443: connect: connection refused
```

**Why?**

You have **TWO Kubernetes clusters** running at the same time:
1. ✅ **K3s** - Running on port 6443 (what you want to use)
2. ✅ **Kind** - Running on port 45345 (left over from before)

Your `kubectl` command is confused about which one to talk to!

## 🚀 IMMEDIATE FIX (Copy & Paste This)

```bash
cd ~/project-ffinal/k8s

# Make scripts executable
chmod +x switch-cluster.sh quick-deploy.sh

# Switch kubectl to use K3s
sudo ./switch-cluster.sh k3s

# Now deploy
./deploy.sh up
```

**That's it!** This will fix your problem immediately.

## 🎉 Even Better: Use quick-deploy.sh

```bash
./quick-deploy.sh
```

This ONE command automatically:
1. Checks if K3s is running (starts it if needed)
2. Configures kubectl to use K3s (not Kind)
3. Loads Docker images into K3s
4. Deploys everything
5. Shows you the URLs

## 📚 New Scripts I Created for You

### 1. `switch-cluster.sh` - Fix kubectl Connection ⭐ NEW!

This solves your exact problem!

```bash
# Switch kubectl to K3s
sudo ./switch-cluster.sh k3s

# Check which cluster you're using
./switch-cluster.sh check

# Switch to Kind (if you want)
./switch-cluster.sh kind
```

### 2. `library.sh` - Install Everything

```bash
sudo ./library.sh install-all    # Install Docker, K3s, kubectl, jq
sudo ./library.sh start-k3s      # Start K3s service
sudo ./library.sh check          # Check status
```

### 3. `start-k3s.sh` - Start K3s with Options

```bash
sudo ./start-k3s.sh --load-images    # Start K3s and load Docker images
sudo ./start-k3s.sh --stop-kind      # Stop Kind cluster
sudo ./start-k3s.sh --restart        # Restart K3s
```

### 4. `quick-deploy.sh` - ONE Command Deploy

```bash
./quick-deploy.sh    # Does EVERYTHING automatically!
```

### 5. `check-system.sh` - Beautiful Status Check

```bash
./check-system.sh    # Shows what's working and what's not
```

### 6. Updated `deploy.sh` - Smarter Error Detection

Now warns you if kubectl can't connect and tells you exactly what to do!

## 📖 Documentation Files

| File | What It Does |
|------|--------------|
| **FIX_CONNECTION_REFUSED.md** | Complete guide to fix your exact error |
| **QUICK_START.md** | Quick commands for common issues |
| **ONE_COMMAND_DEPLOY.md** | Complete deployment guide |
| **K3S_QUICK_FIX.md** | Troubleshooting guide |

## 🔍 Understanding Your Situation

### What You Have:
- ✅ Docker running
- ✅ K3s installed and running
- ✅ Kind cluster also running
- ❌ kubectl pointing to wrong cluster

### What's Wrong:
Your `~/.kube/config` file is configured for Kind, but the error message shows it's trying to connect to K3s's port (6443).

### The Fix:
Point kubectl to K3s by copying the K3s config:
```bash
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config
```

Or use the script: `sudo ./switch-cluster.sh k3s`

## 📋 Recommended Workflow

### For Your Current Situation:

```bash
# 1. Switch to K3s
sudo ./switch-cluster.sh k3s

# 2. Optional: Stop Kind to avoid confusion
docker stop apartment-cluster-control-plane
docker rm apartment-cluster-control-plane

# 3. Deploy
./deploy.sh up
```

### For Daily Development:

```bash
# Just run this one command every time:
./quick-deploy.sh
```

### To Check Status:

```bash
./check-system.sh
./switch-cluster.sh check
kubectl get nodes
kubectl get pods -n superproject-ns
```

## 🎯 Why You Got This Error

K3s stores its kubeconfig at: `/etc/rancher/k3s/k3s.yaml`
Kind stores its kubeconfig in: `~/.kube/config`

When you started K3s with `sudo ./library.sh start-k3s`, it copied the K3s config to your `~/.kube/config`.

But then somehow (maybe from a previous Kind setup), your kubectl was trying to use a mixed configuration.

The `switch-cluster.sh` script fixes this by cleanly setting up the config for whichever cluster you want to use.

## ✅ Verification Commands

After switching to K3s, verify everything works:

```bash
# Should show K3s node
kubectl get nodes
# Output: pipatq   Ready   control-plane,master

# Should show K3s API
kubectl cluster-info
# Output: Kubernetes control plane is running at https://127.0.0.1:6443

# Check if images are loaded
sudo k3s ctr images ls | grep apartment

# If images are missing, load them:
docker save apartment-backend:prod | sudo k3s ctr images import -
docker save apartment-frontend:prod | sudo k3s ctr images import -
```

## 🚨 If You Still Get Errors

### Error: "connection refused" on port 6443

**Fix:**
```bash
sudo systemctl restart k3s
sleep 5
sudo ./switch-cluster.sh k3s
```

### Error: "ImagePullBackOff" in pods

**Fix:**
```bash
# Load images into K3s
docker save apartment-backend:prod | sudo k3s ctr images import -
docker save apartment-frontend:prod | sudo k3s ctr images import -

# Restart deployments
kubectl rollout restart deployment/backend -n superproject-ns
kubectl rollout restart deployment/frontend -n superproject-ns
```

### Error: Still can't connect

**Fix:**
```bash
# Check what's actually running
sudo systemctl status k3s
docker ps | grep kindest

# Use diagnostic script
./switch-cluster.sh check

# Nuclear option: Clean everything and start fresh
./deploy.sh down
sudo systemctl restart k3s
sudo ./switch-cluster.sh k3s
./deploy.sh up
```

## 💡 Pro Tips

1. **Stick with K3s** - It's simpler for this project
2. **Stop Kind** if you're not using it - Avoids confusion
3. **Use `quick-deploy.sh`** - It handles everything automatically
4. **Check before deploy** - Run `./check-system.sh` first
5. **Load images** - K3s uses containerd, must import Docker images

## 🎉 Summary

**Right now, run:**
```bash
sudo ./switch-cluster.sh k3s
./deploy.sh up
```

**For future deployments:**
```bash
./quick-deploy.sh
```

**To check status:**
```bash
./check-system.sh
```

You're all set! 🚀

---

## Quick Reference Card

```bash
# FIX CONNECTION ISSUES
sudo ./switch-cluster.sh k3s

# ONE-COMMAND DEPLOY
./quick-deploy.sh

# CHECK WHAT'S RUNNING
./check-system.sh
./switch-cluster.sh check

# VIEW LOGS
./deploy.sh logs --target backend

# TEAR DOWN
./deploy.sh down

# RESTART SERVICE
kubectl rollout restart deployment/backend -n superproject-ns
```

**Access after deployment:**
- Frontend: http://localhost/
- API: http://localhost/api/health
