# Apartment Management System

A comprehensive apartment rental management system with **automated lease lifecycle management**, booking system, and admin dashboard.

## Key Features

- **Unit Management**: Manage 24 rental units across 2 floors
- **Booking System**: User-friendly rental request submission with real-time status tracking
- **Tenant Management**: Complete tenant lifecycle from booking to lease expiration
- **Payment Tracking**: Track rent payments, deposits, and other charges
- **Maintenance Requests**: Submit and track maintenance issues
- **Role-Based Access Control**: USER, VILLAGER, and ADMIN roles

> **🔔 Important**: When a user's lease expires, the system **automatically downgrades their role from VILLAGER to USER**, allowing them to create new booking requests. See [AUTOMATION_GUIDE.md](AUTOMATION_GUIDE.md) for details.

## Prerequisites

Once the services are running, you can access them at the following URLs:
- **Frontend**: [http://localhost:5173](http://localhost:5173)
- **Backend API**: [http://localhost:8080](http://localhost:8080)
- **Database**: `localhost:3307` (MySQL 8.0)

## Getting Started

Follow these steps to get your development environment set up and running.

### 1. Clone the Repository

```bash
git clone https://github.com/f-midterm/project-ffinal.git
cd project-ffinal
```

### 2. Start the Services (Development)
```bash
docker compose -f docker-compose.dev.yml up --build -d
```

### 3. Stop the Services
```bash
docker compose -f docker-compose.dev.yml down
```

