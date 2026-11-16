package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "API kiểm tra tình trạng hoạt động của hệ thống")
public class HealthCheckController {

    @GetMapping
    @Operation(summary = "Kiểm tra trạng thái server", description = "Trả về thông tin trạng thái hoạt động của Spring Boot server.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        Map<String, Object> status = Map.of(
                "status", "UP",
                "service", "shopping-backend",
                "timestamp", Instant.now().toString(),
                "javaVersion", System.getProperty("java.version")
        );
        return ResponseEntity.ok(ApiResponse.success(200, "Hệ thống hoạt động bình thường", status));
    }
}
