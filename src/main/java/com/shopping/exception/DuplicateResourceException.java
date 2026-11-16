package com.shopping.exception;

public class DuplicateResourceException extends AppException {

    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_RESOURCE, message);
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(ErrorCode.DUPLICATE_RESOURCE, String.format("%s đã tồn tại với %s = '%s'", resourceName, fieldName, fieldValue));
    }
}
