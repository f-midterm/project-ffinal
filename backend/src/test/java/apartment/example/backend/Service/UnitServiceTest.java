package apartment.example.backend.Service;

import apartment.example.backend.entity.Unit;
import apartment.example.backend.entity.enums.UnitStatus;
import apartment.example.backend.repository.UnitRepository;
import apartment.example.backend.service.UnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private UnitService unitService;

    private Unit unit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        unit = new Unit();
        unit.setId(1L);
        unit.setRoomNumber("A101");
        unit.setFloor(1);
        unit.setStatus(UnitStatus.AVAILABLE);
        unit.setType("Studio");
        unit.setRentAmount(new BigDecimal("8000"));
        unit.setSizeSqm(new BigDecimal("30"));
        unit.setDescription("Nice view room");
    }

    @Test
    void getAllUnits_ShouldReturnList() {
        when(unitRepository.findAll()).thenReturn(List.of(unit));

        List<Unit> result = unitService.getAllUnits();

        assertEquals(1, result.size());
        verify(unitRepository).findAll();
    }

    @Test
    void getAllUnits_WithPageable_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 5);
        when(unitRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(unit)));

        Page<Unit> result = unitService.getAllUnits(pageable);

        assertEquals(1, result.getContent().size());
        verify(unitRepository).findAll(pageable);
    }

    @Test
    void getUnitById_ShouldReturnUnit_WhenFound() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));

        Optional<Unit> result = unitService.getUnitById(1L);

        assertTrue(result.isPresent());
        assertEquals("A101", result.get().getRoomNumber());
    }

    @Test
    void getUnitById_ShouldReturnEmpty_WhenNotFound() {
        when(unitRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Unit> result = unitService.getUnitById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getUnitByRoomNumber_ShouldReturnUnit_WhenExists() {
        when(unitRepository.findByRoomNumber("A101")).thenReturn(Optional.of(unit));

        Optional<Unit> result = unitService.getUnitByRoomNumber("A101");

        assertTrue(result.isPresent());
        assertEquals("A101", result.get().getRoomNumber());
    }

    @Test
    void getUnitsByFloor_ShouldReturnList() {
        when(unitRepository.findByFloor(1)).thenReturn(List.of(unit));

        List<Unit> result = unitService.getUnitsByFloor(1);

        assertEquals(1, result.size());
        verify(unitRepository).findByFloor(1);
    }

    @Test
    void getUnitsByStatus_ShouldReturnList() {
        when(unitRepository.findByStatus(UnitStatus.AVAILABLE)).thenReturn(List.of(unit));

        List<Unit> result = unitService.getUnitsByStatus(UnitStatus.AVAILABLE);

        assertEquals(1, result.size());
        verify(unitRepository).findByStatus(UnitStatus.AVAILABLE);
    }

    @Test
    void getUnitsByType_ShouldReturnList() {
        when(unitRepository.findByType("Studio")).thenReturn(List.of(unit));

        List<Unit> result = unitService.getUnitsByType("Studio");

        assertEquals(1, result.size());
        verify(unitRepository).findByType("Studio");
    }

    @Test
    void getAvailableUnits_ShouldReturnList() {
        when(unitRepository.findByStatus(UnitStatus.AVAILABLE)).thenReturn(List.of(unit));

        List<Unit> result = unitService.getAvailableUnits();

        assertEquals(1, result.size());
    }

    @Test
    void getUnitsByRentRange_ShouldReturnList() {
        when(unitRepository.findByRentAmountBetween(new BigDecimal("5000"), new BigDecimal("10000")))
                .thenReturn(List.of(unit));

        List<Unit> result = unitService.getUnitsByRentRange(new BigDecimal("5000"), new BigDecimal("10000"));

        assertEquals(1, result.size());
        verify(unitRepository).findByRentAmountBetween(any(), any());
    }

    @Test
    void countUnitsByStatus_ShouldReturnCount() {
        when(unitRepository.countByStatus(UnitStatus.AVAILABLE)).thenReturn(5L);

        long result = unitService.countUnitsByStatus(UnitStatus.AVAILABLE);

        assertEquals(5L, result);
        verify(unitRepository).countByStatus(UnitStatus.AVAILABLE);
    }

    @Test
    void countUnitsByFloor_ShouldReturnCount() {
        when(unitRepository.countByFloor(2)).thenReturn(3L);

        long result = unitService.countUnitsByFloor(2);

        assertEquals(3L, result);
        verify(unitRepository).countByFloor(2);
    }

    @Test
    void createUnit_ShouldSave_WhenUniqueRoomNumber() {
        when(unitRepository.findByRoomNumber("A101")).thenReturn(Optional.empty());
        when(unitRepository.save(unit)).thenReturn(unit);

        Unit result = unitService.createUnit(unit);

        assertEquals("A101", result.getRoomNumber());
        verify(unitRepository).save(unit);
    }

    @Test
    void createUnit_ShouldThrow_WhenDuplicateRoomNumber() {
        when(unitRepository.findByRoomNumber("A101")).thenReturn(Optional.of(unit));

        assertThrows(IllegalArgumentException.class, () -> unitService.createUnit(unit));
    }

    @Test
    void updateUnit_ShouldUpdate_WhenValid() {
        Unit updateData = new Unit();
        updateData.setRoomNumber("A102");
        updateData.setFloor(2);
        updateData.setStatus(UnitStatus.OCCUPIED);
        updateData.setType("1-Bedroom");
        updateData.setRentAmount(new BigDecimal("9000"));
        updateData.setSizeSqm(new BigDecimal("35"));
        updateData.setDescription("Updated room");

        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(unitRepository.findByRoomNumber("A102")).thenReturn(Optional.empty());
        when(unitRepository.save(any(Unit.class))).thenAnswer(inv -> inv.getArgument(0));

        Unit result = unitService.updateUnit(1L, updateData);

        assertEquals("A102", result.getRoomNumber());
        assertEquals(UnitStatus.OCCUPIED, result.getStatus());
        verify(unitRepository).save(unit);
    }

    @Test
    void updateUnit_ShouldThrow_WhenNotFound() {
        when(unitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> unitService.updateUnit(99L, unit));
    }

    @Test
    void updateUnit_ShouldThrow_WhenDuplicateRoomNumber() {
        Unit existing = new Unit();
        existing.setRoomNumber("A102");

        Unit updateData = new Unit();
        updateData.setRoomNumber("A102");

        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(unitRepository.findByRoomNumber("A102")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> unitService.updateUnit(1L, updateData));
    }

    @Test
    void deleteUnit_ShouldDelete_WhenFound() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));

        unitService.deleteUnit(1L);

        verify(unitRepository).delete(unit);
    }

    @Test
    void deleteUnit_ShouldThrow_WhenNotFound() {
        when(unitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> unitService.deleteUnit(99L));
    }

    @Test
    void updateUnitStatus_ShouldUpdateStatus() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(unitRepository.save(unit)).thenReturn(unit);

        unitService.updateUnitStatus(1L, UnitStatus.MAINTENANCE);

        assertEquals(UnitStatus.MAINTENANCE, unit.getStatus());
        verify(unitRepository).save(unit);
    }

    @Test
    void updateUnitStatus_ShouldThrow_WhenNotFound() {
        when(unitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> unitService.updateUnitStatus(99L, UnitStatus.AVAILABLE));
    }

    @Test
    void existsByRoomNumber_ShouldReturnTrue_WhenExists() {
        when(unitRepository.findByRoomNumber("A101")).thenReturn(Optional.of(unit));

        boolean result = unitService.existsByRoomNumber("A101");

        assertTrue(result);
    }

    @Test
    void existsByRoomNumber_ShouldReturnFalse_WhenNotExists() {
        when(unitRepository.findByRoomNumber("A101")).thenReturn(Optional.empty());

        boolean result = unitService.existsByRoomNumber("A101");

        assertFalse(result);
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        when(unitRepository.existsById(1L)).thenReturn(true);

        boolean result = unitService.existsById(1L);

        assertTrue(result);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenNotExists() {
        when(unitRepository.existsById(99L)).thenReturn(false);

        boolean result = unitService.existsById(99L);

        assertFalse(result);
    }
}