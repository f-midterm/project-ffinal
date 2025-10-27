package apartment.example.backend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Data not found");
        assertEquals("Data not found", ex.getMessage());
    }

    @Test
    void testConstructorWithResourceFieldAndValue() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("User", "id", 10L);

        assertEquals("User not found with id: '10'", ex.getMessage());
    }
}
