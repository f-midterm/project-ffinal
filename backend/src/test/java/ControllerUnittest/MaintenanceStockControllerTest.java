package apartment.example.backend.controller;

import apartment.example.backend.entity.MaintenanceStock;
import apartment.example.backend.entity.MaintenanceStock.Category;
import apartment.example.backend.service.MaintenanceStockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaintenanceStockControllerTest {

    @Mock
    private MaintenanceStockService stockService;

    @InjectMocks
    private MaintenanceStockController controller;

    private MaintenanceStock sampleStock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleStock = new MaintenanceStock();
        sampleStock.setId(1L);
        sampleStock.setItemName("Test Item");
        sampleStock.setCategory(Category.STRUCTURAL);
        sampleStock.setQuantity(5);
    }

    @Test
    void testGetAllStocks() {
        when(stockService.getAllStocks()).thenReturn(Collections.singletonList(sampleStock));

        ResponseEntity<List<MaintenanceStock>> response = controller.getAllStocks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(stockService).getAllStocks();
    }

    @Test
    void testGetStockById() {
        when(stockService.getStockById(1L)).thenReturn(sampleStock);

        ResponseEntity<MaintenanceStock> response = controller.getStockById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Test Item", response.getBody().getItemName());
        verify(stockService).getStockById(1L);
    }

    @Test
    void testGetStocksByCategory() {
        when(stockService.getStocksByCategory(Category.STRUCTURAL)).thenReturn(Collections.singletonList(sampleStock));

        ResponseEntity<List<MaintenanceStock>> response = controller.getStocksByCategory(Category.STRUCTURAL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(stockService).getStocksByCategory(Category.STRUCTURAL);
    }

    @Test
    void testSearchStocks() {
        when(stockService.searchStocks("Test")).thenReturn(Collections.singletonList(sampleStock));

        ResponseEntity<List<MaintenanceStock>> response = controller.searchStocks("Test");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(stockService).searchStocks("Test");
    }

    @Test
    void testGetLowStockItems_DefaultThreshold() {
        when(stockService.getLowStockItems(10)).thenReturn(Collections.singletonList(sampleStock));

        ResponseEntity<List<MaintenanceStock>> response = controller.getLowStockItems(10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(stockService).getLowStockItems(10);
    }

    @Test
    void testCreateStock() {
        when(stockService.createStock(sampleStock)).thenReturn(sampleStock);

        ResponseEntity<MaintenanceStock> response = controller.createStock(sampleStock);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Test Item", response.getBody().getItemName());
        verify(stockService).createStock(sampleStock);
    }

    @Test
    void testUpdateStock() {
        when(stockService.updateStock(1L, sampleStock)).thenReturn(sampleStock);

        ResponseEntity<MaintenanceStock> response = controller.updateStock(1L, sampleStock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(stockService).updateStock(1L, sampleStock);
    }

    @Test
    void testUpdateStockQuantity_Success() {
        Map<String, Integer> payload = Map.of("quantityChange", 5);
        when(stockService.updateStockQuantity(1L, 5)).thenReturn(sampleStock);

        ResponseEntity<MaintenanceStock> response = controller.updateStockQuantity(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(stockService).updateStockQuantity(1L, 5);
    }

    @Test
    void testUpdateStockQuantity_BadRequest() {
        Map<String, Integer> payload = Map.of();
        ResponseEntity<MaintenanceStock> response = controller.updateStockQuantity(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(stockService, never()).updateStockQuantity(anyLong(), anyInt());
    }

    @Test
    void testAddStock_Success() {
        Map<String, Integer> payload = Map.of("quantity", 10);
        when(stockService.addStock(1L, 10)).thenReturn(sampleStock);

        ResponseEntity<MaintenanceStock> response = controller.addStock(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(stockService).addStock(1L, 10);
    }

    @Test
    void testAddStock_BadRequest_NullQuantity() {
        Map<String, Integer> payload = Map.of();
        ResponseEntity<MaintenanceStock> response = controller.addStock(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(stockService, never()).addStock(anyLong(), anyInt());
    }

    @Test
    void testAddStock_BadRequest_NegativeQuantity() {
        Map<String, Integer> payload = Map.of("quantity", -5);
        ResponseEntity<MaintenanceStock> response = controller.addStock(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(stockService, never()).addStock(anyLong(), anyInt());
    }

    @Test
    void testDeleteStock() {
        doNothing().when(stockService).deleteStock(1L);

        ResponseEntity<Map<String, String>> response = controller.deleteStock(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Stock item deleted successfully", response.getBody().get("message"));
        verify(stockService).deleteStock(1L);
    }

    @Test
    void testHealthCheck() {
        ResponseEntity<String> response = controller.healthCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("MaintenanceStockController is working!", response.getBody());
    }
}
