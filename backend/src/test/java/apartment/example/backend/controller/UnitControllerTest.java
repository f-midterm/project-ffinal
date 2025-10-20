package apartment.example.backend.controller;

import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.service.UnitService;
import apartment.example.backend.service.TenantService;
import apartment.example.backend.service.LeaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UnitControllerTest {

    @Mock
    private UnitService unitService;

    @Mock
    private TenantService tenantService;

    @Mock
    private LeaseService leaseService;

    @InjectMocks
    private UnitController unitController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Unit testUnit;
    private Tenant testTenant;
    private Lease testLease;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(unitController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Support for Java 8 date/time

        // Setup test data
        testUnit = new Unit();
        testUnit.setId(1L);
        testUnit.setRoomNumber("101");
        testUnit.setFloor(1);
        testUnit.setType("1 Bedroom");
        testUnit.setStatus(UnitStatus.AVAILABLE);
        // Remove setMonthlyRent and setSize if Unit entity doesn't have these fields

        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setFirstName("John");
        testTenant.setLastName("Doe");
        testTenant.setEmail("john.doe@example.com");
        testTenant.setPhone("0812345678");

        testLease = new Lease();
        testLease.setId(1L);
        testLease.setUnit(testUnit);
        testLease.setTenant(testTenant);
        testLease.setStatus(LeaseStatus.ACTIVE);
        testLease.setStartDate(LocalDate.now());
        testLease.setEndDate(LocalDate.now().plusYears(1));
    }

    @Test
    void getAllUnits_ShouldReturnListOfUnits() throws Exception {
        // Arrange
        List<Unit> units = Arrays.asList(testUnit);
        when(unitService.getAllUnits()).thenReturn(units);

        // Act
        mockMvc.perform(get("/api/units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].roomNumber").value("101"));

        // Assert
        verify(unitService, times(1)).getAllUnits();
    }

//    @Test
//    void getAllUnitsPaged_ShouldReturnPageOfUnits() throws Exception {
//        // Arrange
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Unit> page = new PageImpl<>(Arrays.asList(testUnit), pageable, 1);
//        when(unitService.getAllUnits(any(Pageable.class))).thenReturn(page);
//
//        // Act
//        mockMvc.perform(get("/api/units/paged")
//                        .param("page", "0")
//                        .param("size", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content[0].roomNumber").value("101"));
//
//        // Assert
//        verify(unitService, times(1)).getAllUnits(any(Pageable.class));
//    }

    @Test
    void getUnitById_WhenUnitExists_ShouldReturnUnit() throws Exception {
        // Arrange
        when(unitService.getUnitById(1L)).thenReturn(Optional.of(testUnit));

        // Act
        mockMvc.perform(get("/api/units/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"));

        // Assert
        verify(unitService, times(1)).getUnitById(1L);
    }

    @Test
    void getUnitById_WhenUnitNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(unitService.getUnitById(999L)).thenReturn(Optional.empty());

        // Act
        mockMvc.perform(get("/api/units/999"))
                .andExpect(status().isNotFound());

        // Assert
        verify(unitService, times(1)).getUnitById(999L);
    }

    @Test
    void getUnitByRoomNumber_WhenExists_ShouldReturnUnit() throws Exception {
        // Arrange
        when(unitService.getUnitByRoomNumber("101")).thenReturn(Optional.of(testUnit));

        // Act
        mockMvc.perform(get("/api/units/room/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"));

        // Assert
        verify(unitService, times(1)).getUnitByRoomNumber("101");
    }

    @Test
    void getUnitsByFloor_ShouldReturnUnitsOnFloor() throws Exception {
        // Arrange
        List<Unit> units = Arrays.asList(testUnit);
        when(unitService.getUnitsByFloor(1)).thenReturn(units);

        // Act
        mockMvc.perform(get("/api/units/floor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].floor").value(1));

        // Assert
        verify(unitService, times(1)).getUnitsByFloor(1);
    }

    @Test
    void getUnitsByStatus_ShouldReturnUnitsWithStatus() throws Exception {
        // Arrange
        List<Unit> units = Arrays.asList(testUnit);
        when(unitService.getUnitsByStatus(UnitStatus.AVAILABLE)).thenReturn(units);

        // Act
        mockMvc.perform(get("/api/units/status/AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));

        // Assert
        verify(unitService, times(1)).getUnitsByStatus(UnitStatus.AVAILABLE);
    }

    @Test
    void getUnitsByType_ShouldReturnUnitsOfType() throws Exception {
        // Arrange
        List<Unit> units = Arrays.asList(testUnit);
        when(unitService.getUnitsByType("1 Bedroom")).thenReturn(units);

        // Act
        mockMvc.perform(get("/api/units/type/1 Bedroom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("1 Bedroom"));

        // Assert
        verify(unitService, times(1)).getUnitsByType("1 Bedroom");
    }

    @Test
    void getAvailableUnits_ShouldReturnOnlyAvailableUnits() throws Exception {
        // Arrange
        List<Unit> units = Arrays.asList(testUnit);
        when(unitService.getAvailableUnits()).thenReturn(units);

        // Act
        mockMvc.perform(get("/api/units/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));

        // Assert
        verify(unitService, times(1)).getAvailableUnits();
    }

//    @Test
//    void getUnitsByRentRange_ShouldReturnUnitsInRange() throws Exception {
//        // Arrange
//        List<Unit> units = Arrays.asList(testUnit);
//        when(unitService.getUnitsByRentRange(any(BigDecimal.class), any(BigDecimal.class)))
//                .thenReturn(units);
//
//        // Act
//        mockMvc.perform(get("/api/units/rent-range")
//                        .param("minRent", "10000")
//                        .param("maxRent", "20000"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].monthlyRent").value(15000));
//
//        // Assert
//        verify(unitService, times(1)).getUnitsByRentRange(any(BigDecimal.class), any(BigDecimal.class));
//    }

    @Test
    void getDashboard_ShouldReturnDashboardData() throws Exception {
        // Arrange
        when(unitService.getAllUnits()).thenReturn(Arrays.asList(testUnit));
        when(unitService.countUnitsByStatus(UnitStatus.AVAILABLE)).thenReturn(5L);
        when(unitService.countUnitsByStatus(UnitStatus.OCCUPIED)).thenReturn(3L);
        when(unitService.countUnitsByStatus(UnitStatus.MAINTENANCE)).thenReturn(1L);
        when(unitService.countUnitsByStatus(UnitStatus.RESERVED)).thenReturn(1L);
        when(unitService.countUnitsByFloor(1)).thenReturn(5L);
        when(unitService.countUnitsByFloor(2)).thenReturn(5L);

        // Act
        mockMvc.perform(get("/api/units/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUnits").value(1))
                .andExpect(jsonPath("$.availableUnits").value(5))
                .andExpect(jsonPath("$.occupiedUnits").value(3));

        // Assert
        verify(unitService, times(1)).getAllUnits();
    }

    @Test
    void createUnit_WithValidData_ShouldReturnCreatedUnit() throws Exception {
        // Arrange
        when(unitService.createUnit(any(Unit.class))).thenReturn(testUnit);

        // Act
        mockMvc.perform(post("/api/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUnit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value("101"));

        // Assert
        verify(unitService, times(1)).createUnit(any(Unit.class));
    }

    @Test
    void createUnit_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(unitService.createUnit(any(Unit.class)))
                .thenThrow(new IllegalArgumentException("Invalid unit data"));

        // Act
        mockMvc.perform(post("/api/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUnit)))
                .andExpect(status().isBadRequest());

        // Assert
        verify(unitService, times(1)).createUnit(any(Unit.class));
    }

    @Test
    void updateUnit_WhenExists_ShouldReturnUpdatedUnit() throws Exception {
        // Arrange
        when(unitService.updateUnit(eq(1L), any(Unit.class))).thenReturn(testUnit);

        // Act
        mockMvc.perform(put("/api/units/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUnit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"));

        // Assert
        verify(unitService, times(1)).updateUnit(eq(1L), any(Unit.class));
    }

    @Test
    void updateUnit_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(unitService.updateUnit(eq(999L), any(Unit.class)))
                .thenThrow(new RuntimeException("Unit not found"));

        // Act
        mockMvc.perform(put("/api/units/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUnit)))
                .andExpect(status().isNotFound());

        // Assert
        verify(unitService, times(1)).updateUnit(eq(999L), any(Unit.class));
    }

    @Test
    void updateUnitStatus_WithValidStatus_ShouldReturnUpdatedUnit() throws Exception {
        // Arrange
        testUnit.setStatus(UnitStatus.OCCUPIED);
        doNothing().when(unitService).updateUnitStatus(1L, UnitStatus.OCCUPIED);
        when(unitService.getUnitById(1L)).thenReturn(Optional.of(testUnit));

        // Act
        mockMvc.perform(patch("/api/units/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OCCUPIED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OCCUPIED"));

        // Assert
        verify(unitService, times(1)).updateUnitStatus(1L, UnitStatus.OCCUPIED);
    }

    @Test
    void updateUnitStatus_WithInvalidStatus_ShouldReturnBadRequest() throws Exception {
        // Act
        mockMvc.perform(patch("/api/units/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVALID_STATUS\"}"))
                .andExpect(status().isBadRequest());

        // Assert
        verify(unitService, never()).updateUnitStatus(anyLong(), any(UnitStatus.class));
    }

    @Test
    void deleteUnit_WhenExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(unitService).deleteUnit(1L);

        // Act
        mockMvc.perform(delete("/api/units/1"))
                .andExpect(status().isNoContent());

        // Assert
        verify(unitService, times(1)).deleteUnit(1L);
    }

    @Test
    void deleteUnit_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Unit not found")).when(unitService).deleteUnit(999L);

        // Act
        mockMvc.perform(delete("/api/units/999"))
                .andExpect(status().isNotFound());

        // Assert
        verify(unitService, times(1)).deleteUnit(999L);
    }

    @Test
    void checkUnitExists_WhenExists_ShouldReturnTrue() throws Exception {
        // Arrange
        when(unitService.existsById(1L)).thenReturn(true);

        // Act
        mockMvc.perform(get("/api/units/1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Assert
        verify(unitService, times(1)).existsById(1L);
    }

    @Test
    void checkRoomNumberExists_WhenExists_ShouldReturnTrue() throws Exception {
        // Arrange
        when(unitService.existsByRoomNumber("101")).thenReturn(true);

        // Act
        mockMvc.perform(get("/api/units/room/101/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Assert
        verify(unitService, times(1)).existsByRoomNumber("101");
    }

    @Test
    void getUnitsWithDetails_ShouldReturnDetailedUnits() throws Exception {
        // Arrange
        testUnit.setStatus(UnitStatus.OCCUPIED);
        List<Unit> units = Arrays.asList(testUnit);
        List<Lease> leases = Arrays.asList(testLease);

        when(unitService.getAllUnits()).thenReturn(units);
        when(leaseService.getAllLeases()).thenReturn(leases);

        // Act
        mockMvc.perform(get("/api/units/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unit.roomNumber").value("101"))
                .andExpect(jsonPath("$[0].lease").exists())
                .andExpect(jsonPath("$[0].tenant.firstName").value("John"));

        // Assert
        verify(unitService, times(1)).getAllUnits();
        verify(leaseService, times(1)).getAllLeases();
    }

    @Test
    void getUnitDetails_WhenExists_ShouldReturnDetails() throws Exception {
        // Arrange
        when(unitService.getUnitById(1L)).thenReturn(Optional.of(testUnit));
        when(leaseService.getActiveLeaseByUnit(1L)).thenReturn(Optional.of(testLease));

        // Act
        mockMvc.perform(get("/api/units/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unit.roomNumber").value("101"))
                .andExpect(jsonPath("$.lease").exists())
                .andExpect(jsonPath("$.tenant.firstName").value("John"));

        // Assert
        verify(unitService, times(1)).getUnitById(1L);
        verify(leaseService, times(1)).getActiveLeaseByUnit(1L);
    }

    @Test
    void getUnitDetails_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(unitService.getUnitById(999L)).thenReturn(Optional.empty());

        // Act
        mockMvc.perform(get("/api/units/999/details"))
                .andExpect(status().isNotFound());

        // Assert
        verify(unitService, times(1)).getUnitById(999L);
    }

    @Test
    void maintenanceTest_ShouldReturnTestMessage() throws Exception {
        // Act
        mockMvc.perform(get("/api/units/maintenance-test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Maintenance functionality test - working!"));
    }

    // Direct controller method tests (without MockMvc)
    @Test
    void getAllUnits_DirectCall_ShouldReturnListOfUnits() {
        // Arrange
        List<Unit> units = Arrays.asList(testUnit);
        when(unitService.getAllUnits()).thenReturn(units);

        // Act
        ResponseEntity<List<Unit>> response = unitController.getAllUnits();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("101", response.getBody().get(0).getRoomNumber());
    }

    @Test
    void getUnitById_DirectCall_WhenExists_ShouldReturnUnit() {
        // Arrange
        when(unitService.getUnitById(1L)).thenReturn(Optional.of(testUnit));

        // Act
        ResponseEntity<Unit> response = unitController.getUnitById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("101", response.getBody().getRoomNumber());
    }

    @Test
    void createUnit_DirectCall_ShouldReturnCreatedUnit() {
        // Arrange
        when(unitService.createUnit(any(Unit.class))).thenReturn(testUnit);

        // Act
        ResponseEntity<Unit> response = unitController.createUnit(testUnit);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("101", response.getBody().getRoomNumber());
    }
}