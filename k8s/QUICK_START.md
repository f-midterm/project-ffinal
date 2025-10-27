# 🚀 Fix Your Deployment NOW

## Your Problem

```
K3s service: not running
error: dial tcp 127.0.0.1:6443: connect: connection refused
```

## The Solution (Copy & Paste)

```bash
# 1. Start K3s service
sudo ./library.sh start-k3s

# 2. Deploy everything
./deploy.sh up

# 3. Access your app
# Frontend: http://localhost/
```

That's it! ✅

---

## Alternative: Use the Ultra-Quick Deploy

```bash
chmod +x quick-deploy.sh
./quick-deploy.sh
```

This ONE command:
- ✅ Starts K3s if needed
- ✅ Loads images
- ✅ Deploys everything
- ✅ Shows you the URLs

---

## What These Scripts Do

### `library.sh` - Install & Manage K3s
```bash
sudo ./library.sh install-all    # Install Docker, K3s, kubectl
sudo ./library.sh start-k3s      # Start K3s service
sudo ./library.sh check          # Check what's installed
```

### `start-k3s.sh` - Advanced K3s Control
```bash
sudo ./start-k3s.sh --load-images    # Start + load images
sudo ./start-k3s.sh --stop-kind      # Stop Kind, start K3s
```

### `quick-deploy.sh` - One Command Deploy
```bash
./quick-deploy.sh    # Does everything automatically
```

### `deploy.sh` - Full Control (Original)
```bash
./deploy.sh up          # Deploy everything
./deploy.sh status      # Check status
./deploy.sh logs --target backend    # View logs
./deploy.sh down        # Tear down
```

---

## Daily Workflow

```bash
# Morning: Start everything
sudo systemctl start k3s
./deploy.sh up

# Make changes to code...

# Deploy changes
./quick-deploy.sh

# Check logs
./deploy.sh logs --target backend

# Evening: Stop everything (optional)
./deploy.sh down
sudo systemctl stop k3s
```

---

## Troubleshooting

### "connection refused"
```bash
sudo systemctl start k3s
```

### "ImagePullBackOff"
```bash
docker save apartment-backend:prod | sudo k3s ctr images import -
docker save apartment-frontend:prod | sudo k3s ctr images import -
kubectl rollout restart deployment/backend -n superproject-ns
```

### "K3s not installed"
```bash
sudo ./library.sh install-all
sudo ./library.sh start-k3s
```

---

## Useful Commands

```bash
# Check K3s
sudo systemctl status k3s
kubectl get nodes

# Check your app
kubectl get pods -n superproject-ns
kubectl get svc -n superproject-ns

# Logs
kubectl logs -n superproject-ns deployment/backend -f

# Restart
kubectl rollout restart deployment/backend -n superproject-ns
```

---

## Documentation

- 📄 **ONE_COMMAND_DEPLOY.md** - Complete guide
- 📄 **K3S_QUICK_FIX.md** - Troubleshooting guide
- 📄 **README.md** - Original docs (in k8s/)

---

## TL;DR

```bash
# Fix your current issue:
sudo ./library.sh start-k3s && ./deploy.sh up

# Future deployments:
./quick-deploy.sh
```

🎉 Done!
