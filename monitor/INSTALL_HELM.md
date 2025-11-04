# How to Install Helm on Windows

## เกิดอะไรขึ้น?

สคริปต์ `deploy-monitoring.ps1` ตรวจสอบและพบว่า Helm ยังไม่ได้ติดตั้งในระบบของคุณ

## วิธีติดตั้ง Helm (เลือกวิธีใดวิธีหนึ่ง)

### วิธีที่ 1: ดาวน์โหลดและติดตั้งด้วยตัวเอง (แนะนำ)

1. **ดาวน์โหลด Helm:**
   - ไปที่: https://github.com/helm/helm/releases/latest
   - ดาวน์โหลดไฟล์: `helm-v3.x.x-windows-amd64.zip`
   - หรือใช้ลิงก์ตรง: https://get.helm.sh/helm-v3.13.1-windows-amd64.zip

2. **แตกไฟล์:**
   - คลิกขวาที่ไฟล์ `.zip` → Extract All
   - จะได้โฟลเดอร์ `windows-amd64` ที่มีไฟล์ `helm.exe`

3. **ติดตั้ง (2 วิธี):**

   **วิธี A - ติดตั้งในโฟลเดอร์ monitoring (ไม่ต้อง Admin):**
   ```powershell
   # Copy helm.exe ไปที่โฟลเดอร์ monitoring
   Copy-Item "path\to\extracted\windows-amd64\helm.exe" -Destination "C:\Users\pipat\OneDrive\เอกสาร\GitHub\project-ffinal\monitoring\helm.exe"
   
   # แก้ไขสคริปต์ให้ใช้ helm.exe ในโฟลเดอร์เดียวกัน
   # ไม่ต้องทำอะไรเพิ่ม สคริปต์จะใช้ .\helm.exe ถ้าหา helm ไม่เจอ
   ```

   **วิธี B - ติดตั้งแบบ Global (ต้อง Admin):**
   ```powershell
   # Run PowerShell as Administrator
   
   # สร้างโฟลเดอร์
   New-Item -ItemType Directory -Path "C:\Program Files\Helm" -Force
   
   # Copy helm.exe
   Copy-Item "path\to\extracted\windows-amd64\helm.exe" -Destination "C:\Program Files\Helm\helm.exe"
   
   # เพิ่มเข้า PATH
   $path = [Environment]::GetEnvironmentVariable("Path", "Machine")
   $path += ";C:\Program Files\Helm"
   [Environment]::SetEnvironmentVariable("Path", $path, "Machine")
   
   # Restart PowerShell
   ```

4. **ตรวจสอบ:**
   ```powershell
   helm version
   # หรือถ้าติดตั้งแบบ local:
   .\helm.exe version
   ```

### วิธีที่ 2: ใช้ Chocolatey

ถ้ามี Chocolatey ติดตั้งอยู่แล้ว:

```powershell
# Run PowerShell as Administrator
choco install kubernetes-helm
```

### วิธีที่ 3: ใช้ WinGet (Windows 10/11)

```powershell
winget install Helm.Helm
```

### วิธีที่ 4: ใช้ Scoop

```powershell
scoop install helm
```

## หลังจากติดตั้ง Helm แล้ว

### ถ้าติดตั้งแบบ Global:
```powershell
cd C:\Users\pipat\OneDrive\เอกสาร\GitHub\project-ffinal\monitoring
.\deploy-monitoring.ps1
```

### ถ้าติดตั้งแบบ Local (helm.exe อยู่ในโฟลเดอร์ monitoring):
คุณต้องแก้ไขสคริปต์ `deploy-monitoring.ps1` เล็กน้อย:

1. เปิดไฟล์ `deploy-monitoring.ps1`
2. หาบรรทัดที่มี `helm` (ประมาณบรรทัด 52-55)
3. เปลี่ยนจาก `helm` เป็น `.\helm.exe`

หรือใช้คำสั่งนี้แทน:
```powershell
# สร้างสคริปต์ wrapper
'.\helm.exe $args' | Out-File -FilePath helm.ps1 -Encoding UTF8
Set-Alias -Name helm -Value "$PWD\helm.ps1"
.\deploy-monitoring.ps1
```

## Quick Fix: ติดตั้ง Helm ในโฟลเดอร์ปัจจุบัน

```powershell
# ดาวน์โหลด
Invoke-WebRequest -Uri "https://get.helm.sh/helm-v3.13.1-windows-amd64.zip" -OutFile "helm.zip"

# แตกไฟล์
Expand-Archive -Path "helm.zip" -DestinationPath "helm-temp" -Force

# Copy helm.exe
Copy-Item "helm-temp\windows-amd64\helm.exe" -Destination ".\helm.exe"

# ลบไฟล์ชั่วคราว
Remove-Item "helm.zip", "helm-temp" -Recurse -Force

# ทดสอบ
.\helm.exe version

# รันสคริปต์ด้วย local helm
# แก้ไขสคริปต์ให้ใช้ .\helm.exe แทน helm
```

## แก้ไขสคริปต์ให้ใช้ Local Helm

แก้ไขไฟล์ `deploy-monitoring.ps1`:

```powershell
# เพิ่มที่บรรทัดแรกของสคริปต์
param(
    [switch]$Uninstall,
    [string]$Namespace = "superproject-ns",
    [string]$HelmPath = "helm"  # <-- เพิ่มบรรทัดนี้
)

# แล้วใช้ $HelmPath แทน helm ทุกที่
# ตัวอย่าง:
$helmVersion = & $HelmPath version --short 2>$null
& $HelmPath repo add $HelmRepoName $HelmRepoUrl
# ... และอื่นๆ
```

จากนั้นรัน:
```powershell
.\deploy-monitoring.ps1 -HelmPath ".\helm.exe"
```

## ตัวเลือกที่ง่ายที่สุด

**ดาวน์โหลดและรัน 1 คำสั่ง:**

```powershell
# ดาวน์โหลด Helm
(New-Object System.Net.WebClient).DownloadFile("https://get.helm.sh/helm-v3.13.1-windows-amd64.zip", "$PWD\helm.zip")

# แตกไฟล์
Expand-Archive -Path "helm.zip" -DestinationPath "." -Force

# เคลื่อนย้าย helm.exe
Move-Item "windows-amd64\helm.exe" -Destination "helm.exe" -Force

# ลบไฟล์ที่ไม่ใช้
Remove-Item "helm.zip", "windows-amd64" -Recurse -Force

# ตรวจสอบ
.\helm.exe version
```

## หากยังมีปัญหา

ติดต่อหรือดูวิธีอื่นๆ ได้ที่:
- Helm Official Installation Guide: https://helm.sh/docs/intro/install/
- Helm GitHub Releases: https://github.com/helm/helm/releases

## หลังจากแก้ไขแล้ว

รันคำสั่งนี้เพื่อทดสอบ:
```powershell
# ถ้า helm ติดตั้ง global
helm version

# ถ้าใช้ local helm.exe
.\helm.exe version

# จากนั้นรันสคริปต์
.\deploy-monitoring.ps1
```
