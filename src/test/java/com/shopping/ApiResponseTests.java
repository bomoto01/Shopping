package com.shopping;

import com.shopping.apiResponse.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTests {

    @Test
    @DisplayName("Test success ApiResponse structure")
    void testSuccessApiResponse() {
        ApiResponse<String> response = ApiResponse.success("Hello E-Commerce");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertEquals("Thành công", response.getMessage());
        assertEquals("Hello E-Commerce", response.getData());
        assertNotNull(response.getTimestamp());
        assertNull(response.getErrors());
    }

    @Test
    @DisplayName("Test error ApiResponse structure with errors map")
    void testErrorApiResponse() {
        Map<String, String> errors = Map.of("email", "Email không đúng định dạng");
        ApiResponse<Void> response = ApiResponse.error(400, "Dữ liệu không hợp lệ", errors);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatusCode());
        assertEquals("Dữ liệu không hợp lệ", response.getMessage());
        assertNull(response.getData());
        assertEquals(1, response.getErrors().size());
        assertEquals("Email không đúng định dạng", response.getErrors().get("email"));
    }
}
