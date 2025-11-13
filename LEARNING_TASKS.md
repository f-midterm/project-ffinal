# 🎯 Task List: สร้าง GCP Cluster ด้วย Account ของตัวเอง

## 📌 ข้อมูลโปรเจค
- **Domain**: `beliv.pipatpongpri.dev` (Cloudflare)
- **วัตถุประสงค์**: เรียนรู้และทดสอบก่อนทำจริงกับอาจารย์
- **Platform**: Google Cloud Platform (GCP)
- **ระยะเวลา**: 2-3 ชั่วโมง (ทำครั้งแรก)

---

## 📋 Phase 1: เตรียมความพร้อม (30 นาที)

### ✅ Task 1.1: สมัคร/ตรวจสอบ GCP Account
- [ ] สมัคร GCP ที่: https://cloud.google.com/free
  - ใช้บัตรเครดิต/เดบิต (ไม่มีค่าใช้จ่าย - Free Trial $300)
  - รับ **$300 credit** ใช้ได้ 90 วัน
- [ ] Verify email และ activate account
- [ ] เข้า GCP Console: https://console.cloud.google.com
- [ ] จด Account ID ไว้: `___________________`

**เอกสาร:** https://cloud.google.com/free/docs/gcp-free-tier

---

### ✅ Task 1.2: สร้าง GCP Project
- [ ] ไปที่: https://console.cloud.google.com/projectcreate
- [ ] สร้าง Project ใหม่:
  - **Project Name**: `apartment-learning` หรือชื่อที่ชอบ
  - **Project ID**: จะได้จากระบบ (ไม่ซ้ำใคร)
  - จดบันทึก Project ID: `___________________`
- [ ] เลือก Project นี้เป็น Active Project

**Screenshot:** เก็บไว้เป็นหลักฐาน

---

### ✅ Task 1.3: Enable Billing
- [ ] ไปที่: https://console.cloud.google.com/billing
- [ ] Link Project กับ Billing Account
- [ ] ตั้งค่า Budget Alert (แนะนำ $50-100)
  - จะแจ้งเตือนเมื่อใช้จ่ายเกิน
- [ ] Verify: Billing is enabled

**⚠️ สำคัญ:** ตั้ง Budget Alert เพื่อไม่ให้เกินงบประมาณ!

---

### ✅ Task 1.4: ติดตั้ง Tools บนเครื่อง
- [ ] **gcloud CLI**
  - Download: https://cloud.google.com/sdk/docs/install
  - ติดตั้งแล้วทดสอบ: `gcloud --version`
  
- [ ] **kubectl** (ถ้ายังไม่มี)
  ```powershell
  choco install kubernetes-cli
  kubectl version --client
  ```

- [ ] **helm**
  ```powershell
  choco install kubernetes-helm
  helm version
  ```

- [ ] **Docker Desktop** (มีแล้ว ✅)
  ```powershell
  docker --version
  ```

---

### ✅ Task 1.5: Login และ Configure gcloud
```powershell
# Login to GCP
gcloud auth login

# List projects
gcloud projects list

# Set your project (แทน PROJECT_ID)
gcloud config set project YOUR_PROJECT_ID

# Set default zone (ใช้ asia-southeast1-a ใกล้ไทย)
gcloud config set compute/zone asia-southeast1-a

# Verify configuration
gcloud config list
```

- [ ] Login สำเร็จ
- [ ] Set project สำเร็จ
- [ ] จดข้อมูล:
  - Project ID: `___________________`
  - Zone: `asia-southeast1-a`

---

## 📋 Phase 2: Setup Cloudflare DNS (15 นาที)

### ✅ Task 2.1: เตรียม Cloudflare
- [ ] Login Cloudflare: https://dash.cloudflare.com
- [ ] เลือก domain: `pipatpongpri.dev`
- [ ] ไปที่ DNS management

---

### ✅ Task 2.2: เตรียม A Record (รอ External IP ก่อน)
**หมายเหตุ:** ทำในขั้นตอนหลัง (Phase 4) เมื่อได้ External IP แล้ว

จะเพิ่ม A Record:
```
Type: A
Name: beliv
Content: [รอ External IP จาก GCP]
Proxy: Off (DNS Only) - สำคัญ!
TTL: Auto
```

- [ ] บันทึกขั้นตอนนี้ไว้ ทำทีหลัง

---

## 📋 Phase 3: สร้าง GKE Cluster (30 นาที)

### ✅ Task 3.1: Enable Required APIs
```powershell
# Enable Kubernetes Engine API
gcloud services enable container.googleapis.com

# Enable Container Registry API
gcloud services enable containerregistry.googleapis.com

# Verify
gcloud services list --enabled | Select-String "container"
```

- [ ] APIs enabled สำเร็จ

---

### ✅ Task 3.2: สร้าง GKE Cluster
```powershell
# สร้าง cluster (ใช้เวลา 5-10 นาที)
gcloud container clusters create beliv-learning-cluster `
  --zone asia-southeast1-a `
  --num-nodes 3 `
  --machine-type e2-medium `
  --disk-size 30 `
  --enable-autorepair `
  --enable-autoupgrade
```

**Specifications:**
- **Cluster Name**: `beliv-learning-cluster`
- **Zone**: `asia-southeast1-a` (Singapore - ใกล้ไทย)
- **Nodes**: 3 nodes
- **Machine Type**: e2-medium (2 vCPU, 4 GB RAM)
- **Disk**: 30 GB per node
- **Cost**: ~$75-85/เดือน (ใช้ Free Credit)

- [ ] คำสั่งรันสำเร็จ (ใช้เวลา 5-10 นาที)
- [ ] Cluster สร้างเสร็จ

---

### ✅ Task 3.3: Connect kubectl to GKE
```powershell
# เชื่อมต่อ kubectl กับ cluster
gcloud container clusters get-credentials beliv-learning-cluster `
  --zone asia-southeast1-a

# ตรวจสอบ nodes
kubectl get nodes

# ควรเห็น 3 nodes ในสถานะ Ready
```

- [ ] kubectl เชื่อมต่อ GKE สำเร็จ
- [ ] เห็น 3 nodes สถานะ Ready

---

## 📋 Phase 4: ติดตั้ง Ingress Controller (15 นาที)

### ✅ Task 4.1: Add Helm Repository
```powershell
# Add Traefik repo
helm repo add traefik https://traefik.github.io/charts

# Update repo
helm repo update

# Verify
helm search repo traefik
```

- [ ] Traefik repo added

---

### ✅ Task 4.2: Install Traefik Ingress
```powershell
# Install Traefik with LoadBalancer
helm install traefik traefik/traefik `
  --namespace kube-system `
  --set service.type=LoadBalancer `
  --wait

# ตรวจสอบ
kubectl get svc -n kube-system traefik
```

- [ ] Traefik installed
- [ ] รอ EXTERNAL-IP ประมาณ 2-3 นาที

---

### ✅ Task 4.3: Get External IP
```powershell
# ดู External IP (รอจนกว่าจะไม่เป็น <pending>)
kubectl get svc -n kube-system traefik --watch

# Ctrl+C เมื่อเห็น External IP แล้ว

# หรือใช้คำสั่งนี้
kubectl get svc -n kube-system traefik -o jsonpath='{.status.loadBalancer.ingress[0].ip}'
```

- [ ] ได้ External IP แล้ว
- **จดบันทึก External IP**: `___________________`

**ตัวอย่าง:** `34.87.123.45`

---

## 📋 Phase 5: Configure DNS (5 นาที)

### ✅ Task 5.1: เพิ่ม A Record ใน Cloudflare
- [ ] ไปที่ Cloudflare Dashboard
- [ ] เลือก domain `pipatpongpri.dev`
- [ ] ไปที่ DNS → Records
- [ ] Add record:
  ```
  Type: A
  Name: beliv
  Content: [External IP จาก Task 4.3]
  Proxy status: DNS only (ปิด Proxy - สำคัญ!)
  TTL: Auto
  ```
- [ ] Save

---

### ✅ Task 5.2: ทดสอบ DNS
```powershell
# รอ DNS propagate (1-5 นาที)
nslookup beliv.pipatpongpri.dev

# ควรได้ IP ที่ตรงกับ External IP
```

- [ ] DNS ตอบกลับถูกต้อง
- [ ] IP ตรงกับ External IP

---

## 📋 Phase 6: Install cert-manager (SSL) (10 นาที)

### ✅ Task 6.1: Install cert-manager
```powershell
# Install cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# รอ pods ready (ใช้เวลา 1-2 นาที)
kubectl wait --for=condition=ready pod `
  -l app.kubernetes.io/instance=cert-manager `
  -n cert-manager `
  --timeout=300s

# ตรวจสอบ
kubectl get pods -n cert-manager
```

- [ ] cert-manager installed
- [ ] ทุก pods ใน cert-manager namespace สถานะ Running

---

## 📋 Phase 7: Update และ Deploy Application (30 นาที)

### ✅ Task 7.1: Update Configuration Files
```powershell
cd k8s-gcp

# แก้ไขไฟล์ต่อไปนี้:
```

#### **7.1.1: frontend/deployment.yaml**
- [ ] เปิดไฟล์: `k8s-gcp/frontend/deployment.yaml`
- [ ] แทน `YOUR_PROJECT_ID` ด้วย Project ID จริง
  ```yaml
  image: gcr.io/apartment-learning/apartment-frontend:prod
  ```

#### **7.1.2: backend/deployment.yaml**
- [ ] เปิดไฟล์: `k8s-gcp/backend/deployment.yaml`
- [ ] แทน `YOUR_PROJECT_ID` ด้วย Project ID จริง
  ```yaml
  image: gcr.io/apartment-learning/apartment-backend:prod
  ```
- [ ] แทน CORS domain:
  ```yaml
  - name: CORS_ALLOWED_ORIGINS
    value: "https://beliv.pipatpongpri.dev"
  ```

#### **7.1.3: ingress/ingress-traefik.yaml**
- [ ] เปิดไฟล์: `k8s-gcp/ingress/ingress-traefik.yaml`
- [ ] แทน `beliv.muict.app` ทุกที่ด้วย `beliv.pipatpongpri.dev`

#### **7.1.4: ingress/certificate.yaml**
- [ ] เปิดไฟล์: `k8s-gcp/ingress/certificate.yaml`
- [ ] แทน `beliv.muict.app` ด้วย `beliv.pipatpongpri.dev`
- [ ] แทน `your-email@example.com` ด้วย email จริง

---

### ✅ Task 7.2: Build และ Push Docker Images
```powershell
cd k8s-gcp

# Build and push (ใช้เวลา 10-15 นาที)
.\build-and-push.ps1 -ProjectId "apartment-learning"
```

- [ ] Backend image built & pushed
- [ ] Frontend image built & pushed
- [ ] ตรวจสอบ: `gcloud container images list`

**หากเจอ Error:**
```powershell
# Configure Docker for GCR
gcloud auth configure-docker
```

---

### ✅ Task 7.3: Deploy Application
```powershell
# Deploy (ใช้เวลา 5-10 นาที)
.\deploy.ps1 -ProjectId "apartment-learning" -Domain "beliv.pipatpongpri.dev"
```

- [ ] Namespace created
- [ ] Database deployed
- [ ] Backend deployed
- [ ] Frontend deployed
- [ ] Ingress deployed

---

### ✅ Task 7.4: ตรวจสอบ Deployment
```powershell
# ดู pods ทั้งหมด
kubectl get pods -n superproject-ns

# ดู services
kubectl get svc -n superproject-ns

# ดู ingress
kubectl get ingress -n superproject-ns

# ตรวจสอบ certificate
kubectl get certificate -n superproject-ns
```

- [ ] ทุก pods สถานะ Running (อาจใช้เวลา 2-3 นาที)
- [ ] Certificate status: Ready (ใช้เวลา 2-5 นาที)

---

## 📋 Phase 8: ทดสอบ Application (15 นาที)

### ✅ Task 8.1: รอ SSL Certificate
```powershell
# ตรวจสอบสถานะ certificate
kubectl describe certificate tls-certificate -n superproject-ns

# รอจนกว่า Status จะเป็น "Certificate is up to date and has not expired"
```

- [ ] Certificate issued successfully
- [ ] Status: Ready

**หากค้าง:** รอ 5-10 นาที Let's Encrypt ต้องใช้เวลา validate

---

### ✅ Task 8.2: ทดสอบเข้าเว็บ
- [ ] เปิด browser: `https://beliv.pipatpongpri.dev`
- [ ] เว็บโหลดได้ (อาจต้องรอ DNS propagate)
- [ ] ไม่มี SSL warning (🔒 ใน address bar)
- [ ] Login page แสดง
- [ ] ทดสอบ login (ถ้ามี account)

---

### ✅ Task 8.3: ทดสอบ Backend API
```powershell
# ทดสอบ API endpoint
curl https://beliv.pipatpongpri.dev/api/health

# หรือ
Invoke-WebRequest -Uri "https://beliv.pipatpongpri.dev/api/health"
```

- [ ] API ตอบกลับ
- [ ] Status code: 200

---

### ✅ Task 8.4: ตรวจสอบ Logs
```powershell
# Backend logs
kubectl logs -n superproject-ns deployment/backend --tail=50

# Frontend logs
kubectl logs -n superproject-ns deployment/frontend --tail=50

# Database logs
kubectl logs -n superproject-ns statefulset/mysql --tail=50
```

- [ ] ไม่มี error ร้ายแรง
- [ ] Application ทำงานปกติ

---

## 📋 Phase 9: Monitoring (Optional - 20 นาที)

### ✅ Task 9.1: Deploy Monitoring Stack
```powershell
cd monitor
.\deploy-monitoring.ps1
```

- [ ] Prometheus deployed
- [ ] Grafana deployed

---

### ✅ Task 9.2: เข้าถึง Monitoring
```powershell
# Port forward Grafana
kubectl port-forward -n monitoring svc/grafana 3000:80

# เปิด browser: http://localhost:3000
# Username: admin
# Password: ดูใน grafana-admin-secret.yaml
```

- [ ] Grafana accessible
- [ ] Dashboards มีข้อมูล

---

## 📋 Phase 10: บันทึกและ Documentation (15 นาที)

### ✅ Task 10.1: Screenshot สำคัญ
- [ ] GCP Console - Cluster overview
- [ ] GCP Console - Workloads
- [ ] Cloudflare DNS records
- [ ] Website homepage (https://beliv.pipatpongpri.dev)
- [ ] Grafana dashboard

---

### ✅ Task 10.2: บันทึกข้อมูล
สร้างไฟล์: `LEARNING_DEPLOYMENT.md`

```markdown
# My Learning Deployment

## Project Info
- Project ID: [YOUR_PROJECT_ID]
- Domain: beliv.pipatpongpri.dev
- External IP: [YOUR_EXTERNAL_IP]
- Cluster: beliv-learning-cluster
- Zone: asia-southeast1-a

## Access URLs
- Frontend: https://beliv.pipatpongpri.dev
- Backend API: https://beliv.pipatpongpri.dev/api
- Grafana: [port-forward]

## Costs
- Start Date: [DATE]
- Estimated: ~$3/day
- Free Credit Remaining: $XXX

## Lessons Learned
1. [บันทึกสิ่งที่เรียนรู้]
2. [ปัญหาที่เจอและแก้ไข]
3. [สิ่งที่จะทำต่อ]
```

- [ ] สร้างไฟล์บันทึก
- [ ] Commit ขึ้น GitHub

---

## 📋 Phase 11: Cleanup (เมื่อเรียนรู้เสร็จ)

### ⚠️ สำคัญ: ลบทรัพยากรเพื่อไม่เสีย Credit

### ✅ Task 11.1: Delete Application
```powershell
cd k8s-gcp
.\delete.ps1
```

- [ ] Application deleted

---

### ✅ Task 11.2: Delete Cluster
```powershell
# ลบ cluster (จะหยุดค่าใช้จ่าย)
gcloud container clusters delete beliv-learning-cluster `
  --zone asia-southeast1-a `
  --quiet
```

- [ ] Cluster deleted

---

### ✅ Task 11.3: Delete Images (Optional)
```powershell
# ลบ Docker images ใน GCR
gcloud container images delete gcr.io/PROJECT_ID/apartment-frontend:prod --quiet
gcloud container images delete gcr.io/PROJECT_ID/apartment-backend:prod --quiet
```

- [ ] Images deleted

---

### ✅ Task 11.4: ตรวจสอบค่าใช้จ่าย
- [ ] ไปที่: https://console.cloud.google.com/billing
- [ ] ดู total cost
- [ ] จดบันทึก: ใช้ $_______ จาก $300 credit

---

## 📊 Summary Checklist

### เตรียมความพร้อม
- [ ] GCP Account + Free Credit $300
- [ ] สร้าง Project
- [ ] Enable Billing + Budget Alert
- [ ] ติดตั้ง Tools (gcloud, kubectl, helm)

### สร้าง Infrastructure
- [ ] สร้าง GKE Cluster (3 nodes)
- [ ] ติดตั้ง Traefik Ingress
- [ ] ได้ External IP
- [ ] ติดตั้ง cert-manager

### Configure
- [ ] Update config files (Project ID, Domain)
- [ ] Configure Cloudflare DNS
- [ ] Build & Push Docker images

### Deploy
- [ ] Deploy application
- [ ] รอ SSL certificate
- [ ] ทดสอบ website

### Optional
- [ ] Deploy monitoring
- [ ] เข้าถึง Grafana

### Cleanup
- [ ] บันทึก screenshots
- [ ] Commit to GitHub
- [ ] ลบ resources (เมื่อเรียนรู้เสร็จ)

---

## 💰 Cost Estimate

### ค่าใช้จ่ายโดยประมาณ:
- **GKE Cluster (3 x e2-medium)**: ~$2.50/วัน
- **Load Balancer**: ~$0.60/วัน
- **Persistent Disk (15GB)**: ~$0.10/วัน
- **Network Egress**: ~$0.10-0.30/วัน

**รวมประมาณ**: **$3-4/วัน** หรือ **~$100/เดือน**

### Free Credit:
- เริ่มต้น: $300
- ทดลอง 1 สัปดาห์: ใช้ ~$21-28
- คงเหลือ: ~$272-279

---

## 🎯 Learning Objectives

หลังทำเสร็จคุณจะได้:
1. ✅ เข้าใจวิธีสร้าง GKE Cluster
2. ✅ เข้าใจการ configure Ingress + SSL
3. ✅ เข้าใจ DNS configuration
4. ✅ เข้าใจการ deploy application จริง
5. ✅ พร้อมสำหรับการทำงานกับอาจารย์

---

## 📞 Help & Resources

### เจอปัญหา?
1. อ่าน error message ให้ดี
2. ใช้ `kubectl describe` และ `kubectl logs`
3. Google error message
4. ดู GCP Console - Workloads

### Useful Commands:
```powershell
# ดูสถานะทั้งหมด
kubectl get all -n superproject-ns

# ดู logs
kubectl logs -n superproject-ns deployment/backend -f

# Restart deployment
kubectl rollout restart -n superproject-ns deployment/backend

# Port forward
kubectl port-forward -n superproject-ns svc/frontend-service 8080:80
```

### Documentation:
- GCP: https://cloud.google.com/docs
- Kubernetes: https://kubernetes.io/docs/
- Traefik: https://doc.traefik.io/traefik/
- cert-manager: https://cert-manager.io/docs/

---

**Created**: November 4, 2025  
**Domain**: beliv.pipatpongpri.dev  
**Goal**: เรียนรู้และทดสอบก่อนทำจริงกับอาจารย์  
**Status**: Ready to Start! 🚀

---

## ✅ Quick Start Commands

```powershell
# === Phase 3: Create Cluster ===
gcloud container clusters create beliv-learning-cluster --zone asia-southeast1-a --num-nodes 3 --machine-type e2-medium
gcloud container clusters get-credentials beliv-learning-cluster --zone asia-southeast1-a

# === Phase 4: Install Traefik ===
helm repo add traefik https://traefik.github.io/charts
helm install traefik traefik/traefik --namespace kube-system --set service.type=LoadBalancer

# === Phase 6: Install cert-manager ===
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# === Phase 7: Deploy ===
cd k8s-gcp
.\build-and-push.ps1 -ProjectId "YOUR_PROJECT_ID"
.\deploy.ps1 -ProjectId "YOUR_PROJECT_ID" -Domain "beliv.pipatpongpri.dev"
```

**Happy Learning! 🎓**
