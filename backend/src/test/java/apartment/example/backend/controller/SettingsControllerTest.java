package apartment.example.backend.controller;

import apartment.example.backend.entity.ApartmentSettings;
import apartment.example.backend.service.ApartmentSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SettingsControllerTest {

    private ApartmentSettingsService settingsService;
    private SettingsController controller;

    @BeforeEach
    void setUp() {
        settingsService = mock(ApartmentSettingsService.class);
        controller = new SettingsController(settingsService);
    }

    @Test
    void testGetAllSettings() {
        ApartmentSettings setting = new ApartmentSettings();
        setting.setSettingKey("TEST_KEY");
        setting.setSettingValue("123");
        when(settingsService.getAllSettings()).thenReturn(List.of(setting));

        ResponseEntity<List<ApartmentSettings>> response = controller.getAllSettings();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("TEST_KEY", response.getBody().get(0).getSettingKey());
    }

    @Test
    void testGetSettingByKey_found() {
        ApartmentSettings setting = new ApartmentSettings();
        setting.setSettingKey("TEST_KEY");
        setting.setSettingValue("123");
        when(settingsService.getSettingByKey("TEST_KEY")).thenReturn(Optional.of(setting));

        ResponseEntity<ApartmentSettings> response = controller.getSettingByKey("TEST_KEY");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("123", response.getBody().getSettingValue());
    }

    @Test
    void testGetUtilityRates() {
        when(settingsService.getElectricityRate()).thenReturn(BigDecimal.valueOf(5.0));
        when(settingsService.getWaterRate()).thenReturn(BigDecimal.valueOf(2.5));

        ResponseEntity<Map<String, Object>> response = controller.getUtilityRates();
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(5.0), response.getBody().get("electricityRate"));
        assertEquals(BigDecimal.valueOf(2.5), response.getBody().get("waterRate"));
    }

    @Test
    void testUpdateUtilityRates_success() {
        when(settingsService.getElectricityRate()).thenReturn(BigDecimal.valueOf(5.0));
        when(settingsService.getWaterRate()).thenReturn(BigDecimal.valueOf(2.5));

        ResponseEntity<Map<String, Object>> response = controller.updateUtilityRates(
                Map.of("electricityRate", "5.0", "waterRate", "2.5")
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(BigDecimal.valueOf(5.0), response.getBody().get("electricityRate"));
        assertEquals(BigDecimal.valueOf(2.5), response.getBody().get("waterRate"));

        verify(settingsService).updateSetting("ELECTRICITY_RATE", "5.0", null);
        verify(settingsService).updateSetting("WATER_RATE", "2.5", null);
    }
}

