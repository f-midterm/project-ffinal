package apartment.example.backend.dto;

import apartment.example.backend.entity.MaintenanceRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRequestDTO {
    private Long id;
    private Long tenantId;
    private Long unitId;
    private String title;
    private String description;
    private String priority;
    private String category;
    private String urgency;
    private String preferredTime;
    private String status;
    private Long assignedToUserId;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String completionNotes;
    private LocalDateTime submittedDate;
    private LocalDateTime completedDate;
    
    // Additional fields
    private String roomNumber;
    private String tenantName;
    private String unitType;
    
    public static MaintenanceRequestDTO fromEntity(MaintenanceRequest request, String roomNumber, String tenantName, String unitType) {
        MaintenanceRequestDTO dto = new MaintenanceRequestDTO();
        dto.setId(request.getId());
        dto.setTenantId(request.getTenantId());
        dto.setUnitId(request.getUnitId());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setPriority(request.getPriority() != null ? request.getPriority().name() : null);
        dto.setCategory(request.getCategory() != null ? request.getCategory().name() : null);
        dto.setUrgency(request.getUrgency() != null ? request.getUrgency().name() : null);
        dto.setPreferredTime(request.getPreferredTime());
        dto.setStatus(request.getStatus() != null ? request.getStatus().name() : null);
        dto.setAssignedToUserId(request.getAssignedToUserId());
    dto.setEstimatedCost(request.getEstimatedCost());
    dto.setActualCost(request.getActualCost());
        dto.setCompletionNotes(request.getCompletionNotes());
        dto.setSubmittedDate(request.getSubmittedDate());
        dto.setCompletedDate(request.getCompletedDate());
        dto.setRoomNumber(roomNumber);
        dto.setTenantName(tenantName);
        dto.setUnitType(unitType);
        return dto;
    }
}
