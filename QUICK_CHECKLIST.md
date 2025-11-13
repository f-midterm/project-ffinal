# ✅ Quick Checklist - Deploy to GCP

## 🎯 Domain: beliv.pipatpongpri.dev

---

## Day 1: Setup (1-2 ชั่วโมง)

### GCP Setup
- [ ] สมัคร GCP (ได้ $300 credit)
- [ ] สร้าง Project: `__________________`
- [ ] Enable Billing + Budget Alert
- [ ] ติดตั้ง gcloud CLI
- [ ] Login: `gcloud auth login`

### Tools
- [ ] kubectl installed
- [ ] helm installed  
- [ ] Docker running

### Create Cluster
```powershell
gcloud container clusters create beliv-learning-cluster `
  --zone asia-southeast1-a --num-nodes 3 --machine-type e2-medium
```
- [ ] Cluster created (5-10 นาที)
- [ ] kubectl connected

### Install Ingress
```powershell
helm install traefik traefik/traefik `
  --namespace kube-system --set service.type=LoadBalancer
```
- [ ] Traefik installed
- [ ] External IP: `__________________`

### DNS Configuration
- [ ] Login Cloudflare
- [ ] Add A Record:
  - Name: `beliv`
  - Content: `[External IP]`
  - Proxy: ❌ OFF (DNS only)
- [ ] Test: `nslookup beliv.pipatpongpri.dev`

---

## Day 2: Deploy Application (1-2 ชั่วโมง)

### Install cert-manager
```powershell
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml
```
- [ ] cert-manager ready

### Update Config Files
- [ ] `frontend/deployment.yaml` → Project ID
- [ ] `backend/deployment.yaml` → Project ID + domain
- [ ] `ingress/*.yaml` → domain + email

### Build & Deploy
```powershell
cd k8s-gcp
.\build-and-push.ps1 -ProjectId "YOUR_PROJECT_ID"
.\deploy.ps1 -ProjectId "YOUR_PROJECT_ID" -Domain "beliv.pipatpongpri.dev"
```
- [ ] Images pushed to GCR
- [ ] Application deployed
- [ ] All pods Running

### Test
- [ ] SSL certificate ready (2-5 นาที)
- [ ] Open: `https://beliv.pipatpongpri.dev`
- [ ] Website loads ✅
- [ ] Login works ✅

---

## Cleanup (เมื่อเรียนรู้เสร็จ)

```powershell
# Delete app
cd k8s-gcp
.\delete.ps1

# Delete cluster
gcloud container clusters delete beliv-learning-cluster --zone asia-southeast1-a --quiet
```

- [ ] Application deleted
- [ ] Cluster deleted
- [ ] Cost: $_______ used

---

## 📊 Progress Tracking

| Phase | Status | Time Spent | Notes |
|-------|--------|------------|-------|
| GCP Setup | ⬜ | ___ min | |
| Cluster Created | ⬜ | ___ min | |
| Ingress + DNS | ⬜ | ___ min | |
| cert-manager | ⬜ | ___ min | |
| Build Images | ⬜ | ___ min | |
| Deploy App | ⬜ | ___ min | |
| Testing | ⬜ | ___ min | |
| **Total** | | **___ min** | |

---

## 🆘 Common Issues

### Pods not starting
```powershell
kubectl describe pod -n superproject-ns [pod-name]
kubectl logs -n superproject-ns [pod-name]
```

### External IP pending
Wait 2-3 minutes, then:
```powershell
kubectl get svc -n kube-system traefik
```

### SSL not ready
```powershell
kubectl describe certificate tls-certificate -n superproject-ns
# Wait 5-10 minutes for Let's Encrypt
```

### DNS not resolving
```powershell
nslookup beliv.pipatpongpri.dev
# Wait 1-5 minutes for propagation
```

---

**Start Date**: ___________  
**End Date**: ___________  
**Status**: ⬜ Not Started | 🔄 In Progress | ✅ Completed
