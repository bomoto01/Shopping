package com.shopping.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION("UNCATEGORIZED", "Lỗi máy chủ không xác định.", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.", HttpStatus.NOT_FOUND),
    BAD_REQUEST("BAD_REQUEST", "Yêu cầu không hợp lệ.", HttpStatus.BAD_REQUEST),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "Dữ liệu đã tồn tại trong hệ thống.", HttpStatus.CONFLICT),
    UNAUTHORIZED("UNAUTHORIZED", "Xác thực không thành công hoặc phiên đăng nhập đã hết hạn.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "Bạn không có quyền thực hiện thao tác này.", HttpStatus.FORBIDDEN),
    VALIDATION_FAILED("VALIDATION_FAILED", "Dữ liệu đầu vào không hợp lệ.", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token đã hết hạn sử dụng.", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("TOKEN_INVALID", "Token không hợp lệ hoặc đã bị thu hồi.", HttpStatus.UNAUTHORIZED),
    STOCK_NOT_ENOUGH("STOCK_NOT_ENOUGH", "Số lượng tồn kho không đủ đáp ứng.", HttpStatus.CONFLICT),
    COUPON_INVALID("COUPON_INVALID", "Mã giảm giá không hợp lệ hoặc không đủ điều kiện.", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS("REVIEW_ALREADY_EXISTS", "Sản phẩm trong đơn hàng này đã được đánh giá.", HttpStatus.CONFLICT),
    SPOOFED_EVENT_REJECTED("SPOOFED_EVENT_REJECTED", "Loại sự kiện không được phép gửi từ giao diện người dùng.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
