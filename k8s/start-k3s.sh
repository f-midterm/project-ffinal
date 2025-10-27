#!/bin/bash
#
# Start and configure K3s for deployment
#
# This script:
#   - Starts K3s service
#   - Configures kubectl context
#   - Loads Docker images into K3s
#   - Verifies cluster is ready
#
# Usage:
#   sudo ./start-k3s.sh [OPTIONS]
#
# Options:
#   --load-images    - Load apartment images into K3s
#   --stop-kind      - Stop Kind cluster if running
#   --restart        - Restart K3s service

set -e

# Default options
LOAD_IMAGES=false
STOP_KIND=false
RESTART_K3S=false

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --load-images)
      LOAD_IMAGES=true
      shift
      ;;
    --stop-kind)
      STOP_KIND=true
      shift
      ;;
    --restart)
      RESTART_K3S=true
      shift
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

# Color output functions
write_section() {
  echo -e "\n\033[1;36m=== $1 ===\033[0m"
}

write_step() {
  echo -e "\033[0;36m- $1\033[0m"
}

write_warning() {
  echo -e "\033[1;33m$1\033[0m"
}

write_success() {
  echo -e "\033[1;32m✓ $1\033[0m"
}

write_error() {
  echo -e "\033[1;31m✗ $1\033[0m"
}

write_info() {
  echo -e "\033[0;34mℹ $1\033[0m"
}

# Check if running as root
if [ "$EUID" -ne 0 ]; then
  write_error "This script must be run as root or with sudo"
  exit 1
fi

# Stop Kind cluster if requested
if [ "$STOP_KIND" = true ]; then
  write_section "Stopping Kind cluster"
  if command -v kind >/dev/null 2>&1; then
    if docker ps | grep -q "kindest/node"; then
      write_step "Stopping Kind cluster..."
      kind delete cluster --name apartment-cluster 2>/dev/null || true
      write_success "Kind cluster stopped"
    else
      write_info "No Kind cluster running"
    fi
  fi
fi

# Start or restart K3s
write_section "Starting K3s"

if [ "$RESTART_K3S" = true ]; then
  write_step "Restarting K3s service..."
  systemctl restart k3s
else
  if systemctl is-active --quiet k3s; then
    write_success "K3s is already running"
  else
    write_step "Starting K3s service..."
    systemctl start k3s
  fi
fi

# Enable K3s to start on boot
write_step "Enabling K3s on boot..."
systemctl enable k3s

# Wait for K3s to be ready
write_step "Waiting for K3s to be ready..."
for i in {1..30}; do
  if k3s kubectl get nodes >/dev/null 2>&1; then
    write_success "K3s is ready"
    break
  fi
  if [ $i -eq 30 ]; then
    write_error "K3s failed to start after 60 seconds"
    systemctl status k3s
    exit 1
  fi
  sleep 2
done

# Configure kubeconfig for regular user
if [ -n "$SUDO_USER" ]; then
  SUDO_HOME=$(eval echo ~$SUDO_USER)
  write_step "Configuring kubeconfig for $SUDO_USER..."
  
  mkdir -p $SUDO_HOME/.kube
  cp /etc/rancher/k3s/k3s.yaml $SUDO_HOME/.kube/config
  chown -R $SUDO_USER:$SUDO_USER $SUDO_HOME/.kube
  chmod 600 $SUDO_HOME/.kube/config
  
  write_success "Kubeconfig configured at $SUDO_HOME/.kube/config"
fi

# Show cluster info
write_section "Cluster Information"
k3s kubectl get nodes
k3s kubectl cluster-info

# Load images into K3s if requested
if [ "$LOAD_IMAGES" = true ]; then
  write_section "Loading Docker images into K3s"
  
  # Check if images exist
  if docker images | grep -q "apartment-backend.*prod"; then
    write_step "Loading apartment-backend:prod..."
    docker save apartment-backend:prod | k3s ctr images import -
    write_success "Backend image loaded"
  else
    write_warning "apartment-backend:prod not found in Docker"
  fi
  
  if docker images | grep -q "apartment-frontend.*prod"; then
    write_step "Loading apartment-frontend:prod..."
    docker save apartment-frontend:prod | k3s ctr images import -
    write_success "Frontend image loaded"
  else
    write_warning "apartment-frontend:prod not found in Docker"
  fi
  
  # List loaded images
  write_section "Images in K3s"
  k3s ctr images ls | grep -E "(apartment-|REPOSITORY)" || echo "No apartment images found"
fi

# Final status
write_section "Status"
write_success "K3s is running and ready!"
write_info "You can now run: ./deploy.sh up"

# Show useful commands
echo ""
write_info "Useful commands:"
echo "  Check cluster:    kubectl get nodes"
echo "  View pods:        kubectl get pods -A"
echo "  Stop K3s:         sudo systemctl stop k3s"
echo "  Restart K3s:      sudo systemctl restart k3s"
echo "  K3s logs:         sudo journalctl -u k3s -f"
