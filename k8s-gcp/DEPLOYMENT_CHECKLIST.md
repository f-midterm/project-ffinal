# ✅ GCP Deployment Checklist

## 📋 Pre-Deployment (ก่อนได้ข้อมูลจากอาจารย์)

### ✅ โปรเจคพร้อม
- [x] Kubernetes manifests ครบถ้วน
- [x] Docker images พร้อม build
- [x] Deployment scripts พร้อมใช้
- [x] แยกโฟลเดอร์ localhost (`k8s/`) และ GCP (`k8s-gcp/`) แล้ว

### ⚠️ ติดตั้งเครื่องมือ
- [ ] **gcloud CLI** - [Download](https://cloud.google.com/sdk/docs/install)
  ```powershell
  gcloud --version
  ```
- [ ] **kubectl** - [Download](https://kubernetes.io/docs/tasks/tools/)
  ```powershell
  kubectl version --client
  ```
- [ ] **helm** - [Download](https://helm.sh/docs/intro/install/)
  ```powershell
  helm version
  ```
- [x] **Docker Desktop** (มีแล้ว)
  ```powershell
  docker --version
  ```

### 📝 ทดสอบ Localhost
- [ ] Deploy บน Docker Desktop Kubernetes สำเร็จ
  ```powershell
  cd k8s
  .\deploy.ps1 up
  ```
- [ ] ทุก pods ทำงานปกติ (Running status)
  ```powershell
  kubectl get pods -n superproject-ns
  ```
- [ ] Frontend เข้าถึงได้
  ```powershell
  kubectl port-forward -n superproject-ns svc/frontend-service 8080:80
  # ทดสอบ: http://localhost:8080
  ```
- [ ] Backend API ตอบกลับถูกต้อง
  ```powershell
  curl http://localhost:8080/api/health
  ```

### 🔐 Git & Backup
- [ ] Commit code ทั้งหมดขึ้น GitHub
  ```powershell
  git add .
  git commit -m "Add GCP deployment configuration"
  git push origin deploy/feature/monitor
  ```
- [ ] Backup database (ถ้ามีข้อมูลสำคัญ)

---

## 📨 รอจากอาจารย์ (2-3 วัน)

### ข้อมูลที่จะได้รับ:
- [ ] **GCP Project ID**
  - ตัวอย่าง: `muict-project-2025`
  - จะใช้ใน: Scripts, Deployment files
  
- [ ] **Domain Name**
  - ตัวอย่าง: `beliv.muict.app`
  - จะใช้ใน: Ingress, Certificate
  
- [ ] **GCP Access/Credentials**
  - Service Account Key (JSON file)
  - หรือ IAM Role assignment
  
- [ ] **DNS Access** (อาจจะ)
  - ตั้งค่า DNS records เอง
  - หรืออาจารย์ตั้งค่าให้

---

## 🚀 Deployment Phase (หลังได้ข้อมูลจากอาจารย์)

### 📅 Day 1: Setup GCP (30 นาที)

#### 1. Login to GCP
```powershell
gcloud auth login
```
- [ ] Login สำเร็จ

#### 2. Set Project
```powershell
$PROJECT_ID = "muict-project-2025"  # แทนด้วย Project ID จริง
gcloud config set project $PROJECT_ID
```
- [ ] Set project สำเร็จ

#### 3. Enable APIs
```powershell
gcloud services enable container.googleapis.com
gcloud services enable containerregistry.googleapis.com
```
- [ ] APIs enabled

#### 4. Create GKE Cluster
```powershell
gcloud container clusters create beliv-cluster `
  --zone asia-southeast1-a `
  --num-nodes 3 `
  --machine-type e2-medium `
  --disk-size 30 `
  --enable-autorepair `
  --enable-autoupgrade
```
- [ ] Cluster created (ใช้เวลา 5-10 นาที)
- [ ] ตรวจสอบ: `kubectl get nodes` เห็น 3 nodes

#### 5. Install Traefik Ingress
```powershell
helm repo add traefik https://traefik.github.io/charts
helm repo update
helm install traefik traefik/traefik `
  --namespace kube-system `
  --set service.type=LoadBalancer
```
- [ ] Traefik installed
- [ ] ตรวจสอบ: `kubectl get svc -n kube-system traefik`

#### 6. Install cert-manager
```powershell
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml
```
- [ ] cert-manager installed
- [ ] รอ pods ready: `kubectl get pods -n cert-manager`

---

### 📅 Day 2: Build & Deploy (60 นาที)

#### 7. Update Configuration Files
```powershell
cd k8s-gcp

# แก้ไขไฟล์เหล่านี้:
# - frontend/deployment.yaml: แทน YOUR_PROJECT_ID
# - backend/deployment.yaml: แทน YOUR_PROJECT_ID และ domain
# - ingress/ingress-traefik.yaml: แทน beliv.muict.app ด้วย domain จริง
# - ingress/certificate.yaml: แทน beliv.muict.app และ email
```
- [ ] ไฟล์ทั้งหมด updated

#### 8. Build & Push Images
```powershell
.\build-and-push.ps1 -ProjectId "muict-project-2025"
```
- [ ] Backend image built & pushed
- [ ] Frontend image built & pushed
- [ ] ตรวจสอบ: `gcloud container images list --project=$PROJECT_ID`

#### 9. Deploy Application
```powershell
.\deploy.ps1 -ProjectId "muict-project-2025" -Domain "beliv.muict.app"
```
- [ ] Namespace created
- [ ] Database deployed & ready
- [ ] Backend deployed & ready
- [ ] Frontend deployed & ready
- [ ] Ingress deployed
- [ ] ตรวจสอบ: `kubectl get all -n superproject-ns`

#### 10. Get External IP
```powershell
kubectl get svc -n kube-system traefik
```
- [ ] External IP assigned (จด IP ไว้)
- IP Address: `_________________`

---

### 📅 Day 3: DNS & SSL (30 นาที)

#### 11. Configure DNS
ไปที่ DNS Provider (Google Domains/CloudFlare/etc.)

เพิ่ม A Record:
```
Type: A
Name: beliv.muict.app
Value: [External IP จาก step 10]
TTL: 300
```
- [ ] DNS record added
- [ ] รอ DNS propagate (5-10 นาที)
- [ ] ตรวจสอบ: `nslookup beliv.muict.app`

#### 12. Verify SSL Certificate
```powershell
kubectl describe certificate tls-certificate -n superproject-ns
```
- [ ] Certificate requested
- [ ] Certificate issued (2-5 นาที)
- [ ] Status = Ready

#### 13. Test Access
```powershell
# ทดสอบ HTTP (should redirect to HTTPS)
curl http://beliv.muict.app

# ทดสอบ HTTPS
curl https://beliv.muict.app
```
- [ ] Frontend accessible via HTTPS
- [ ] Backend API accessible via HTTPS
- [ ] No SSL warnings

---

## ✅ Post-Deployment Verification

### 🧪 Testing

#### Frontend
- [ ] เปิด `https://beliv.muict.app` ได้
- [ ] Login page แสดงถูกต้อง
- [ ] สามารถ login ได้
- [ ] Dashboard แสดงข้อมูล

#### Backend API
- [ ] API endpoints ตอบกลับถูกต้อง
- [ ] Database connection ทำงาน
- [ ] Authentication ทำงาน

#### Database
```powershell
kubectl exec -it -n superproject-ns statefulset/mysql -- mysql -u root -p
# Enter password
SHOW DATABASES;
USE apartment_db;
SHOW TABLES;
```
- [ ] Database accessible
- [ ] Tables created
- [ ] Sample data exists

### 📊 Monitoring

#### Check Pods
```powershell
kubectl get pods -n superproject-ns
```
- [ ] All pods in `Running` status
- [ ] No CrashLoopBackOff
- [ ] Restart count = 0

#### Check Logs
```powershell
# Backend logs
kubectl logs -n superproject-ns deployment/backend --tail=50

# Frontend logs
kubectl logs -n superproject-ns deployment/frontend --tail=50

# Database logs
kubectl logs -n superproject-ns statefulset/mysql --tail=50
```
- [ ] No errors in logs
- [ ] Application starting correctly

#### Check Resources
```powershell
kubectl top nodes
kubectl top pods -n superproject-ns
```
- [ ] CPU/Memory usage normal
- [ ] No resource exhaustion

---

## 🎯 Optional: Monitoring Stack

### Deploy Prometheus & Grafana
```powershell
cd monitor
.\deploy-monitoring.ps1
```
- [ ] Prometheus deployed
- [ ] Grafana deployed
- [ ] ServiceMonitor configured

### Add Monitoring Ingress
```powershell
cd k8s-gcp

# สร้าง monitoring/prometheus-ingress.yaml
# สร้าง monitoring/grafana-ingress.yaml

kubectl apply -f monitoring/
```
- [ ] Prometheus accessible: `https://prometheus.beliv.muict.app`
- [ ] Grafana accessible: `https://grafana.beliv.muict.app`

---

## 📝 Documentation

### สร้างเอกสาร
- [ ] README.md อธิบายวิธี access
- [ ] Architecture diagram
- [ ] API documentation
- [ ] User manual (ถ้าจำเป็น)

### Screenshots
- [ ] Dashboard
- [ ] Key features
- [ ] Monitoring dashboards

---

## 💰 Cost Monitoring

### ตรวจสอบค่าใช้จ่าย
- [ ] ตั้งค่า Budget alerts ใน GCP Console
- [ ] ตรวจสอบ daily cost
- [ ] Monitor quota usage

---

## 🆘 Troubleshooting Checklist

### Pods not starting
```powershell
kubectl describe pod -n superproject-ns <pod-name>
kubectl logs -n superproject-ns <pod-name>
```

### External IP pending
```powershell
kubectl describe svc -n kube-system traefik
gcloud compute project-info describe --project=$PROJECT_ID
```

### Domain not resolving
```powershell
nslookup beliv.muict.app
ping beliv.muict.app
```

### SSL Certificate not issuing
```powershell
kubectl describe certificate tls-certificate -n superproject-ns
kubectl logs -n cert-manager deployment/cert-manager -f
```

---

## 📞 Emergency Contacts

- **อาจารย์**: [Email/Phone]
- **GCP Support**: https://cloud.google.com/support
- **Team Members**: [Contact info]

---

## ✅ Final Checklist

- [ ] Application deployed successfully
- [ ] All components running
- [ ] HTTPS enabled
- [ ] Domain accessible
- [ ] Data persisted
- [ ] Monitoring working
- [ ] Documentation complete
- [ ] GitHub updated
- [ ] Demo prepared

---

**Last Updated**: [Date]  
**Deployed By**: [Name]  
**Project ID**: [GCP Project ID]  
**Domain**: [Domain Name]
