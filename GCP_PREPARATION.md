# 🚀 คู่มือเตรียมความพร้อม GCP Deployment

## 📋 สิ่งที่จะได้รับจากอาจารย์ (คาดการณ์)

- [ ] GCP Project ID
- [ ] Domain name หรือ Subdomain
- [ ] Credentials สำหรับ GCP
- [ ] Budget/Quota ที่ใช้ได้

---

## 🛠️ เครื่องมือที่ต้องติดตั้ง

### 1. Google Cloud CLI (gcloud)
```powershell
# ดาวน์โหลดจาก: https://cloud.google.com/sdk/docs/install-sdk
# หลังติดตั้งเสร็จ
gcloud init
gcloud auth login
```

### 2. kubectl (ติดตั้งแล้ว)
```powershell
kubectl version --client
```

### 3. ตรวจสอบ Docker
```powershell
docker --version
```

---

## 🌐 ขั้นตอนการ Deploy ลง GCP (เมื่อพร้อม)

### **Option 1: Deploy ลง GKE (Google Kubernetes Engine)**

#### Step 1: สร้าง GKE Cluster
```bash
# Set project
gcloud config set project YOUR_PROJECT_ID

# สร้าง cluster (3 nodes, machine type e2-medium)
gcloud container clusters create project-cluster \
  --zone asia-southeast1-a \
  --num-nodes 3 \
  --machine-type e2-medium \
  --enable-autorepair \
  --enable-autoupgrade

# เชื่อมต่อ kubectl กับ cluster
gcloud container clusters get-credentials project-cluster \
  --zone asia-southeast1-a
```

#### Step 2: Deploy Application
```bash
# Deploy ทั้งหมด
cd k8s
kubectl apply -f namespace.yaml
kubectl apply -f database/
kubectl apply -f backend/
kubectl apply -f frontend/
kubectl apply -f ingress/
```

#### Step 3: ติดตั้ง Traefik Ingress Controller
```bash
# Add Traefik Helm repo
helm repo add traefik https://traefik.github.io/charts
helm repo update

# Install Traefik
helm install traefik traefik/traefik \
  --namespace kube-system \
  --set service.type=LoadBalancer
```

#### Step 4: รอ External IP
```bash
kubectl get svc -n kube-system traefik
# จดบันทึก EXTERNAL-IP
```

---

### **Option 2: Deploy ลง Compute Engine (VM)**

#### Step 1: สร้าง VM Instance
```bash
gcloud compute instances create k8s-node \
  --zone=asia-southeast1-a \
  --machine-type=e2-standard-4 \
  --boot-disk-size=50GB \
  --image-family=ubuntu-2204-lts \
  --image-project=ubuntu-os-cloud
```

#### Step 2: SSH เข้า VM
```bash
gcloud compute ssh k8s-node --zone=asia-southeast1-a
```

#### Step 3: ติดตั้ง K3s (Lightweight Kubernetes)
```bash
# บน VM
curl -sfL https://get.k3s.io | sh -

# ตรวจสอบ
sudo kubectl get nodes
```

---

## 🔧 Domain Name Configuration

### เมื่อได้ Domain มาแล้ว (เช่น `yourgroup.nvit.app`)

#### 1. ชี้ DNS ไปยัง External IP
```
Type: A Record
Name: @ (หรือ subdomain)
Value: YOUR_EXTERNAL_IP
TTL: 300
```

#### 2. ตั้งค่า Wildcard (ถ้าต้องการ subdomain หลายตัว)
```
Type: A Record
Name: *
Value: YOUR_EXTERNAL_IP
TTL: 300
```

#### 3. แก้ไข Ingress Configuration
```yaml
# k8s/ingress/ingress-traefik.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: main-ingress
  namespace: project-final
spec:
  rules:
  - host: yourgroup.nvit.app  # เปลี่ยน domain
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: frontend
            port:
              number: 80
      - path: /api
        pathType: Prefix
        backend:
          service:
            name: backend
            port:
              number: 8080
  - host: prometheus.yourgroup.nvit.app  # Monitoring subdomain
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: prometheus-server
            port:
              number: 9090
  - host: grafana.yourgroup.nvit.app
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: grafana
            port:
              number: 3000
```

---

## 🔐 SSL/TLS Certificate (HTTPS)

### ใช้ cert-manager (แนะนำ)
```bash
# ติดตั้ง cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# สร้าง ClusterIssuer สำหรับ Let's Encrypt
cat <<EOF | kubectl apply -f -
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: your-email@example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: traefik
EOF
```

### เพิ่ม Annotation ใน Ingress
```yaml
metadata:
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - yourgroup.nvit.app
    - prometheus.yourgroup.nvit.app
    - grafana.yourgroup.nvit.app
    secretName: tls-certificate
```

---

## 💾 Push Docker Images to GCP

### Option 1: Google Container Registry (GCR)
```bash
# Enable API
gcloud services enable containerregistry.googleapis.com

# Tag images
docker tag frontend:latest gcr.io/YOUR_PROJECT_ID/frontend:latest
docker tag backend:latest gcr.io/YOUR_PROJECT_ID/backend:latest

# Push to GCR
docker push gcr.io/YOUR_PROJECT_ID/frontend:latest
docker push gcr.io/YOUR_PROJECT_ID/backend:latest
```

### Option 2: Google Artifact Registry (แนะนำใหม่)
```bash
# Enable API
gcloud services enable artifactregistry.googleapis.com

# สร้าง repository
gcloud artifacts repositories create project-repo \
  --repository-format=docker \
  --location=asia-southeast1

# Configure Docker
gcloud auth configure-docker asia-southeast1-docker.pkg.dev

# Tag and Push
docker tag frontend:latest asia-southeast1-docker.pkg.dev/YOUR_PROJECT_ID/project-repo/frontend:latest
docker push asia-southeast1-docker.pkg.dev/YOUR_PROJECT_ID/project-repo/frontend:latest
```

---

## 📊 Monitoring บน GCP

### ใช้ Cloud Monitoring (Stackdriver) หรือ Deploy Prometheus/Grafana
```bash
# Deploy monitoring stack (ใช้ helm chart ที่มีอยู่แล้ว)
cd monitor
./deploy-monitoring.sh

# หรือใช้ GCP Cloud Monitoring
gcloud services enable monitoring.googleapis.com
```

---

## 💰 ประมาณการค่าใช้จ่าย (สำหรับแนวทาง)

### GKE (3 nodes, e2-medium)
- ~$75-100/เดือน
- **Free Tier**: GCP ให้ $300 credit สำหรับ 90 วัน

### Compute Engine (1 VM, e2-standard-4)
- ~$100-120/เดือน

### เคล็ดลับประหยัด:
- ใช้ Preemptible VMs (ถูกกว่า 60-80%)
- ปิด instance เมื่อไม่ใช้งาน
- ใช้ Auto-scaling

---

## 🔍 Troubleshooting

### ตรวจสอบ External IP
```bash
kubectl get svc -A | grep LoadBalancer
```

### ตรวจสอบ DNS
```powershell
nslookup yourgroup.nvit.app
```

### ดู Logs
```bash
# Frontend
kubectl logs -n project-final deployment/frontend

# Backend
kubectl logs -n project-final deployment/backend

# Ingress
kubectl logs -n kube-system deployment/traefik
```

### เข้าถึง Pod โดยตรง
```bash
kubectl port-forward -n project-final svc/frontend 8080:80
# เปิด browser: http://localhost:8080
```

---

## 📞 ติดต่ออาจารย์เมื่อ

- [ ] ไม่สามารถสร้าง GCP Project ได้
- [ ] ต้องการ Domain name
- [ ] ต้องการเพิ่ม Budget/Quota
- [ ] มีปัญหาเรื่อง Permissions

---

## ✅ Checklist ก่อน Deploy

### ตรวจสอบ Local
- [ ] ทุก pods ทำงานได้ปกติบน Minikube/Kind
- [ ] Frontend เข้าถึง Backend API ได้
- [ ] Database มีข้อมูลถูกต้อง
- [ ] Monitoring (Prometheus/Grafana) ทำงาน
- [ ] มี backup ของ database

### เตรียม GCP
- [ ] ติดตั้ง gcloud CLI แล้ว
- [ ] Login GCP account แล้ว
- [ ] มี Project ID
- [ ] เข้าใจ Architecture
- [ ] มี Domain name (รอจากอาจารย์)

### Documentation
- [ ] README.md อธิบายวิธี deploy
- [ ] มี Architecture Diagram
- [ ] มีคำอธิบาย Services ทั้งหมด
- [ ] มี Troubleshooting guide

---

## 📚 Resources

- [GCP Documentation](https://cloud.google.com/docs)
- [GKE Quickstart](https://cloud.google.com/kubernetes-engine/docs/quickstart)
- [Kubernetes Documentation](https://kubernetes.io/docs/home/)
- [Traefik Ingress](https://doc.traefik.io/traefik/providers/kubernetes-ingress/)

