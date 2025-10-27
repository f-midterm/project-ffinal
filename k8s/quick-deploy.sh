#!/bin/bash
#
# One-command deployment script
# This script ensures K3s is running and deploys the application
#
# Usage: ./quick-deploy.sh

set -e

# Color output
write_section() { echo -e "\n\033[1;36m=== $1 ===\033[0m"; }
write_step() { echo -e "\033[0;36m- $1\033[0m"; }
write_success() { echo -e "\033[1;32m✓ $1\033[0m"; }
write_error() { echo -e "\033[1;31m✗ $1\033[0m"; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

write_section "Quick Deploy: Starting K3s and deploying application"

# Check if running as root
if [ "$EUID" -eq 0 ]; then
  write_error "Don't run this script as root/sudo"
  write_error "The script will prompt for sudo when needed"
  exit 1
fi

# Check if K3s is installed
if ! command -v k3s >/dev/null 2>&1; then
  write_error "K3s is not installed!"
  echo ""
  echo "Run: sudo ./library.sh install-all"
  exit 1
fi

# Start K3s if not running
if ! sudo systemctl is-active --quiet k3s 2>/dev/null; then
  write_step "K3s is not running, starting it..."
  sudo systemctl start k3s
  sudo systemctl enable k3s
  
  # Wait for K3s to be ready
  write_step "Waiting for K3s to be ready..."
  for i in {1..30}; do
    if sudo k3s kubectl get nodes >/dev/null 2>&1; then
      write_success "K3s is ready"
      break
    fi
    if [ $i -eq 30 ]; then
      write_error "K3s failed to start"
      exit 1
    fi
    sleep 2
  done
  
  # Configure kubeconfig
  mkdir -p ~/.kube
  sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
  sudo chown $USER:$USER ~/.kube/config
  chmod 600 ~/.kube/config
else
  write_success "K3s is already running"
fi

# Check if images need to be loaded into K3s
if docker images | grep -q "apartment-backend.*prod"; then
  write_step "Loading images into K3s..."
  docker save apartment-backend:prod | sudo k3s ctr images import - 2>/dev/null || true
  docker save apartment-frontend:prod | sudo k3s ctr images import - 2>/dev/null || true
  write_success "Images loaded"
fi

# Deploy
write_section "Deploying application"
./deploy.sh up

write_success "Deployment complete!"
