# Monitoring Stack - Installation Summary

## 📦 ไฟล์ที่สร้างขึ้นทั้งหมด

### 1. สคริปต์หลัก (Deployment Scripts)
- ✅ `monitoring/deploy-monitoring.sh` - Bash script สำหรับ Linux/macOS
- ✅ `monitoring/deploy-monitoring.ps1` - PowerShell script สำหรับ Windows

### 2. ไฟล์ Configuration
- ✅ `monitoring/values.yaml` - Helm values สำหรับ kube-prometheus-stack
- ✅ `monitoring/grafana-admin-secret.yaml` - Kubernetes Secret สำหรับรหัสผ่าน Grafana

### 3. Dashboard
- ✅ `monitoring/grafana-dashboard-backend.json` - Custom dashboard สำหรับ Backend metrics

### 4. Documentation
- ✅ `monitoring/README.md` - เอกสารหลักที่ครบถ้วน
- ✅ `monitoring/BACKEND_SETUP.md` - คู่มือตั้งค่า Backend
- ✅ `monitoring/QUICK_REFERENCE.md` - Quick reference guide
- ✅ `monitoring/SUMMARY.md` - ไฟล์นี้

### 5. Testing Scripts
- ✅ `monitoring/test-monitoring.sh` - Bash test script
- ✅ `monitoring/test-monitoring.ps1` - PowerShell test script

### 6. การแก้ไขไฟล์เดิม
- ✅ `k8s/backend/service.yaml` - เพิ่ม label `monitoring: enabled`

## 🎯 Features ที่ได้รับ

### ✅ Production-Grade Components
- **Prometheus**: ระบบเก็บ metrics ขนาด 10Gi, retention 7 วัน
- **Grafana**: Dashboard พร้อม Ingress ที่ grafana.localhost
- **ServiceMonitor**: Auto-discovery ของ backend metrics
- **Persistence**: ข้อมูลไม่หายเมื่อ restart pods

### ✅ Dashboard Metrics
Dashboard แสดงข้อมูล 7 แผง:
1. HTTP Request Rate (by method & status)
2. JVM Heap Memory Usage (Gauge)
3. Process CPU Usage (Gauge)
4. JVM Memory Usage (All areas)
5. HTTP Request Duration (Average)
6. JVM Threads
7. Garbage Collection Rate

### ✅ Automation
- One-command deployment
- Prerequisite checking
- Auto repository setup
- Namespace creation
- Secret management
- Health verification

### ✅ Resource Optimization
- Agent mode สำหรับ Prometheus
- Resource limits ที่เหมาะสมกับ K3s single-node
- Disabled components ที่ไม่จำเป็น (alertmanager, thanosRuler, etc.)

### ✅ Documentation
- Complete README with troubleshooting
- Backend setup guide
- Quick reference for common tasks
- Test scripts for validation

## 🚀 การติดตั้ง (Quick Start)

### สำหรับ Linux/macOS:
```bash
cd monitoring
chmod +x deploy-monitoring.sh
./deploy-monitoring.sh
```

### สำหรับ Windows PowerShell:
```powershell
cd monitoring
.\deploy-monitoring.ps1
```

## 📝 ขั้นตอนหลังติดตั้ง

### 1. แก้ไข Hosts File
**Linux/macOS**: `/etc/hosts`
```bash
sudo echo "127.0.0.1 grafana.localhost" >> /etc/hosts
```

**Windows**: `C:\Windows\System32\drivers\etc\hosts` (ต้อง Run as Administrator)
```
127.0.0.1 grafana.localhost
```

### 2. Apply Backend Service Changes
```bash
kubectl apply -f k8s/backend/service.yaml
```

### 3. เข้าถึง Grafana
- URL: http://grafana.localhost
- Username: `admin`
- Password: `SuperSecure2024!`

### 4. Import Dashboard
1. Login to Grafana
2. คลิก `+` → Import
3. อัปโหลด `monitoring/grafana-dashboard-backend.json`
4. คลิก Import

## ✅ การทดสอบ

### Linux/macOS:
```bash
chmod +x test-monitoring.sh
./test-monitoring.sh
```

### Windows PowerShell:
```powershell
.\test-monitoring.ps1
```

## 📊 Resource Requirements

| Component | CPU Request | Memory Request | Storage |
|-----------|-------------|----------------|---------|
| Prometheus | 250m | 512Mi | 10Gi |
| Grafana | 100m | 128Mi | 1Gi |
| Prometheus Operator | 100m | 128Mi | - |
| Node Exporter | 50m | 64Mi | - |
| Kube State Metrics | 50m | 64Mi | - |
| **Total** | **~550m** | **~896Mi** | **11Gi** |

## 🔧 Backend Requirements

ตรวจสอบว่า Backend มีการตั้งค่าเหล่านี้:

### build.gradle
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
}
```

### application.properties
```properties
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true
```

## 🔍 การตรวจสอบว่าทำงานถูกต้อง

### 1. ตรวจสอบ Pods
```bash
kubectl get pods -n superproject-ns -l app.kubernetes.io/instance=monitoring
```

ควรเห็น:
- `monitoring-grafana-xxx` - Running
- `monitoring-kube-prometheus-prometheus-0` - Running
- `monitoring-kube-state-metrics-xxx` - Running
- `monitoring-prometheus-node-exporter-xxx` - Running

### 2. ตรวจสอบ ServiceMonitor
```bash
kubectl get servicemonitor -n superproject-ns
```

ควรเห็น: `backend-servicemonitor`

### 3. ตรวจสอบ Prometheus Targets
```bash
kubectl port-forward -n superproject-ns svc/monitoring-kube-prometheus-prometheus 9090:9090
```
เปิด: http://localhost:9090/targets
ควรเห็น `backend-service` ใน state "UP"

### 4. ทดสอบ Backend Metrics
```bash
kubectl port-forward -n superproject-ns deployment/backend-deployment 8080:8080
curl http://localhost:8080/actuator/prometheus
```

ควรเห็น metrics เช่น:
```
jvm_memory_used_bytes{...}
http_server_requests_seconds_count{...}
process_cpu_usage{...}
```

## 🛑 Uninstall

### Linux/macOS:
```bash
./deploy-monitoring.sh --uninstall
```

### Windows PowerShell:
```powershell
.\deploy-monitoring.ps1 -Uninstall
```

### ลบ PVCs (ถ้าต้องการ):
```bash
kubectl delete pvc -n superproject-ns -l app.kubernetes.io/instance=monitoring
```

## 🔐 Security Notes

**สำหรับ Production ต้องเปลี่ยน:**

1. **Grafana Password**: แก้ไขใน `grafana-admin-secret.yaml`
   ```bash
   # Generate secure password
   openssl rand -base64 32
   ```

2. **Enable HTTPS**: เพิ่ม TLS ใน Ingress

3. **Authentication**: เปิดใช้งาน authentication สำหรับ Prometheus

4. **RBAC**: จำกัดสิทธิ์การเข้าถึง

5. **Network Policies**: จำกัดการสื่อสารระหว่าง pods

## 📚 เอกสารเพิ่มเติม

- 📖 [README.md](./README.md) - เอกสารหลักที่ครบถ้วน
- 🔧 [BACKEND_SETUP.md](./BACKEND_SETUP.md) - คู่มือตั้งค่า Backend
- ⚡ [QUICK_REFERENCE.md](./QUICK_REFERENCE.md) - Quick reference
- 🌐 [kube-prometheus-stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack)
- 📊 [Prometheus Docs](https://prometheus.io/docs/)
- 📈 [Grafana Docs](https://grafana.com/docs/)

## ✨ Key Features Summary

### ✅ ใช้ Best Practices
- Helm Chart จาก prometheus-community (industry standard)
- ServiceMonitor สำหรับ auto-discovery
- Persistence สำหรับเก็บข้อมูล
- Resource limits ที่เหมาะสม
- Security (Secret management)

### ✅ Production-Ready
- High availability configuration
- Data persistence
- Proper resource allocation
- Health checks
- Monitoring of monitoring (meta!)

### ✅ Developer-Friendly
- One-command deployment
- Comprehensive documentation
- Test scripts
- Troubleshooting guides
- Quick reference

### ✅ Integrated
- Works with existing deployment
- Same namespace
- Automatic service discovery
- No manual configuration needed

## 🎉 ผลลัพธ์สุดท้าย

หลังจากรันสคริปต์ คุณจะได้:

1. ✅ Prometheus ที่รันอยู่และ scrape metrics จาก backend
2. ✅ Grafana พร้อม dashboard ที่แสดงผล metrics
3. ✅ ServiceMonitor ที่ auto-discover backend service
4. ✅ Persistent storage สำหรับข้อมูล
5. ✅ Ingress สำหรับเข้าถึง Grafana ง่ายๆ
6. ✅ เอกสารครบถ้วนสำหรับ maintenance

## 🆘 Troubleshooting Quick Links

- **Grafana ไม่เปิด**: ตรวจสอบ Ingress และ hosts file
- **ไม่มี metrics**: ตรวจสอบ backend service label และ actuator endpoint
- **Pod ไม่ Running**: ตรวจสอบ resources และ logs
- **Permission denied**: ตรวจสอบ RBAC และ service account

ดูเพิ่มเติมใน [README.md](./README.md) section "Troubleshooting"

## 📞 Support Commands

```bash
# ดู status ทั้งหมด
kubectl get all -n superproject-ns -l app.kubernetes.io/instance=monitoring

# ดู logs
kubectl logs -n superproject-ns -l app.kubernetes.io/name=grafana -f

# ดู Helm release
helm list -n superproject-ns

# Run tests
./test-monitoring.sh  # or .\test-monitoring.ps1
```

---

**Created by:** Monitoring Stack Integration Project
**Date:** November 2025
**Version:** 1.0.0
**Status:** ✅ Production Ready
