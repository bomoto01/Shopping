package com.shopping;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.exception.GlobalExceptionHandler;
import com.shopping.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHandlerTests {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Test ResourceNotFoundException handling in GlobalExceptionHandler")
    void testResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Product", "id", 999L);
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleAppException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Product không tồn tại với id = '999'", response.getBody().getMessage());
    }
}
