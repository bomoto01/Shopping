package com.shopping;

import com.shopping.util.SlugUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlugUtilTests {

    @Test
    @DisplayName("Chuyển đổi chuỗi tiếng Việt có dấu thành slug URL chuẩn")
    void testToSlugVietnamese() {
        assertEquals("ao-thun-nam-co-tron", SlugUtil.toSlug("Áo Thun Nam Cổ Tròn"));
        assertEquals("quan-jeans-slimfit-ong-dung", SlugUtil.toSlug("Quần Jeans Slimfit Ống Đứng"));
        assertEquals("vay-xoe-hoa-nhi-vintage", SlugUtil.toSlug("Váy Xòe Hoa Nhí Vintage"));
        assertEquals("dam-da-hoi-cao-cap", SlugUtil.toSlug("  Đầm Dạ Hội Cao Cấp -- "));
    }
}
