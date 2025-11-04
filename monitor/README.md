# Monitoring Stack Setup Guide

## Overview

This directory contains the complete setup for integrating Prometheus and Grafana monitoring into the apartment system Kubernetes deployment. The stack uses the industry-standard `kube-prometheus-stack` Helm chart with custom configurations optimized for K3s single-node deployments.

## Components

- **Prometheus**: Metrics collection and storage (agent mode for lower resource usage)
- **Grafana**: Visualization and dashboarding
- **Node Exporter**: System metrics collection
- **Kube State Metrics**: Kubernetes cluster metrics
- **ServiceMonitor**: Automatic discovery of backend application metrics

## Prerequisites

1. **Helm 3.x** must be installed on your system
   - Linux/macOS: `curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash`
   - Windows: `choco install kubernetes-helm`
   - Or visit: https://helm.sh/docs/intro/install/

2. **kubectl** must be configured to access your K3s cluster

3. **Backend service** must have Spring Boot Actuator with Prometheus endpoint enabled at `/actuator/prometheus`

## Quick Start

### 1. Deploy the Monitoring Stack

```bash
cd monitoring
./deploy-monitoring.sh
```

The script will:
- Check prerequisites (Helm, kubectl)
- Add Prometheus Community Helm repository
- Create namespace if needed
- Apply Grafana admin credentials
- Install/upgrade the monitoring stack
- Display access information

### 2. Access Grafana

**Add to hosts file:**

- **Linux/macOS**: Edit `/etc/hosts`
- **Windows**: Edit `C:\Windows\System32\drivers\etc\hosts`

Add this line:
```
127.0.0.1 grafana.localhost
```

**Access Grafana:**
- URL: http://grafana.localhost
- Username: `admin`
- Password: `SuperSecure2024!` (default, can be changed in `grafana-admin-secret.yaml`)

### 3. Import Custom Backend Dashboard

1. Log in to Grafana
2. Click the `+` icon in the left sidebar
3. Select "Import dashboard"
4. Click "Upload JSON file"
5. Select `grafana-dashboard-backend.json` from this directory
6. Click "Import"

The dashboard will display:
- HTTP Request Rate (by method and status)
- JVM Heap Memory Usage
- Process CPU Usage
- JVM Memory Usage (all areas)
- HTTP Request Duration
- JVM Threads
- Garbage Collection Rate

## Configuration Files

### `deploy-monitoring.sh`
Main deployment script with the following features:
- Automated installation/upgrade
- Prerequisite checking
- Colored output for better readability
- Support for uninstallation with `--uninstall` flag

### `values.yaml`
Helm values configuration including:
- **Persistence**: 10Gi for Prometheus, 1Gi for Grafana
- **Resource Limits**: Optimized for single-node K3s
- **Ingress**: Traefik-based access for Grafana
- **ServiceMonitor**: Automatic backend service discovery
- **Data Sources**: Pre-configured Prometheus datasource

### `grafana-admin-secret.yaml`
Kubernetes Secret containing Grafana admin credentials:
- Default username: `admin`
- Default password: `SuperSecure2024!`
- **Important**: Change the password before production deployment!

### `grafana-dashboard-backend.json`
Pre-configured Grafana dashboard for backend application monitoring with 7 panels covering essential metrics.

## Backend Service Configuration

To enable Prometheus monitoring, your backend service must be labeled correctly. Update `k8s/backend/service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend-service
  namespace: superproject-ns
  labels:
    app: apartment-system
    component: backend
    monitoring: enabled  # Add this label
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: apartment-system
    component: backend
```

**Important**: The `monitoring: enabled` label is required for the ServiceMonitor to discover this service.

After updating the service:
```bash
kubectl apply -f k8s/backend/service.yaml
```

## Accessing Prometheus

Prometheus is not exposed via Ingress by default. To access it:

```bash
kubectl port-forward -n superproject-ns svc/monitoring-kube-prometheus-prometheus 9090:9090
```

Then visit: http://localhost:9090

## Retrieving Grafana Password

If you forget the admin password:

```bash
kubectl get secret grafana-admin-secret -n superproject-ns -o jsonpath='{.data.admin-password}' | base64 --decode
```

## Resource Usage

The monitoring stack is configured with the following resource limits:

| Component | CPU Request | CPU Limit | Memory Request | Memory Limit |
|-----------|-------------|-----------|----------------|--------------|
| Prometheus | 250m | 500m | 512Mi | 1Gi |
| Grafana | 100m | 200m | 128Mi | 256Mi |
| Prometheus Operator | 100m | 200m | 128Mi | 256Mi |
| Node Exporter | 50m | 100m | 64Mi | 128Mi |
| Kube State Metrics | 50m | 100m | 64Mi | 128Mi |

Total: ~550m CPU request, ~1.1Gi memory request

## Persistence

Data is persisted using PersistentVolumeClaims:
- **Prometheus**: 10Gi, 7-day retention
- **Grafana**: 1Gi for dashboards and configuration

## Uninstalling

To remove the monitoring stack:

```bash
./deploy-monitoring.sh --uninstall
```

**Note**: PVCs are not automatically deleted. To remove them:

```bash
kubectl delete pvc -n superproject-ns -l app.kubernetes.io/instance=monitoring
```

## Troubleshooting

### Grafana not accessible
1. Check if Ingress is created: `kubectl get ingress -n superproject-ns`
2. Verify Traefik is running: `kubectl get pods -n kube-system | grep traefik`
3. Ensure hosts file is configured correctly

### No metrics in Grafana
1. Check if ServiceMonitor is created: `kubectl get servicemonitor -n superproject-ns`
2. Verify backend service has the correct label: `kubectl get svc backend-service -n superproject-ns -o yaml | grep monitoring`
3. Check Prometheus targets: Port-forward Prometheus and visit http://localhost:9090/targets

### Backend metrics not showing
1. Verify Spring Boot Actuator is enabled
2. Check if `/actuator/prometheus` endpoint is accessible from within the cluster
3. Verify the service port name is `http` in the service definition

## Advanced Configuration

### Changing Prometheus Retention
Edit `values.yaml`:
```yaml
prometheus:
  prometheusSpec:
    retention: 14d  # Change from 7d to 14d
    retentionSize: "18GB"  # Adjust accordingly
```

### Adding More ServiceMonitors
Edit `values.yaml` under `additionalServiceMonitors` section.

### Custom Grafana Dashboards
Place additional dashboard JSON files in the monitoring directory and import them manually through Grafana UI, or configure them in `values.yaml` under `grafana.dashboards`.

## Integration with Existing Deployment

This monitoring stack is designed to work alongside your existing deployment scripts:
- Can be deployed independently of the main application
- Uses the same namespace (`superproject-ns`)
- Does not interfere with existing services
- Can be updated without affecting the main application

## Security Considerations

For production deployments:
1. Change the default Grafana admin password
2. Enable HTTPS/TLS for Grafana Ingress
3. Implement authentication for Prometheus
4. Use Kubernetes RBAC to restrict access
5. Enable network policies to limit traffic

## Support

For issues or questions:
1. Check logs: `kubectl logs -n superproject-ns -l app.kubernetes.io/name=grafana`
2. Verify Helm release: `helm list -n superproject-ns`
3. Check ServiceMonitor status: `kubectl describe servicemonitor backend-servicemonitor -n superproject-ns`

## References

- [kube-prometheus-stack Chart](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Spring Boot Actuator with Prometheus](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics.export.prometheus)
