# 🌐 Access Your Kubernetes Application

Your application is now accessible via **multiple methods**!

## ✅ Option 1: Use Localhost (Easiest - No Setup Required)

Simply open your browser:

### **Frontend**
```
http://localhost/
```

### **Backend API**
```
http://localhost/api/
```

### **Test It**
```powershell
# Test frontend
curl http://localhost/

# Test backend health
curl http://localhost/api/actuator/health
```

---

## ✅ Option 2: Use Your Machine's IP Address

Find your IP and access directly:

```powershell
# Get your IP
ipconfig | Select-String "IPv4"

# Then access (example):
# http://192.168.1.100/
# http://172.20.10.3/
```

---

## ✅ Option 3: Port Forward (Direct Pod Access)

Bypass Ingress completely and connect directly to services:

### Forward Frontend
```powershell
kubectl port-forward -n superproject-ns svc/frontend-service 8080:80
```
Then open: http://localhost:8080

### Forward Backend
```powershell
kubectl port-forward -n superproject-ns svc/backend-service 8081:8080
```
Then open: http://localhost:8081/actuator/health

---

## 🎯 Recommended: Use Localhost

**Just use:** http://localhost

No need for:
- ❌ Editing hosts file
- ❌ Domain names
- ❌ DNS configuration

**Why?** The Ingress now accepts **any hostname**, so `localhost` works perfectly!

---

## 🧪 Quick Test

```powershell
# Test frontend
Start-Process "http://localhost/"

# Or with PowerShell
Invoke-WebRequest http://localhost/ -UseBasicParsing
```

---

## 📝 What Changed?

**Before:**
```yaml
rules:
  - host: apartment.local  # Required specific hostname
```

**After:**
```yaml
rules:
  - http:  # Accepts ANY hostname (localhost, IP, domain, etc.)
```

This means the Ingress will respond to:
- ✅ http://localhost/
- ✅ http://127.0.0.1/
- ✅ http://192.168.1.100/ (your actual IP)
- ✅ http://any-domain.com/ (if DNS points to your machine)

---

## 🚀 Default Login

Once you open http://localhost/, use these credentials:

- **Admin**: `admin` / `admin123`
- **User**: `testuser` / `test123`
- **Villager**: `villager` / `villager123`

---

## 🔍 Troubleshooting

### "Site can't be reached"
```powershell
# Check if Ingress controller is running
kubectl get pods -n ingress-nginx

# Check if your pods are ready
kubectl get pods -n superproject-ns
```

### "404 Not Found"
```powershell
# Check Ingress configuration
kubectl get ingress -n superproject-ns

# View Ingress logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/component=controller --tail=50
```

### Still having issues?
Use port-forward to bypass Ingress:
```powershell
kubectl port-forward -n superproject-ns svc/frontend-service 8080:80
# Then open: http://localhost:8080
```

---

## 🎉 You're All Set!

**Access your application now:**
```
http://localhost/
```

No domain name needed! 🚀
