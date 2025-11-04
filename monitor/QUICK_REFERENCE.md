# Monitoring Stack - Quick Reference

## 🚀 Quick Deploy

```bash
# Linux/macOS
cd monitoring
chmod +x deploy-monitoring.sh
./deploy-monitoring.sh

# Windows PowerShell
cd monitoring
.\deploy-monitoring.ps1
```

## 🔑 Access Credentials

**Grafana:**
- URL: http://grafana.localhost
- Username: `admin`
- Password: `SuperSecure2024!`

**Get password from secret:**
```bash
# Linux/macOS
kubectl get secret grafana-admin-secret -n superproject-ns -o jsonpath='{.data.admin-password}' | base64 --decode

# Windows PowerShell
kubectl get secret grafana-admin-secret -n superproject-ns -o jsonpath='{.data.admin-password}' | ForEach-Object { [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($_)) }
```

## 📝 Hosts File Setup

Add this line to your hosts file:

**Linux/macOS:** `/etc/hosts`
**Windows:** `C:\Windows\System32\drivers\etc\hosts`

```
127.0.0.1 grafana.localhost
```

## 📊 Import Dashboard

1. Login to Grafana at http://grafana.localhost
2. Click `+` → Import
3. Upload `monitoring/grafana-dashboard-backend.json`
4. Click Import

## 🔍 Useful Commands

### Check Deployment Status
```bash
# All monitoring pods
kubectl get pods -n superproject-ns -l app.kubernetes.io/instance=monitoring

# Grafana
kubectl get pods -n superproject-ns -l app.kubernetes.io/name=grafana

# Prometheus
kubectl get pods -n superproject-ns -l app.kubernetes.io/name=prometheus
```

### View Logs
```bash
# Grafana logs
kubectl logs -n superproject-ns -l app.kubernetes.io/name=grafana -f

# Prometheus logs
kubectl logs -n superproject-ns -l app.kubernetes.io/name=prometheus -f
```

### Access Prometheus
```bash
kubectl port-forward -n superproject-ns svc/monitoring-kube-prometheus-prometheus 9090:9090
# Then visit: http://localhost:9090
```

### Check ServiceMonitor
```bash
# List ServiceMonitors
kubectl get servicemonitor -n superproject-ns

# Describe backend ServiceMonitor
kubectl describe servicemonitor backend-servicemonitor -n superproject-ns
```

### Verify Backend Metrics
```bash
# Port forward to backend
kubectl port-forward -n superproject-ns deployment/backend-deployment 8080:8080

# Test metrics endpoint
curl http://localhost:8080/actuator/prometheus
```

## 🔄 Update Monitoring Stack

```bash
# Linux/macOS
./deploy-monitoring.sh

# Windows PowerShell
.\deploy-monitoring.ps1
```

## 🗑️ Uninstall

```bash
# Linux/macOS
./deploy-monitoring.sh --uninstall

# Windows PowerShell
.\deploy-monitoring.ps1 -Uninstall
```

### Remove PVCs (Optional)
```bash
kubectl delete pvc -n superproject-ns -l app.kubernetes.io/instance=monitoring
```

## 🛠️ Troubleshooting

### Grafana not accessible
```bash
# Check Ingress
kubectl get ingress -n superproject-ns

# Check Grafana pod
kubectl get pods -n superproject-ns -l app.kubernetes.io/name=grafana

# View Grafana logs
kubectl logs -n superproject-ns -l app.kubernetes.io/name=grafana
```

### No metrics showing
```bash
# Check if backend has monitoring label
kubectl get svc backend-service -n superproject-ns -o yaml | grep monitoring

# Check ServiceMonitor
kubectl get servicemonitor -n superproject-ns

# Check Prometheus targets (via port-forward)
kubectl port-forward -n superproject-ns svc/monitoring-kube-prometheus-prometheus 9090:9090
# Visit: http://localhost:9090/targets
```

### Backend metrics not working
```bash
# Test actuator endpoint
kubectl exec -n superproject-ns deployment/backend-deployment -- curl localhost:8080/actuator/prometheus

# Check backend logs
kubectl logs -n superproject-ns -l component=backend
```

## 📦 Resource Usage

| Component | CPU Request | Memory Request | Storage |
|-----------|-------------|----------------|---------|
| Prometheus | 250m | 512Mi | 10Gi |
| Grafana | 100m | 128Mi | 1Gi |
| Operator | 100m | 128Mi | - |
| Node Exporter | 50m | 64Mi | - |
| Kube State | 50m | 64Mi | - |

**Total:** ~550m CPU, ~896Mi Memory

## 🔐 Security Notes

**For Production:**
1. Change default Grafana password in `grafana-admin-secret.yaml`
2. Enable HTTPS for Grafana Ingress
3. Restrict Prometheus access with authentication
4. Implement NetworkPolicies
5. Use RBAC for access control

## 📚 Key Files

- `deploy-monitoring.sh` / `deploy-monitoring.ps1` - Deployment scripts
- `values.yaml` - Helm chart configuration
- `grafana-admin-secret.yaml` - Grafana credentials
- `grafana-dashboard-backend.json` - Backend dashboard
- `README.md` - Full documentation
- `BACKEND_SETUP.md` - Backend configuration guide

## 🎯 Common Prometheus Queries

```promql
# Request rate
rate(http_server_requests_seconds_count{job="backend-service"}[5m])

# Average response time
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Error rate
rate(http_server_requests_seconds_count{status=~"[45].."}[5m])

# Memory usage %
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# CPU usage %
process_cpu_usage * 100
```

## 🌐 Important URLs

- **Grafana**: http://grafana.localhost
- **Prometheus** (via port-forward): http://localhost:9090
- **Backend Metrics**: http://backend-service:8080/actuator/prometheus

## 💡 Tips

1. **Auto-refresh in Grafana**: Set dashboard refresh to 10s for real-time monitoring
2. **Time range**: Adjust time range in top-right corner (Last 1h, 6h, 24h, etc.)
3. **Save dashboards**: Always save after making changes
4. **Variables**: Use dashboard variables for dynamic filtering
5. **Alerts**: Configure in Grafana Alerting section

## 🔗 Documentation Links

- [Main README](./README.md) - Complete setup guide
- [Backend Setup](./BACKEND_SETUP.md) - Backend configuration
- [kube-prometheus-stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack)
- [Prometheus Docs](https://prometheus.io/docs/)
- [Grafana Docs](https://grafana.com/docs/)

## 📞 Support

Check status:
```bash
helm list -n superproject-ns
kubectl get all -n superproject-ns -l app.kubernetes.io/instance=monitoring
```

Get help:
```bash
./deploy-monitoring.sh --help      # Linux/macOS
Get-Help .\deploy-monitoring.ps1   # Windows PowerShell
```
