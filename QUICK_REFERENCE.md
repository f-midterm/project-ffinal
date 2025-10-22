# Quick Reference - Kubernetes Deployment

## One-Command Deployment

Deploy the entire stack (MySQL, Backend, Frontend, Ingress) after a cluster reset:

```powershell
cd k8s
.\deploy.ps1
```

This will:
- Build Docker images (backend + frontend)
- Apply all Kubernetes manifests in order
- Wait for MySQL, backend, frontend to be ready
- Configure Ingress or auto-expose NodePorts
- Run health checks
- Print access URLs

## Production Update (Code Changes Only)

When you update backend or frontend code **without changing the database**:

```powershell
cd k8s
.\update.ps1
```

**What it does:**
- Rebuilds backend and frontend Docker images
- Applies updated Kubernetes manifests
- Performs rolling restart (zero-downtime)
- Waits for rollouts to complete
- Validates health endpoints
- Shows deployment status

**Options:**
```powershell
# Fast update: skip image rebuild (manifest changes only)
.\update.ps1 -SkipBuild

# Skip health validation
.\update.ps1 -SkipHealthCheck

# Both
.\update.ps1 -SkipBuild -SkipHealthCheck
```

## Manual Operations

### Build Images Only
```powershell
cd k8s
.\build-images.ps1
```

### Deploy Without Building
```powershell
cd k8s
.\deploy.ps1 -Command up
```

### Tear Down
```powershell
cd k8s
.\deploy.ps1 -Command down
```

### Check Status
```powershell
cd k8s
.\deploy.ps1 -Command status
```

### View Logs
```powershell
cd k8s
.\deploy.ps1 -Command logs -Target backend
.\deploy.ps1 -Command logs -Target frontend
```

### Manual Restart
```powershell
cd k8s
.\deploy.ps1 -Command restart -Target backend
.\deploy.ps1 -Command restart -Target frontend
```

## Access URLs

- **Frontend:** http://localhost/
- **API Health:** http://localhost/api/health
- **Login:** http://localhost/login

## Test Credentials

- **Admin:** admin / admin123
- **User:** villager / villager123
- **Test:** testuser / testuser123

## Common Workflows

### After Code Changes (Backend/Frontend)
```powershell
# 1. Make your code changes in backend/ or frontend/
# 2. Run update script
cd k8s
.\update.ps1

# 3. Verify
# - Check pod status: kubectl get pods -n superproject-ns
# - Test login: http://localhost/login
```

### After Database Schema Changes
```powershell
# 1. Update k8s/database/configmap.yaml with new schema
# 2. Delete MySQL pod to reset database
kubectl delete pod mysql-0 -n superproject-ns

# 3. Wait for it to restart and re-initialize
kubectl wait --for=condition=ready pod/mysql-0 -n superproject-ns --timeout=120s

# 4. Restart backend to reconnect
kubectl rollout restart deployment/backend -n superproject-ns
```

### Full Cluster Reset Recovery
```powershell
# After `docker desktop reset` or cluster wipe:
cd k8s
.\deploy.ps1

# This rebuilds images and deploys everything from scratch
```

## Troubleshooting

### Pods Not Starting
```powershell
kubectl get pods -n superproject-ns
kubectl describe pod <pod-name> -n superproject-ns
kubectl logs <pod-name> -n superproject-ns
```

### Image Pull Errors
```powershell
# Rebuild images
cd k8s
.\build-images.ps1

# Restart deployment
kubectl rollout restart deployment/backend -n superproject-ns
kubectl rollout restart deployment/frontend -n superproject-ns
```

### Database Issues
```powershell
# Check MySQL logs
kubectl logs mysql-0 -n superproject-ns

# Connect to MySQL
kubectl exec -it mysql-0 -n superproject-ns -- mysql -u apartment -p
# Password: secure_password_change_me

# Reset database
kubectl delete pod mysql-0 -n superproject-ns --force --grace-period=0
```

### Ingress Not Working
```powershell
# Check ingress controller
kubectl get pods -n ingress-nginx

# Check ingress rules
kubectl get ingress -n superproject-ns
kubectl describe ingress apartment-ingress-backend -n superproject-ns
kubectl describe ingress apartment-ingress-frontend -n superproject-ns

# If not installed, install ingress-nginx:
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
```

## Production Best Practices

1. **Always use `update.ps1` for code changes** - It handles zero-downtime rolling updates
2. **Never update database manually in production** - Use migrations via ConfigMap and controlled restarts
3. **Monitor rollout status** - The script waits and validates; watch for errors
4. **Keep images versioned** - Consider tagging with git SHA or version numbers instead of `:prod`
5. **Use proper secrets** - In production, replace inline secrets with Kubernetes Secrets or external secret managers

## CI/CD Integration

For automated deployments:

```powershell
# In your CI/CD pipeline:
# 1. Build images with version tag
docker build -t apartment-backend:v1.2.3 ...
docker build -t apartment-frontend:v1.2.3 ...

# 2. Push to registry
docker push your-registry/apartment-backend:v1.2.3
docker push your-registry/apartment-frontend:v1.2.3

# 3. Update manifests and deploy
kubectl set image deployment/backend backend=your-registry/apartment-backend:v1.2.3 -n superproject-ns
kubectl set image deployment/frontend frontend=your-registry/apartment-frontend:v1.2.3 -n superproject-ns

# 4. Wait for rollout
kubectl rollout status deployment/backend -n superproject-ns
kubectl rollout status deployment/frontend -n superproject-ns
```

## Additional Resources

- **Kubernetes Manifests:** `k8s/` folder
- **Build Script:** `k8s/build-images.ps1`
- **Deploy Script:** `k8s/deploy.ps1`
- **Update Script:** `k8s/update.ps1`
- **Backend Dockerfile:** `backend/Dockerfile.prod`
- **Frontend Dockerfile:** `frontend/Dockerfile.prod`
- **Nginx K8s Config:** `frontend/nginx.k8s.conf`
