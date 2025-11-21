package apartment.example.backend.service;

import apartment.example.backend.entity.ApartmentSettings;
import apartment.example.backend.repository.ApartmentSettingsRepository;
import apartment.example.backend.service.ApartmentSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ApartmentSettingsServiceTest {

    @Mock
    private ApartmentSettingsRepository repository;

    @InjectMocks
    private ApartmentSettingsService service;

    private ApartmentSettings setting;

    @BeforeEach
    void setup() {
        setting = new ApartmentSettings();
        setting.setId(1L);
        setting.setSettingKey("TEST_KEY");
        setting.setSettingValue("123.45");
        setting.setUpdatedByUserId(10L);
    }

    @Test
    void testGetAllSettings() {
        when(repository.findAll()).thenReturn(List.of(setting));

        List<ApartmentSettings> result = service.getAllSettings();

        assertEquals(1, result.size());
        assertEquals("123.45", result.get(0).getSettingValue());
    }

    @Test
    void testGetSettingByKey_found() {
        when(repository.findBySettingKey("TEST_KEY")).thenReturn(Optional.of(setting));

        Optional<ApartmentSettings> result = service.getSettingByKey("TEST_KEY");

        assertTrue(result.isPresent());
        assertEquals("123.45", result.get().getSettingValue());
    }

    @Test
    void testGetSettingByKey_notFound() {
        when(repository.findBySettingKey("NOT_EXIST")).thenReturn(Optional.empty());

        Optional<ApartmentSettings> result = service.getSettingByKey("NOT_EXIST");

        assertFalse(result.isPresent());
    }

    @Test
    void testGetSettingValueAsDecimal_validNumber() {
        when(repository.findBySettingKey("TEST_KEY")).thenReturn(Optional.of(setting));

        BigDecimal value = service.getSettingValueAsDecimal("TEST_KEY", BigDecimal.ZERO);

        assertEquals(new BigDecimal("123.45"), value);
    }

    @Test
    void testGetSettingValueAsDecimal_invalidNumber_returnsDefault() {
        setting.setSettingValue("ABC"); // invalid numeric format
        when(repository.findBySettingKey("TEST_KEY")).thenReturn(Optional.of(setting));

        BigDecimal result = service.getSettingValueAsDecimal("TEST_KEY", new BigDecimal("9.99"));

        assertEquals(new BigDecimal("9.99"), result);
    }

    @Test
    void testGetSettingValueAsDecimal_notFound_returnsDefault() {
        when(repository.findBySettingKey("NOT_EXIST")).thenReturn(Optional.empty());

        BigDecimal result = service.getSettingValueAsDecimal("NOT_EXIST", new BigDecimal("5.55"));

        assertEquals(new BigDecimal("5.55"), result);
    }

    @Test
    void testGetElectricityRate_defaultWhenNotFound() {
        when(repository.findBySettingKey("ELECTRICITY_RATE")).thenReturn(Optional.empty());

        BigDecimal result = service.getElectricityRate();

        assertEquals(new BigDecimal("4.00"), result);
    }

    @Test
    void testGetWaterRate_defaultWhenNotFound() {
        when(repository.findBySettingKey("WATER_RATE")).thenReturn(Optional.empty());

        BigDecimal result = service.getWaterRate();

        assertEquals(new BigDecimal("20.00"), result);
    }

    @Test
    void testUpdateSetting_success() {
        when(repository.findBySettingKey("TEST_KEY")).thenReturn(Optional.of(setting));
        when(repository.save(setting)).thenReturn(setting);

        ApartmentSettings updated = service.updateSetting("TEST_KEY", "999", 77L);

        assertEquals("999", updated.getSettingValue());
        assertEquals(77L, updated.getUpdatedByUserId());
        verify(repository).save(setting);
    }

    @Test
    void testUpdateSetting_notFound_throwsException() {
        when(repository.findBySettingKey("NOT_EXIST")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.updateSetting("NOT_EXIST", "1", 10L)
        );

        assertTrue(ex.getMessage().contains("Setting not found"));
    }

    @Test
    void testCreateOrUpdate_existingSetting_updates() {
        when(repository.findBySettingKey("TEST_KEY")).thenReturn(Optional.of(setting));
        when(repository.save(setting)).thenReturn(setting);

        ApartmentSettings result = service.createOrUpdateSetting(
                "TEST_KEY", "999", "desc updated", 50L
        );

        assertEquals("999", result.getSettingValue());
        assertEquals("desc updated", result.getDescription());
        assertEquals(50L, result.getUpdatedByUserId());
        verify(repository).save(setting);
    }

    @Test
    void testCreateOrUpdate_notExisting_createsNew() {
        when(repository.findBySettingKey("NEW_KEY")).thenReturn(Optional.empty());
        when(repository.save(any(ApartmentSettings.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // return object passed

        ApartmentSettings result = service.createOrUpdateSetting(
                "NEW_KEY", "555", "new desc", 99L
        );

        assertEquals("NEW_KEY", result.getSettingKey());
        assertEquals("555", result.getSettingValue());
        assertEquals("new desc", result.getDescription());
        assertEquals(99L, result.getUpdatedByUserId());
    }
}


