package com.cookingapp.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cookingapp.application.usecase.inventory.InventoryUseCase;
import com.cookingapp.domain.entity.InventoryItem;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.infrastructure.security.SecurityUtils;
import com.cookingapp.presentation.controller.InventoryController;
import com.cookingapp.presentation.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * InventoryControllerのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryController ユニットテスト")
class InventoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryUseCase inventoryUseCase;

    @Mock
    private MessageSource messageSource;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private ObjectMapper objectMapper;

    private static final String INVENTORY_ENDPOINT = "/api/inventory";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_ITEM_ID = "item-456";

    @BeforeEach
    void setUp() {
        InventoryController inventoryController = new InventoryController(inventoryUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController)
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();

        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    /**
     * テスト用のInventoryItemを作成
     */
    private InventoryItem createTestInventoryItem(String itemId, String name, BigDecimal quantity,
            String unit, LocalDate expiryDate) {
        return InventoryItem.builder()
                .itemId(itemId)
                .userId(TEST_USER_ID)
                .name(name)
                .quantity(quantity)
                .unit(unit)
                .expiryDate(expiryDate)
                .purchasedAt(Instant.now())
                .createdAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/inventory - 在庫一覧取得")
    class GetInventory {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("在庫一覧を取得する")
            void shouldReturnInventoryList() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                List<InventoryItem> items = List.of(
                        createTestInventoryItem("item-1", "にんじん", new BigDecimal("3"), "本", LocalDate.now().plusDays(5)),
                        createTestInventoryItem("item-2", "牛乳", new BigDecimal("1"), "L", LocalDate.now().plusDays(2)));
                when(inventoryUseCase.getInventory(TEST_USER_ID)).thenReturn(items);

                // Act & Assert
                mockMvc.perform(get(INVENTORY_ENDPOINT))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2))
                        .andExpect(jsonPath("$[0].name").value("にんじん"))
                        .andExpect(jsonPath("$[1].name").value("牛乳"));

                verify(inventoryUseCase).getInventory(TEST_USER_ID);
            }

            @Test
            @DisplayName("賞味期限順でソートして在庫一覧を取得する")
            void shouldReturnInventoryListSortedByExpiry() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                List<InventoryItem> items = List.of(
                        createTestInventoryItem("item-1", "牛乳", new BigDecimal("1"), "L", LocalDate.now().plusDays(2)),
                        createTestInventoryItem("item-2", "にんじん", new BigDecimal("3"), "本", LocalDate.now().plusDays(5)));
                when(inventoryUseCase.getInventoryByExpiryDate(TEST_USER_ID)).thenReturn(items);

                // Act & Assert
                mockMvc.perform(get(INVENTORY_ENDPOINT).param("sortByExpiry", "true"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(2))
                        .andExpect(jsonPath("$[0].name").value("牛乳"))
                        .andExpect(jsonPath("$[1].name").value("にんじん"));

                verify(inventoryUseCase).getInventoryByExpiryDate(TEST_USER_ID);
            }

            @Test
            @DisplayName("空の在庫一覧を取得する")
            void shouldReturnEmptyInventoryList() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);
                when(inventoryUseCase.getInventory(TEST_USER_ID)).thenReturn(List.of());

                // Act & Assert
                mockMvc.perform(get(INVENTORY_ENDPOINT))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.length()").value(0));
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("未認証の場合、403 Forbiddenを返す")
            void shouldReturn403WhenNotAuthenticated() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                        .thenThrow(new UnauthorizedException("User is not authenticated"));

                // Act & Assert
                mockMvc.perform(get(INVENTORY_ENDPOINT))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
            }
        }
    }

    @Nested
    @DisplayName("POST /api/inventory - 在庫追加")
    class AddItem {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("在庫アイテムを追加する")
            void shouldAddInventoryItem() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                LocalDate expiryDate = LocalDate.now().plusDays(7);
                InventoryItem createdItem = createTestInventoryItem(TEST_ITEM_ID, "たまご", new BigDecimal("10"), "個",
                        expiryDate);
                when(inventoryUseCase.addItem(eq(TEST_USER_ID), eq("たまご"), any(BigDecimal.class), eq("個"),
                        eq(expiryDate)))
                        .thenReturn(createdItem);

                String requestBody = """
                        {
                            "name": "たまご",
                            "quantity": 10,
                            "unit": "個",
                            "expiryDate": "%s"
                        }
                        """.formatted(expiryDate);

                // Act & Assert
                mockMvc.perform(post(INVENTORY_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.itemId").value(TEST_ITEM_ID))
                        .andExpect(jsonPath("$.name").value("たまご"))
                        .andExpect(jsonPath("$.quantity").value(10))
                        .andExpect(jsonPath("$.unit").value("個"));

                verify(inventoryUseCase).addItem(eq(TEST_USER_ID), eq("たまご"), any(BigDecimal.class), eq("個"),
                        eq(expiryDate));
            }

            @Test
            @DisplayName("賞味期限なしで在庫アイテムを追加する")
            void shouldAddInventoryItemWithoutExpiryDate() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                InventoryItem createdItem = createTestInventoryItem(TEST_ITEM_ID, "塩", new BigDecimal("1"), "袋", null);
                when(inventoryUseCase.addItem(eq(TEST_USER_ID), eq("塩"), any(BigDecimal.class), eq("袋"), eq(null)))
                        .thenReturn(createdItem);

                String requestBody = """
                        {
                            "name": "塩",
                            "quantity": 1,
                            "unit": "袋"
                        }
                        """;

                // Act & Assert
                mockMvc.perform(post(INVENTORY_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.name").value("塩"))
                        .andExpect(jsonPath("$.expiryDate").isEmpty());
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("未認証の場合、403 Forbiddenを返す")
            void shouldReturn403WhenNotAuthenticated() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                        .thenThrow(new UnauthorizedException("User is not authenticated"));

                String requestBody = """
                        {
                            "name": "たまご",
                            "quantity": 10,
                            "unit": "個"
                        }
                        """;

                // Act & Assert
                mockMvc.perform(post(INVENTORY_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/inventory/{itemId} - 在庫更新")
    class UpdateItem {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("在庫アイテムの数量と賞味期限を更新する")
            void shouldUpdateInventoryItem() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                LocalDate newExpiryDate = LocalDate.now().plusDays(14);
                InventoryItem updatedItem = createTestInventoryItem(TEST_ITEM_ID, "たまご", new BigDecimal("5"), "個",
                        newExpiryDate);
                when(inventoryUseCase.updateItem(eq(TEST_USER_ID), eq(TEST_ITEM_ID), any(BigDecimal.class),
                        eq(newExpiryDate)))
                        .thenReturn(updatedItem);

                String requestBody = """
                        {
                            "quantity": 5,
                            "expiryDate": "%s"
                        }
                        """.formatted(newExpiryDate);

                // Act & Assert
                mockMvc.perform(put(INVENTORY_ENDPOINT + "/" + TEST_ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.itemId").value(TEST_ITEM_ID))
                        .andExpect(jsonPath("$.quantity").value(5));

                verify(inventoryUseCase).updateItem(eq(TEST_USER_ID), eq(TEST_ITEM_ID), any(BigDecimal.class),
                        eq(newExpiryDate));
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("存在しない在庫アイテムの場合、400 Bad Requestを返す")
            void shouldReturn400WhenItemNotFound() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);
                when(inventoryUseCase.updateItem(eq(TEST_USER_ID), eq("non-existent"), any(), any()))
                        .thenThrow(new IllegalArgumentException("Inventory item not found"));

                String requestBody = """
                        {
                            "quantity": 5
                        }
                        """;

                // Act & Assert
                mockMvc.perform(put(INVENTORY_ENDPOINT + "/non-existent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("未認証の場合、403 Forbiddenを返す")
            void shouldReturn403WhenNotAuthenticated() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                        .thenThrow(new UnauthorizedException("User is not authenticated"));

                String requestBody = """
                        {
                            "quantity": 5
                        }
                        """;

                // Act & Assert
                mockMvc.perform(put(INVENTORY_ENDPOINT + "/" + TEST_ITEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/inventory/{itemId} - 在庫削除")
    class DeleteItem {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("在庫アイテムを削除する")
            void shouldDeleteInventoryItem() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete(INVENTORY_ENDPOINT + "/" + TEST_ITEM_ID))
                        .andExpect(status().isNoContent());

                verify(inventoryUseCase).deleteItem(TEST_USER_ID, TEST_ITEM_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("存在しない在庫アイテムの場合、400 Bad Requestを返す")
            void shouldReturn400WhenItemNotFound() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);
                Mockito.doThrow(new IllegalArgumentException("Inventory item not found"))
                        .when(inventoryUseCase).deleteItem(TEST_USER_ID, "non-existent");

                // Act & Assert
                mockMvc.perform(delete(INVENTORY_ENDPOINT + "/non-existent"))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("未認証の場合、403 Forbiddenを返す")
            void shouldReturn403WhenNotAuthenticated() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                        .thenThrow(new UnauthorizedException("User is not authenticated"));

                // Act & Assert
                mockMvc.perform(delete(INVENTORY_ENDPOINT + "/" + TEST_ITEM_ID))
                        .andExpect(status().isForbidden());
            }
        }
    }
}
