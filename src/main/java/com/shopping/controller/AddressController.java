package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.dto.request.user.AddressRequest;
import com.shopping.dto.response.user.AddressResponse;
import com.shopping.service.interfaces.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Address Book", description = "Các API quản lý sổ địa chỉ nhận hàng của người dùng")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Lấy danh sách địa chỉ nhận hàng", description = "Trả về danh sách các địa chỉ đã lưu của tài khoản hiện tại.")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses() {
        List<AddressResponse> responses = addressService.getMyAddresses();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết một địa chỉ", description = "Lấy thông tin địa chỉ cụ thể thuộc tài khoản hiện tại.")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressById(@PathVariable("id") Long id) {
        AddressResponse response = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Thêm mới địa chỉ nhận hàng", description = "Tạo mới địa chỉ giao hàng và tự động gán mặc định nếu là địa chỉ đầu tiên.")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(@Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.createAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Thêm địa chỉ nhận hàng thành công", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật địa chỉ nhận hàng", description = "Chỉnh sửa thông tin địa chỉ nhận hàng của chính người dùng.")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable("id") Long id,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse response = addressService.updateAddress(id, request);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật địa chỉ thành công", response));
    }

    @PatchMapping("/{id}/default")
    @Operation(summary = "Đặt làm địa chỉ mặc định", description = "Gán địa chỉ được chọn làm địa chỉ giao hàng mặc định.")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(@PathVariable("id") Long id) {
        AddressResponse response = addressService.setDefaultAddress(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Đặt làm địa chỉ mặc định thành công", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa địa chỉ nhận hàng", description = "Xóa một địa chỉ khỏi sổ địa chỉ của tài khoản.")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable("id") Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Xóa địa chỉ thành công", null));
    }
}
