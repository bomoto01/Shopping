package com.shopping.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.dto.request.CheckoutRequest;
import com.shopping.dto.request.auth.LoginRequest;
import com.shopping.dto.request.auth.RegisterRequest;
import com.shopping.dto.request.cart.AddToCartRequest;
import com.shopping.dto.request.user.AddressRequest;
import com.shopping.entity.Category;
import com.shopping.entity.Product;
import com.shopping.entity.ProductVariant;
import com.shopping.enums.PaymentMethod;
import com.shopping.enums.ProductStatus;
import com.shopping.repository.CategoryRepository;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.ProductVariantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderCheckoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Test
    @DisplayName("Integration Test: Luồng hoàn chỉnh Đăng ký -> Đăng nhập -> Thêm giỏ hàng -> Đặt hàng -> Hủy đơn hoàn kho")
    void testCompleteCheckoutFlow() throws Exception {
        // 1. Đăng ký tài khoản khách hàng mới
        String testEmail = "testcheckout@shopping.vn";
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email(testEmail)
                .password("Password@123")
                .confirmPassword("Password@123")
                .fullName("Nguyễn Khách Mua Hàng")
                .phone("0987654321")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Đăng nhập lấy Bearer token
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password("Password@123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginJson.get("data").get("accessToken").asText();
        assertNotNull(accessToken);

        // 3. Thêm địa chỉ nhận hàng
        AddressRequest addressRequest = AddressRequest.builder()
                .recipientName("Nguyễn Khách Mua Hàng")
                .phone("0987654321")
                .addressLine("456 Nguyễn Huệ")
                .ward("Bến Nghé")
                .district("Quận 1")
                .province("TP. Hồ Chí Minh")
                .isDefault(true)
                .build();

        MvcResult addressResult = mockMvc.perform(post("/api/v1/addresses")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode addressJson = objectMapper.readTree(addressResult.getResponse().getContentAsString());
        long addressId = addressJson.get("data").get("addressId").asLong();

        // 4. Chuẩn bị dữ liệu sản phẩm trong DB
        Category category = categoryRepository.save(Category.builder()
                .name("Thời Trang Nữ")
                .slug("thoi-trang-nu")
                .isActive(true)
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Đầm Hoa Vintage")
                .slug("dam-hoa-vintage")
                .category(category)
                .basePrice(new BigDecimal("450000.00"))
                .status(ProductStatus.ACTIVE)
                .build());

        int initialStock = 15;
        ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                .product(product)
                .sku("DRESS-FLOWER-S")
                .size("S")
                .color("Hoa nhí")
                .price(new BigDecimal("450000.00"))
                .stockQuantity(initialStock)
                .isActive(true)
                .build());

        // 5. Thêm vào giỏ hàng (mua 3 chiếc)
        int buyQty = 3;
        AddToCartRequest cartItemRequest = AddToCartRequest.builder()
                .variantId(variant.getVariantId())
                .quantity(buyQty)
                .build();

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.items").isArray());

        // 6. Đặt hàng (Checkout)
        CheckoutRequest checkoutRequest = CheckoutRequest.builder()
                .addressId(addressId)
                .paymentMethod(PaymentMethod.COD)
                .note("Giao giờ hành chính")
                .build();

        MvcResult orderResult = mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkoutRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        JsonNode orderJson = objectMapper.readTree(orderResult.getResponse().getContentAsString());
        long orderId = orderJson.get("data").get("orderId").asLong();

        // 7. Xác nhận tồn kho đã giảm từ 15 xuống 12
        ProductVariant updatedVariant = productVariantRepository.findById(variant.getVariantId()).orElseThrow();
        assertEquals(initialStock - buyQty, updatedVariant.getStockQuantity());

        // 8. Hủy đơn hàng và kiểm tra hoàn tồn kho
        mockMvc.perform(put("/api/v1/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("reason", "Khách hàng đổi ý"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // Xác nhận tồn kho đã được hoàn lại về 15
        ProductVariant refundedVariant = productVariantRepository.findById(variant.getVariantId()).orElseThrow();
        assertEquals(initialStock, refundedVariant.getStockQuantity());
    }
}
