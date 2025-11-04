# 🚀 Quick Start Guide - GCP Deployment

## เริ่มต้นอย่างรวดเร็ว (เมื่อได้ข้อมูลจากอาจารย์แล้ว)

### 📋 สิ่งที่ต้องมี
- ✅ GCP Project ID (จากอาจารย์)
- ✅ Domain name (จากอาจารย์)
- ✅ gcloud CLI, kubectl, helm ติดตั้งแล้ว

---

## ⚡ 5 ขั้นตอนสำคัญ

### 1️⃣ Setup GCP & Create Cluster (10 นาที)

```powershell
# Login
gcloud auth login

# Set project (แทน YOUR_PROJECT_ID)
gcloud config set project YOUR_PROJECT_ID

# Create cluster
gcloud container clusters create beliv-cluster `
  --zone asia-southeast1-a `
  --num-nodes 3 `
  --machine-type e2-medium

# Connect kubectl
gcloud container clusters get-credentials beliv-cluster `
  --zone asia-southeast1-a

# Install Traefik
helm repo add traefik https://traefik.github.io/charts
helm install traefik traefik/traefik `
  --namespace kube-system `
  --set service.type=LoadBalancer

# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml
```

---

### 2️⃣ Update Configuration (5 นาที)

```powershell
cd k8s-gcp

# แก้ไขไฟล์เหล่านี้:
# 1. frontend/deployment.yaml
#    - แทน: YOUR_PROJECT_ID → muict-project-2025

# 2. backend/deployment.yaml  
#    - แทน: YOUR_PROJECT_ID → muict-project-2025
#    - แทน: CORS domain → https://beliv.muict.app

# 3. ingress/ingress-traefik.yaml
#    - แทน: beliv.muict.app → your-domain.muict.app (ทุกที่)

# 4. ingress/certificate.yaml
#    - แทน: beliv.muict.app → your-domain.muict.app
#    - แทน: your-email@example.com → your-real-email@example.com
```

---

### 3️⃣ Build & Push Images (15 นาที)

```powershell
cd k8s-gcp
.\build-and-push.ps1 -ProjectId "muict-project-2025"
```

ถ้าเจอ error ให้ลองคำสั่งนี้:
```powershell
gcloud auth configure-docker
```

---

### 4️⃣ Deploy Application (10 นาที)

```powershell
.\deploy.ps1 -ProjectId "muict-project-2025" -Domain "beliv.muict.app"
```

รอจนกว่า script จะเสร็จและแสดง External IP

---

### 5️⃣ Configure DNS & Test (10 นาที)

```powershell
# 1. Get External IP
kubectl get svc -n kube-system traefik
# จด EXTERNAL-IP ไว้ เช่น: 34.87.123.45

# 2. ไปที่ DNS Provider แล้วเพิ่ม A Record:
#    Type: A
#    Name: beliv.muict.app
#    Value: 34.87.123.45
#    TTL: 300

# 3. รอ DNS propagate (5-10 นาที)
nslookup beliv.muict.app

# 4. ทดสอบเข้าเว็บ
# เปิด browser: https://beliv.muict.app
```

---

## ✅ เสร็จแล้ว!

Application ของคุณทำงานบน GCP แล้ว! 🎉

### 🔍 ตรวจสอบสถานะ

```powershell
# Pods
kubectl get pods -n superproject-ns

# Services
kubectl get svc -n superproject-ns

# Ingress
kubectl get ingress -n superproject-ns

# Certificate
kubectl get certificate -n superproject-ns
```

### 📊 ดู Logs

```powershell
# Backend
kubectl logs -n superproject-ns deployment/backend -f

# Frontend
kubectl logs -n superproject-ns deployment/frontend -f

# Database
kubectl logs -n superproject-ns statefulset/mysql -f
```

---

## 🔄 Update Code

เมื่อแก้ไข code แล้วต้องการ update:

```powershell
# Update backend
.\update.ps1 -Component backend -ProjectId "muict-project-2025"

# Update frontend
.\update.ps1 -Component frontend -ProjectId "muict-project-2025"

# Update ทั้งหมด
.\update.ps1 -Component all -ProjectId "muict-project-2025"
```

---

## 🗑️ Delete All Resources

```powershell
.\delete.ps1
```

---

## 🆘 หากเจอปัญหา

### Pods ไม่ Running
```powershell
kubectl describe pod -n superproject-ns <pod-name>
```

### External IP ยัง Pending
```powershell
# รออีก 2-3 นาที
kubectl get svc -n kube-system traefik --watch
```

### Domain ไม่ทำงาน
```powershell
# ตรวจสอบ DNS
nslookup beliv.muict.app

# ตรวจสอบ Ingress
kubectl describe ingress -n superproject-ns
```

### SSL Certificate ไม่ออก
```powershell
kubectl describe certificate tls-certificate -n superproject-ns
kubectl logs -n cert-manager deployment/cert-manager
```

---

## 📚 เอกสารเพิ่มเติม

- `README.md` - รายละเอียดทั้งหมด
- `DEPLOYMENT_CHECKLIST.md` - Checklist ครบวงจร
- `../DEPLOYMENT_GUIDE.md` - คู่มือแบบละเอียด

---

**Happy Deploying! 🚀**
