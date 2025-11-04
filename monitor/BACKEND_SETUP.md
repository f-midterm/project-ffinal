# Backend Configuration for Prometheus Monitoring

## สิ่งที่ต้องทำเพื่อเปิดใช้งาน Monitoring

### 1. อัปเดต Backend Service Labels

ไฟล์ `k8s/backend/service.yaml` ได้ถูกอัปเดตแล้วโดยเพิ่ม label `monitoring: enabled` ซึ่งจำเป็นสำหรับ ServiceMonitor ในการค้นหา service นี้

```yaml
metadata:
  name: backend-service
  namespace: superproject-ns
  labels:
    app: apartment-system
    component: backend
    monitoring: enabled  # ← เพิ่ม label นี้
```

หลังจากแก้ไขแล้ว ให้ apply การเปลี่ยนแปลง:

```bash
kubectl apply -f k8s/backend/service.yaml
```

### 2. ตรวจสอบ Spring Boot Actuator Configuration

ตรวจสอบว่า Backend ของคุณมีการตั้งค่าดังนี้ใน `application.properties` หรือ `application.yml`:

**application.properties:**
```properties
# Enable Actuator endpoints
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.prometheus.enabled=true
management.metrics.export.prometheus.enabled=true

# Optional: Customize the base path (default is /actuator)
management.endpoints.web.base-path=/actuator
```

**application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
      base-path: /actuator
  endpoint:
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

### 3. เพิ่ม Dependencies (ถ้ายังไม่มี)

**build.gradle:**
```gradle
dependencies {
    // Spring Boot Actuator
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    
    // Micrometer Prometheus Registry
    implementation 'io.micrometer:micrometer-registry-prometheus'
}
```

**pom.xml (สำหรับ Maven):**
```xml
<dependencies>
    <!-- Spring Boot Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Micrometer Prometheus Registry -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

### 4. ทดสอบ Metrics Endpoint

หลังจาก deploy backend แล้ว ให้ทดสอบว่า metrics endpoint ทำงาน:

```bash
# Port forward to backend pod
kubectl port-forward -n superproject-ns deployment/backend-deployment 8080:8080

# Test the Prometheus endpoint
curl http://localhost:8080/actuator/prometheus
```

คุณควรเห็น output เป็น metrics format ของ Prometheus:

```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space",} 1.234567E8
...
```

### 5. ตรวจสอบ ServiceMonitor

ตรวจสอบว่า ServiceMonitor ถูกสร้างขึ้นแล้ว:

```bash
kubectl get servicemonitor -n superproject-ns
```

ดูรายละเอียด:

```bash
kubectl describe servicemonitor backend-servicemonitor -n superproject-ns
```

### 6. ตรวจสอบใน Prometheus Targets

เข้าถึง Prometheus UI:

```bash
kubectl port-forward -n superproject-ns svc/monitoring-kube-prometheus-prometheus 9090:9090
```

เปิดเบราว์เซอร์ที่ http://localhost:9090 และไปที่:
- Status → Targets
- ค้นหา `backend-service`
- ตรวจสอบว่า State เป็น "UP" (สีเขียว)

### 7. Custom Metrics (Optional)

คุณสามารถเพิ่ม custom metrics ใน Spring Boot:

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    private final Counter bookingCounter;
    
    public BookingService(MeterRegistry registry) {
        this.bookingCounter = Counter.builder("bookings_total")
            .description("Total number of bookings")
            .tag("type", "apartment")
            .register(registry);
    }
    
    public void createBooking() {
        // Your booking logic
        bookingCounter.increment();
    }
}
```

## ตัวอย่าง Metrics ที่มีให้อัตโนมัติ

Spring Boot Actuator จะสร้าง metrics เหล่านี้โดยอัตโนมัติ:

- **HTTP Metrics**: `http_server_requests_seconds_*`
- **JVM Memory**: `jvm_memory_used_bytes`, `jvm_memory_max_bytes`
- **JVM Threads**: `jvm_threads_live_threads`, `jvm_threads_daemon_threads`
- **CPU**: `process_cpu_usage`, `system_cpu_usage`
- **Garbage Collection**: `jvm_gc_*`
- **Database Connection Pool**: `hikaricp_*` (ถ้าใช้ HikariCP)

## Troubleshooting

### ปัญหา: Prometheus ไม่เห็น Backend Target

**แก้ไข:**
1. ตรวจสอบว่า service มี label `monitoring: enabled`:
   ```bash
   kubectl get svc backend-service -n superproject-ns -o yaml | grep monitoring
   ```

2. ตรวจสอบว่า ServiceMonitor ถูกสร้าง:
   ```bash
   kubectl get servicemonitor -n superproject-ns
   ```

3. ตรวจสอบ logs ของ Prometheus Operator:
   ```bash
   kubectl logs -n superproject-ns -l app.kubernetes.io/name=prometheus-operator
   ```

### ปัญหา: Target State เป็น "DOWN"

**แก้ไข:**
1. ตรวจสอบว่า pod ทำงานอยู่:
   ```bash
   kubectl get pods -n superproject-ns -l app=apartment-system
   ```

2. ตรวจสอบว่า `/actuator/prometheus` endpoint ทำงาน:
   ```bash
   kubectl exec -n superproject-ns deployment/backend-deployment -- curl localhost:8080/actuator/prometheus
   ```

3. ตรวจสอบ service port name ต้องเป็น `http`:
   ```bash
   kubectl get svc backend-service -n superproject-ns -o yaml | grep -A5 ports
   ```

### ปัญหา: No data in Grafana

**แก้ไข:**
1. ตรวจสอบว่า Prometheus มี data:
   - เข้า Prometheus UI
   - รัน query: `up{job="backend-service"}`
   - ควรเห็นค่า 1

2. ตรวจสอบว่า Grafana data source ถูกต้อง:
   - Configuration → Data Sources → Prometheus
   - ทดสอบด้วยปุ่ม "Save & Test"

3. ตรวจสอบ query ใน dashboard ว่าถูกต้อง

## Best Practices

1. **ใช้ Tags ใน Metrics**: เพิ่ม tags เพื่อแยกประเภทของ requests, errors, etc.

2. **Monitor Business Metrics**: นอกจาก technical metrics แล้ว ควร monitor business metrics เช่น:
   - จำนวน bookings ต่อชั่วโมง
   - Average booking duration
   - Payment success rate

3. **Set Up Alerts**: สร้าง alerts สำหรับ critical metrics:
   - High error rate
   - High response time
   - Low memory
   - High CPU usage

4. **Regular Dashboard Review**: ตรวจสอบ dashboard เป็นประจำเพื่อหา patterns และ bottlenecks

## ตัวอย่าง Useful Prometheus Queries

```promql
# Request rate
rate(http_server_requests_seconds_count{job="backend-service"}[5m])

# Average response time
rate(http_server_requests_seconds_sum{job="backend-service"}[5m]) 
/ 
rate(http_server_requests_seconds_count{job="backend-service"}[5m])

# Error rate (4xx and 5xx)
rate(http_server_requests_seconds_count{job="backend-service", status=~"[45].."}[5m])

# Memory usage percentage
(jvm_memory_used_bytes{job="backend-service", area="heap"} 
/ 
jvm_memory_max_bytes{job="backend-service", area="heap"}) * 100

# 95th percentile response time
histogram_quantile(0.95, 
  rate(http_server_requests_seconds_bucket{job="backend-service"}[5m])
)
```

## Additional Resources

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Query Language](https://prometheus.io/docs/prometheus/latest/querying/basics/)
- [Grafana Dashboard Best Practices](https://grafana.com/docs/grafana/latest/dashboards/build-dashboards/best-practices/)
