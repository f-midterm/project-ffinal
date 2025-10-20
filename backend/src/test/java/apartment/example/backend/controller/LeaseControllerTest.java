package apartment.example.backend.controller;

import apartment.example.backend.entity.Lease;
import apartment.example.backend.entity.Tenant;
import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.enums.BillingCycle;
import apartment.example.backend.entity.enums.LeaseStatus;
import apartment.example.backend.service.LeaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LeaseControllerTest {

    @Mock
    private LeaseService leaseService;

    @InjectMocks
    private LeaseController leaseController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Lease testLease;
    private Tenant testTenant;
    private Unit testUnit;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(leaseController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // สร้าง test data
        testTenant = new Tenant();
        testTenant.setId(1L);

        testUnit = new Unit();
        testUnit.setId(1L);

        testLease = new Lease();
        testLease.setId(1L);
        testLease.setTenant(testTenant);
        testLease.setUnit(testUnit);
        testLease.setStartDate(LocalDate.now());
        testLease.setEndDate(LocalDate.now().plusYears(1));
        testLease.setRentAmount(new BigDecimal("10000.00"));
        testLease.setSecurityDeposit(new BigDecimal("20000.00"));
        testLease.setBillingCycle(BillingCycle.MONTHLY);
        testLease.setStatus(LeaseStatus.ACTIVE);
    }

    @Test
    void getAllLeases_ShouldReturnListOfLeases() throws Exception {
        List<Lease> leases = Arrays.asList(testLease);
        when(leaseService.getAllLeases()).thenReturn(leases);

        mockMvc.perform(get("/api/leases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(leaseService, times(1)).getAllLeases();
    }

//    @Test
//    void getAllLeasesPaged_ShouldReturnPageOfLeases() throws Exception {
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Lease> leasePage = new PageImpl<>(Arrays.asList(testLease), pageable, 1);
//        when(leaseService.getAllLeases(any(Pageable.class))).thenReturn(leasePage);
//
//        mockMvc.perform(get("/api/leases/paged")
//                        .param("page", "0")
//                        .param("size", "10"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content", hasSize(1)))
//                .andExpect(jsonPath("$.totalElements", is(1)));
//
//        verify(leaseService, times(1)).getAllLeases(any(Pageable.class));
//    }

    @Test
    void getLeaseById_WhenExists_ShouldReturnLease() throws Exception {
        when(leaseService.getLeaseById(1L)).thenReturn(Optional.of(testLease));

        mockMvc.perform(get("/api/leases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(leaseService, times(1)).getLeaseById(1L);
    }

    @Test
    void getLeaseById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(leaseService.getLeaseById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/leases/1"))
                .andExpect(status().isNotFound());

        verify(leaseService, times(1)).getLeaseById(1L);
    }

    @Test
    void getLeasesByStatus_ShouldReturnLeasesList() throws Exception {
        List<Lease> leases = Arrays.asList(testLease);
        when(leaseService.getLeasesByStatus(LeaseStatus.ACTIVE)).thenReturn(leases);

        mockMvc.perform(get("/api/leases/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));

        verify(leaseService, times(1)).getLeasesByStatus(LeaseStatus.ACTIVE);
    }

    @Test
    void getLeasesByTenant_ShouldReturnLeasesList() throws Exception {
        List<Lease> leases = Arrays.asList(testLease);
        when(leaseService.getLeasesByTenant(1L)).thenReturn(leases);

        mockMvc.perform(get("/api/leases/tenant/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(leaseService, times(1)).getLeasesByTenant(1L);
    }

    @Test
    void getLeasesByUnit_ShouldReturnLeasesList() throws Exception {
        List<Lease> leases = Arrays.asList(testLease);
        when(leaseService.getLeasesByUnit(1L)).thenReturn(leases);

        mockMvc.perform(get("/api/leases/unit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(leaseService, times(1)).getLeasesByUnit(1L);
    }

    @Test
    void getActiveLeaseByUnit_WhenExists_ShouldReturnLease() throws Exception {
        when(leaseService.getActiveLeaseByUnit(1L)).thenReturn(Optional.of(testLease));

        mockMvc.perform(get("/api/leases/unit/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(leaseService, times(1)).getActiveLeaseByUnit(1L);
    }

    @Test
    void getActiveLeaseByUnit_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(leaseService.getActiveLeaseByUnit(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/leases/unit/1/active"))
                .andExpect(status().isNotFound());

        verify(leaseService, times(1)).getActiveLeaseByUnit(1L);
    }

    @Test
    void getLeasesEndingSoon_ShouldReturnLeasesList() throws Exception {
        List<Lease> leases = Arrays.asList(testLease);
        when(leaseService.getLeasesEndingSoon(30)).thenReturn(leases);

        mockMvc.perform(get("/api/leases/ending-soon")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(leaseService, times(1)).getLeasesEndingSoon(30);
    }

    @Test
    void getLeasesEndingSoon_WithDefaultDays_ShouldReturnLeasesList() throws Exception {
        List<Lease> leases = Arrays.asList(testLease);
        when(leaseService.getLeasesEndingSoon(30)).thenReturn(leases);

        mockMvc.perform(get("/api/leases/ending-soon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(leaseService, times(1)).getLeasesEndingSoon(30);
    }

    @Test
    void getExpiredLeases_ShouldReturnLeasesList() throws Exception {
        List<Lease> leases = Arrays.asList(testLease);
        when(leaseService.getExpiredLeases()).thenReturn(leases);

        mockMvc.perform(get("/api/leases/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(leaseService, times(1)).getExpiredLeases();
    }

    @Test
    void createLease_WhenValid_ShouldReturnCreatedLease() throws Exception {
        when(leaseService.createLease(any(Lease.class))).thenReturn(testLease);

        mockMvc.perform(post("/api/leases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLease)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(leaseService, times(1)).createLease(any(Lease.class));
    }

    @Test
    void createLease_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        when(leaseService.createLease(any(Lease.class)))
                .thenThrow(new IllegalArgumentException("Invalid lease data"));

        mockMvc.perform(post("/api/leases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLease)))
                .andExpect(status().isBadRequest());

        verify(leaseService, times(1)).createLease(any(Lease.class));
    }

    @Test
    void updateLease_WhenValid_ShouldReturnUpdatedLease() throws Exception {
        when(leaseService.updateLease(eq(1L), any(Lease.class))).thenReturn(testLease);

        mockMvc.perform(put("/api/leases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLease)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));

        verify(leaseService, times(1)).updateLease(eq(1L), any(Lease.class));
    }

    @Test
    void updateLease_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        when(leaseService.updateLease(eq(1L), any(Lease.class)))
                .thenThrow(new IllegalArgumentException("Invalid lease data"));

        mockMvc.perform(put("/api/leases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLease)))
                .andExpect(status().isBadRequest());

        verify(leaseService, times(1)).updateLease(eq(1L), any(Lease.class));
    }

    @Test
    void updateLease_WhenNotFound_ShouldReturnNotFound() throws Exception {
        when(leaseService.updateLease(eq(1L), any(Lease.class)))
                .thenThrow(new RuntimeException("Lease not found"));

        mockMvc.perform(put("/api/leases/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLease)))
                .andExpect(status().isNotFound());

        verify(leaseService, times(1)).updateLease(eq(1L), any(Lease.class));
    }

    @Test
    void activateLease_WhenValid_ShouldReturnActivatedLease() throws Exception {
        testLease.setStatus(LeaseStatus.ACTIVE);
        when(leaseService.activateLease(1L)).thenReturn(testLease);

        mockMvc.perform(patch("/api/leases/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));

        verify(leaseService, times(1)).activateLease(1L);
    }

    @Test
    void activateLease_WhenInvalid_ShouldReturnBadRequest() throws Exception {
        when(leaseService.activateLease(1L))
                .thenThrow(new IllegalArgumentException("Cannot activate lease"));

        mockMvc.perform(patch("/api/leases/1/activate"))
                .andExpect(status().isBadRequest());

        verify(leaseService, times(1)).activateLease(1L);
    }

    @Test
    void activateLease_WhenNotFound_ShouldReturnNotFound() throws Exception {
        when(leaseService.activateLease(1L))
                .thenThrow(new RuntimeException("Lease not found"));

        mockMvc.perform(patch("/api/leases/1/activate"))
                .andExpect(status().isNotFound());

        verify(leaseService, times(1)).activateLease(1L);
    }

    @Test
    void terminateLease_WhenValid_ShouldReturnTerminatedLease() throws Exception {
        testLease.setStatus(LeaseStatus.TERMINATED);
        when(leaseService.terminateLease(eq(1L), anyString())).thenReturn(testLease);

        Map<String, String> request = new HashMap<>();
        request.put("reason", "Tenant request");

        mockMvc.perform(patch("/api/leases/1/terminate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("TERMINATED")));

        verify(leaseService, times(1)).terminateLease(1L, "Tenant request");
    }

    @Test
    void terminateLease_WithoutReason_ShouldUseDefaultReason() throws Exception {
        testLease.setStatus(LeaseStatus.TERMINATED);
        when(leaseService.terminateLease(eq(1L), anyString())).thenReturn(testLease);

        mockMvc.perform(patch("/api/leases/1/terminate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(leaseService, times(1)).terminateLease(1L, "No reason provided");
    }

    @Test
    void terminateLease_WhenNotFound_ShouldReturnNotFound() throws Exception {
        when(leaseService.terminateLease(eq(1L), anyString()))
                .thenThrow(new RuntimeException("Lease not found"));

        Map<String, String> request = new HashMap<>();
        request.put("reason", "Tenant request");

        mockMvc.perform(patch("/api/leases/1/terminate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(leaseService, times(1)).terminateLease(eq(1L), anyString());
    }

    @Test
    void expireLeases_WhenSuccessful_ShouldReturnOk() throws Exception {
        doNothing().when(leaseService).expireLeases();

        mockMvc.perform(post("/api/leases/expire-leases"))
                .andExpect(status().isOk())
                .andExpect(content().string("Expired leases processed successfully"));

        verify(leaseService, times(1)).expireLeases();
    }

    @Test
    void expireLeases_WhenException_ShouldReturnInternalServerError() throws Exception {
        doThrow(new RuntimeException("Database error")).when(leaseService).expireLeases();

        mockMvc.perform(post("/api/leases/expire-leases"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error processing expired leases"));

        verify(leaseService, times(1)).expireLeases();
    }

    @Test
    void deleteLease_WhenSuccessful_ShouldReturnNoContent() throws Exception {
        doNothing().when(leaseService).deleteLease(1L);

        mockMvc.perform(delete("/api/leases/1"))
                .andExpect(status().isNoContent());

        verify(leaseService, times(1)).deleteLease(1L);
    }

    @Test
    void deleteLease_WhenNotFound_ShouldReturnNotFound() throws Exception {
        doThrow(new RuntimeException("Lease not found")).when(leaseService).deleteLease(1L);

        mockMvc.perform(delete("/api/leases/1"))
                .andExpect(status().isNotFound());

        verify(leaseService, times(1)).deleteLease(1L);
    }

    @Test
    void checkLeaseExists_WhenExists_ShouldReturnTrue() throws Exception {
        when(leaseService.existsById(1L)).thenReturn(true);

        mockMvc.perform(get("/api/leases/1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(leaseService, times(1)).existsById(1L);
    }

    @Test
    void checkLeaseExists_WhenNotExists_ShouldReturnFalse() throws Exception {
        when(leaseService.existsById(1L)).thenReturn(false);

        mockMvc.perform(get("/api/leases/1/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(leaseService, times(1)).existsById(1L);
    }
}