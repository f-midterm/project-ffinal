# Project Title

A brief description of your project and what it does.

## Prerequisites

Once the services are running, you can access them at the following URLs:
- [Main Application :](http://localhost)

## Getting Started

Follow these steps to get your development environment set up and running.

### 1. Clone the Repository

```bash
git clone -b Phase2 https://github.com/f-midterm/Setup.git
cd Setup
```

### 2. Start the Services
```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d
```

### 3. Stop the Services
```bash
docker-compose down
```

