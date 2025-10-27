#!/bin/bash
#
# Switch kubectl context between K3s and Kind clusters
#
# Usage:
#   ./switch-cluster.sh k3s     # Switch to K3s
#   ./switch-cluster.sh kind    # Switch to Kind
#   ./switch-cluster.sh check   # Check current context

set -e

CLUSTER="${1:-check}"

# Color output
write_success() { echo -e "\033[1;32m✓ $1\033[0m"; }
write_error() { echo -e "\033[1;31m✗ $1\033[0m"; }
write_warning() { echo -e "\033[1;33m⚠ $1\033[0m"; }
write_info() { echo -e "\033[0;34mℹ $1\033[0m"; }
write_section() { echo -e "\n\033[1;36m=== $1 ===\033[0m"; }

check_current_context() {
  write_section "Current Kubernetes Context"
  
  if kubectl config current-context 2>/dev/null; then
    CURRENT=$(kubectl config current-context 2>/dev/null || echo "none")
    echo ""
    write_info "Current context: $CURRENT"
    
    # Try to connect
    if kubectl get nodes >/dev/null 2>&1; then
      write_success "Cluster is accessible"
      kubectl get nodes
    else
      write_warning "Cannot connect to cluster"
    fi
  else
    write_warning "No kubectl context set"
  fi
  
  echo ""
  write_section "Available Clusters"
  
  # Check K3s
  if systemctl is-active --quiet k3s 2>/dev/null; then
    write_success "K3s is running"
    if [ -f /etc/rancher/k3s/k3s.yaml ]; then
      write_info "  Config: /etc/rancher/k3s/k3s.yaml"
    fi
  else
    write_warning "K3s is not running"
  fi
  
  # Check Kind
  if docker ps | grep -q "kindest/node"; then
    CLUSTER_NAME=$(docker ps | grep "kindest/node" | awk '{print $NF}' | sed 's/-control-plane//')
    write_success "Kind cluster running: $CLUSTER_NAME"
    PORTS=$(docker ps | grep "kindest/node" | grep -oP '\d+:\d+->6443' || echo "unknown")
    write_info "  Port: $PORTS"
  else
    write_warning "Kind cluster not running"
  fi
}

switch_to_k3s() {
  write_section "Switching to K3s"
  
  # Check if K3s is running
  if ! systemctl is-active --quiet k3s 2>/dev/null; then
    write_error "K3s is not running!"
    write_info "Start it with: sudo systemctl start k3s"
    exit 1
  fi
  
  # Check if running as root (needed to copy config)
  if [ "$EUID" -ne 0 ]; then
    write_error "Please run with sudo to switch to K3s"
    write_info "sudo ./switch-cluster.sh k3s"
    exit 1
  fi
  
  # Copy K3s config
  if [ -n "$SUDO_USER" ]; then
    SUDO_HOME=$(eval echo ~$SUDO_USER)
    mkdir -p $SUDO_HOME/.kube
    cp /etc/rancher/k3s/k3s.yaml $SUDO_HOME/.kube/config
    chown -R $SUDO_USER:$SUDO_USER $SUDO_HOME/.kube
    chmod 600 $SUDO_HOME/.kube/config
  else
    mkdir -p ~/.kube
    cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
    chmod 600 ~/.kube/config
  fi
  
  write_success "Switched to K3s!"
  echo ""
  
  # Verify
  if sudo -u ${SUDO_USER:-$USER} kubectl get nodes 2>/dev/null; then
    write_success "K3s cluster is accessible"
    sudo -u ${SUDO_USER:-$USER} kubectl get nodes
  fi
  
  echo ""
  write_info "You can now run: ./deploy.sh up"
}

switch_to_kind() {
  write_section "Switching to Kind"
  
  # Check if Kind cluster is running
  if ! docker ps | grep -q "kindest/node"; then
    write_error "No Kind cluster is running!"
    write_info "Start it with: kind create cluster --name apartment-cluster"
    exit 1
  fi
  
  # Get cluster name
  CLUSTER_NAME=$(docker ps | grep "kindest/node" | awk '{print $NF}' | sed 's/-control-plane//')
  
  # Set kubectl context
  kubectl config use-context kind-${CLUSTER_NAME} 2>/dev/null || \
    kind get kubeconfig --name ${CLUSTER_NAME} > ~/.kube/config
  
  write_success "Switched to Kind cluster: $CLUSTER_NAME"
  echo ""
  
  # Verify
  if kubectl get nodes 2>/dev/null; then
    write_success "Kind cluster is accessible"
    kubectl get nodes
  fi
}

case $CLUSTER in
  k3s)
    switch_to_k3s
    ;;
  kind)
    switch_to_kind
    ;;
  check|status)
    check_current_context
    ;;
  *)
    write_error "Unknown cluster: $CLUSTER"
    echo ""
    echo "Usage: $0 {k3s|kind|check}"
    echo ""
    echo "Examples:"
    echo "  sudo ./switch-cluster.sh k3s     # Switch to K3s"
    echo "  ./switch-cluster.sh kind         # Switch to Kind"
    echo "  ./switch-cluster.sh check        # Check current context"
    exit 1
    ;;
esac
