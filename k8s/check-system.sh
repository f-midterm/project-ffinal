#!/bin/bash
#
# Check system status and provide actionable recommendations
#
# Usage: ./check-system.sh

# Color output
GREEN='\033[1;32m'
RED='\033[1;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[1;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║       K3s Deployment System Status Check                      ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Check Docker
echo -e "${CYAN}[1/5] Docker${NC}"
if command -v docker >/dev/null 2>&1; then
    if systemctl is-active --quiet docker 2>/dev/null || docker info >/dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} Docker installed and running"
        docker --version | sed 's/^/    /'
    else
        echo -e "  ${YELLOW}⚠${NC} Docker installed but not running"
        echo -e "    ${BLUE}→${NC} Run: sudo systemctl start docker"
    fi
else
    echo -e "  ${RED}✗${NC} Docker not installed"
    echo -e "    ${BLUE}→${NC} Run: sudo ./library.sh install-docker"
fi
echo ""

# Check K3s
echo -e "${CYAN}[2/5] K3s${NC}"
if command -v k3s >/dev/null 2>&1; then
    if systemctl is-active --quiet k3s 2>/dev/null; then
        echo -e "  ${GREEN}✓${NC} K3s installed and running"
        k3s --version | head -n1 | sed 's/^/    /'
        
        # Check cluster
        if k3s kubectl get nodes >/dev/null 2>&1; then
            echo -e "  ${GREEN}✓${NC} K3s cluster accessible"
            k3s kubectl get nodes 2>/dev/null | sed 's/^/    /'
        else
            echo -e "  ${YELLOW}⚠${NC} K3s running but cluster not accessible"
        fi
    else
        echo -e "  ${YELLOW}⚠${NC} K3s installed but NOT running"
        echo -e "    ${BLUE}→${NC} Run: sudo ./library.sh start-k3s"
        echo -e "    ${BLUE}→${NC} Or:  sudo systemctl start k3s"
    fi
else
    echo -e "  ${RED}✗${NC} K3s not installed"
    echo -e "    ${BLUE}→${NC} Run: sudo ./library.sh install-k3s"
fi
echo ""

# Check kubectl
echo -e "${CYAN}[3/5] kubectl${NC}"
if command -v kubectl >/dev/null 2>&1; then
    echo -e "  ${GREEN}✓${NC} kubectl installed"
    kubectl version --client --short 2>/dev/null || kubectl version --client 2>/dev/null | head -n1 | sed 's/^/    /'
    
    # Check connectivity
    if kubectl get nodes >/dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} Can connect to cluster"
    else
        echo -e "  ${YELLOW}⚠${NC} Cannot connect to cluster"
        echo -e "    ${BLUE}→${NC} Check if K3s is running"
        echo -e "    ${BLUE}→${NC} Run: sudo ./library.sh start-k3s"
    fi
else
    echo -e "  ${RED}✗${NC} kubectl not installed"
    echo -e "    ${BLUE}→${NC} Run: sudo ./library.sh install-tools"
fi
echo ""

# Check additional tools
echo -e "${CYAN}[4/5] Additional Tools${NC}"
if command -v jq >/dev/null 2>&1; then
    echo -e "  ${GREEN}✓${NC} jq installed ($(jq --version))"
else
    echo -e "  ${YELLOW}⚠${NC} jq not installed (recommended)"
    echo -e "    ${BLUE}→${NC} Run: sudo ./library.sh install-tools"
fi

if command -v curl >/dev/null 2>&1; then
    echo -e "  ${GREEN}✓${NC} curl installed"
else
    echo -e "  ${YELLOW}⚠${NC} curl not installed"
fi
echo ""

# Check deployment
echo -e "${CYAN}[5/5] Application Deployment${NC}"
if kubectl get namespace superproject-ns >/dev/null 2>&1; then
    echo -e "  ${GREEN}✓${NC} Namespace 'superproject-ns' exists"
    
    # Check pods
    POD_COUNT=$(kubectl get pods -n superproject-ns --no-headers 2>/dev/null | wc -l)
    RUNNING_PODS=$(kubectl get pods -n superproject-ns --no-headers 2>/dev/null | grep -c "Running" || echo "0")
    
    if [ "$POD_COUNT" -gt 0 ]; then
        if [ "$RUNNING_PODS" -eq "$POD_COUNT" ]; then
            echo -e "  ${GREEN}✓${NC} All pods running ($RUNNING_PODS/$POD_COUNT)"
        else
            echo -e "  ${YELLOW}⚠${NC} Some pods not ready ($RUNNING_PODS/$POD_COUNT running)"
            echo ""
            kubectl get pods -n superproject-ns 2>/dev/null | sed 's/^/    /'
        fi
    else
        echo -e "  ${YELLOW}⚠${NC} No pods found in namespace"
        echo -e "    ${BLUE}→${NC} Run: ./deploy.sh up"
    fi
    
    # Check services
    if kubectl get svc frontend-service -n superproject-ns >/dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} Services deployed"
    fi
    
    # Check ingress
    if kubectl get ingress -n superproject-ns >/dev/null 2>&1; then
        echo -e "  ${GREEN}✓${NC} Ingress configured"
    fi
else
    echo -e "  ${YELLOW}⚠${NC} Application not deployed"
    echo -e "    ${BLUE}→${NC} Run: ./deploy.sh up"
fi
echo ""

# Summary and recommendations
echo -e "${CYAN}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║  Recommendations                                               ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Determine what needs to be done
NEEDS_INSTALL=false
NEEDS_START=false
NEEDS_DEPLOY=false

if ! command -v k3s >/dev/null 2>&1; then
    NEEDS_INSTALL=true
elif ! systemctl is-active --quiet k3s 2>/dev/null; then
    NEEDS_START=true
elif ! kubectl get namespace superproject-ns >/dev/null 2>&1; then
    NEEDS_DEPLOY=true
else
    POD_COUNT=$(kubectl get pods -n superproject-ns --no-headers 2>/dev/null | wc -l)
    if [ "$POD_COUNT" -eq 0 ]; then
        NEEDS_DEPLOY=true
    fi
fi

if [ "$NEEDS_INSTALL" = true ]; then
    echo -e "${YELLOW}Action needed:${NC} Install prerequisites"
    echo -e "  ${BLUE}→${NC} sudo ./library.sh install-all"
    echo -e "  ${BLUE}→${NC} sudo ./library.sh start-k3s"
    echo -e "  ${BLUE}→${NC} ./deploy.sh up"
elif [ "$NEEDS_START" = true ]; then
    echo -e "${YELLOW}Action needed:${NC} Start K3s"
    echo -e "  ${BLUE}→${NC} sudo ./library.sh start-k3s"
    echo -e "  ${BLUE}→${NC} ./deploy.sh up"
elif [ "$NEEDS_DEPLOY" = true ]; then
    echo -e "${YELLOW}Action needed:${NC} Deploy application"
    echo -e "  ${BLUE}→${NC} ./deploy.sh up"
    echo -e "  ${BLUE}→${NC} Or use: ./quick-deploy.sh"
else
    echo -e "${GREEN}✓ Everything looks good!${NC}"
    echo ""
    echo -e "Your application should be accessible at:"
    echo -e "  ${GREEN}→${NC} Frontend: http://localhost/"
    echo -e "  ${GREEN}→${NC} API:      http://localhost/api/health"
    echo ""
    echo -e "Useful commands:"
    echo -e "  ${BLUE}→${NC} ./deploy.sh status          # Check deployment status"
    echo -e "  ${BLUE}→${NC} ./deploy.sh logs --target backend   # View logs"
    echo -e "  ${BLUE}→${NC} kubectl get pods -n superproject-ns # List pods"
fi

echo ""
echo -e "${CYAN}════════════════════════════════════════════════════════════════${NC}"
