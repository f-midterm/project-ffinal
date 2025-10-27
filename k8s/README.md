# Kubernetes Manifests - Apartment Management System

This directory contains all Kubernetes manifest files needed to deploy the entire Apartment Management System to a K3s/Kubeadm cluster.

## � QUICK START (ONE COMMAND!)

**First time setup:**
```bash
chmod +x *.sh
sudo ./library.sh install-all    # Install Docker, K3s, kubectl, jq
sudo ./library.sh start-k3s      # Start K3s service
./deploy.sh up                   # Deploy everything!
```

**Daily deployment:**
```bash
./quick-deploy.sh    # One command does everything!
```

**Already have K3s running?**
```bash
./deploy.sh up       # Build, deploy, and expose everything
```

📖 **See [QUICK_START.md](./QUICK_START.md) for instant solutions!**
📖 **See [ONE_COMMAND_DEPLOY.md](./ONE_COMMAND_DEPLOY.md) for complete guide!**

---

## �📁 Directory Structure

```
k8s/
├── namespace.yaml                 # Namespace definition
├── database/                      # Database (MySQL) components
│   ├── secret.yaml               # MySQL credentials
│   ├── configmap.yaml            # init.sql script
│   ├── pvc.yaml                  # Persistent Volume Claim (5Gi)
│   ├── statefulset.yaml          # MySQL StatefulSet
│   └── service.yaml              # MySQL ClusterIP Service
├── backend/                       # Backend (Spring Boot) components
│   ├── deployment.yaml           # Backend Deployment (3 replicas)
│   └── service.yaml              # Backend ClusterIP Service
├── frontend/                      # Frontend (React/Nginx) components
│   ├── deployment.yaml           # Frontend Deployment (2 replicas)
│   └── service.yaml              # Frontend ClusterIP Service
└── ingress/                       # Ingress routing
    ├── ingress.yaml              # Nginx Ingress with URL rewriting
    └── ingress-traefik.yaml      # Traefik Ingress (alternative)
```

## 🚀 Quick Start

### 0. Check ingress-nginx
```bash
kubectl get deploy -n ingress-nginx
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
```

### 1. Deploy All Resources

```bash
# Apply all manifests in order
kubectl apply -f namespace.yaml
kubectl apply -f database/
kubectl apply -f backend/
kubectl apply -f frontend/
kubectl apply -f ingress/ingress.yaml

# OR deploy everything at once
kubectl apply -R -f k8s/
```

### 2. Verify Deployment

```bash
# Check namespace
kubectl get ns superproject-ns

# Check all resources
kubectl get all -n superproject-ns

# Watch pods until ready
kubectl get pods -n superproject-ns -w

# Check ingress
kubectl get ingress -n superproject-ns
```

### 3. Access the Application

```bash
# Get ingress IP/hostname
kubectl get ingress apartment-ingress -n superproject-ns

# Add to /etc/hosts (or C:\Windows\System32\drivers\etc\hosts on Windows)
<INGRESS_IP> apartment.local

# Access application
# Frontend: http://apartment.local/
# Backend API: http://apartment.local/api/
```

## 📝 Configuration

### Before Deploying

1. **Update Docker Images** (in deployment files):
   ```yaml
   # backend/deployment.yaml
   image: your-registry/apartment-backend:prod
   
   # frontend/deployment.yaml
   image: your-registry/apartment-frontend:prod
   ```

2. **Update Secrets** (database/secret.yaml):
   ```bash
   # Encode your values
   echo -n 'your-password' | base64
   
   # Update secret.yaml with encoded values
   ```

3. **Update Ingress Host** (ingress/ingress.yaml):
   ```yaml
   host: apartment.local  # Change to your domain
   ```

4. **Choose Ingress Controller**:
   - For **Nginx**: Use `ingress/ingress.yaml`
   - For **Traefik**: Use `ingress/ingress-traefik.yaml`

## 🔧 Customization

### Scaling

```bash
# Scale backend
kubectl scale deployment backend -n superproject-ns --replicas=5

# Scale frontend
kubectl scale deployment frontend -n superproject-ns --replicas=3
```

### Update Configuration

```bash
# Update secrets
kubectl apply -f database/secret.yaml
kubectl rollout restart statefulset mysql -n superproject-ns

# Update deployments
kubectl apply -f backend/deployment.yaml
kubectl rollout restart deployment backend -n superproject-ns
```

### View Logs

```bash
# Database logs
kubectl logs -n superproject-ns -l component=database -f

# Backend logs
kubectl logs -n superproject-ns -l component=backend -f

# Frontend logs
kubectl logs -n superproject-ns -l component=frontend -f
```

## 🛠️ Troubleshooting

### Pods Not Starting

```bash
# Describe pod to see events
kubectl describe pod <pod-name> -n superproject-ns

# Check pod logs
kubectl logs <pod-name> -n superproject-ns

# Check events
kubectl get events -n superproject-ns --sort-by='.lastTimestamp'
```

### Database Issues

```bash
# Check MySQL logs
kubectl logs -n superproject-ns -l component=database

# Connect to MySQL pod
kubectl exec -it mysql-0 -n superproject-ns -- mysql -u apartment -p

# Check PVC status
kubectl get pvc -n superproject-ns
```

### Ingress Not Working

```bash
# Check ingress controller logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/component=controller

# Check ingress resource
kubectl describe ingress apartment-ingress -n superproject-ns

# Test backend directly (port-forward)
kubectl port-forward -n superproject-ns svc/backend-service 8080:8080
```

## 🔒 Production Considerations

### Security

1. **Use proper secret management**:
   - Sealed Secrets
   - External Secrets Operator
   - HashiCorp Vault

2. **Enable TLS/HTTPS**:
   - Uncomment TLS section in ingress.yaml
   - Create TLS secret with your certificate

3. **Network Policies**:
   - Restrict pod-to-pod communication
   - Limit external access

### High Availability

1. **Database Replication**:
   - Consider MySQL read replicas
   - Or use managed database service (RDS, Cloud SQL)

2. **Multiple Ingress Controllers**:
   - Deploy ingress controller across multiple nodes

3. **Pod Disruption Budgets**:
   - Ensure minimum replicas during maintenance

### Monitoring

1. **Add Prometheus annotations** to deployments:
   ```yaml
   annotations:
     prometheus.io/scrape: "true"
     prometheus.io/port: "8080"
     prometheus.io/path: "/actuator/prometheus"
   ```

2. **Set up logging**:
   - ELK Stack (Elasticsearch, Logstash, Kibana)
   - Loki + Grafana
   - Cloud provider logging

## 📊 Resource Requirements

| Component | CPU Request | CPU Limit | Memory Request | Memory Limit |
|-----------|-------------|-----------|----------------|--------------|
| MySQL     | 500m        | 1000m     | 512Mi          | 1Gi          |
| Backend   | 1000m       | 2000m     | 1Gi            | 2Gi          |
| Frontend  | 250m        | 500m      | 128Mi          | 256Mi        |

**Total per replica**:
- Backend: 3 replicas = 3-6 CPU cores, 3-6Gi RAM
- Frontend: 2 replicas = 0.5-1 CPU core, 256-512Mi RAM
- Database: 1 replica = 0.5-1 CPU core, 512Mi-1Gi RAM

**Cluster minimum**: ~4-8 CPU cores, 8-12Gi RAM

## 🔄 CI/CD Integration

### GitLab CI Example

```yaml
deploy:
  stage: deploy
  script:
    - kubectl apply -R -f k8s/
    - kubectl rollout status deployment/backend -n superproject-ns
    - kubectl rollout status deployment/frontend -n superproject-ns
  only:
    - main
```

### GitHub Actions Example

```yaml
- name: Deploy to Kubernetes
  run: |
    kubectl apply -R -f k8s/
    kubectl rollout status deployment/backend -n superproject-ns
    kubectl rollout status deployment/frontend -n superproject-ns
```

## 📚 Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Nginx Ingress Controller](https://kubernetes.github.io/ingress-nginx/)
- [Traefik Ingress Controller](https://doc.traefik.io/traefik/providers/kubernetes-ingress/)
- [K3s Documentation](https://docs.k3s.io/)

## 🆘 Support

For issues or questions:
1. Check pod logs: `kubectl logs -n superproject-ns <pod-name>`
2. Check events: `kubectl get events -n superproject-ns`
3. Describe resources: `kubectl describe <resource> <name> -n superproject-ns`
