package com.shopping.util;

import com.shopping.exception.UnauthorizedException;
import com.shopping.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static UserPrincipal getRequiredCurrentUserPrincipal() {
        return getCurrentUserPrincipal()
                .orElseThrow(() -> new UnauthorizedException("Vui lòng đăng nhập để thực hiện chức năng này"));
    }

    public static Long getCurrentUserId() {
        return getRequiredCurrentUserPrincipal().getUserId();
    }

    public static String getCurrentUserEmail() {
        return getRequiredCurrentUserPrincipal().getEmail();
    }
}
