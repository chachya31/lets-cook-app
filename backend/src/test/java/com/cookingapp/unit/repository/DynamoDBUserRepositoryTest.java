package com.cookingapp.unit.repository;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.infrastructure.repository.DynamoDBUserRepository;
import com.cookingapp.infrastructure.repository.DynamoDbClientWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DynamoDBUserRepositoryのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DynamoDBUserRepository ユニットテスト")
class DynamoDBUserRepositoryTest {

    @Mock
    private DynamoDbClientWrapper clientWrapper;

    private DynamoDBUserRepository userRepository;

    private static final String TABLE_NAME = "Users";

    @BeforeEach
    void setUp() {
        userRepository = new DynamoDBUserRepository(clientWrapper, TABLE_NAME);
    }

    @Test
    @DisplayName("save - ユーザーが正しく保存される")
    void testSave_Success() {
        // Arrange
        User user = new User("test@example.com", "testuser", Language.JA);
        
        when(clientWrapper.putItem(any(PutItemRequest.class)))
                .thenReturn(PutItemResponse.builder().build());

        // Act
        User result = userRepository.save(user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(clientWrapper).putItem(captor.capture());
        
        PutItemRequest request = captor.getValue();
        assertThat(request.tableName()).isEqualTo(TABLE_NAME);
        assertThat(request.item().get("Email").s()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("findById - IDでユーザーが取得される")
    void testFindById_Success() {
        // Arrange
        String userId = "user-123";
        Map<String, AttributeValue> item = createUserItem(userId, "test@example.com", "testuser");
        
        GetItemResponse response = GetItemResponse.builder()
                .item(item)
                .build();
        
        when(clientWrapper.getItem(any(GetItemRequest.class))).thenReturn(response);

        // Act
        Optional<User> result = userRepository.findById(userId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");

        verify(clientWrapper).getItem(any(GetItemRequest.class));
    }

    @Test
    @DisplayName("findById - 存在しないユーザーでOptional.emptyが返される")
    void testFindById_NotFound() {
        // Arrange
        String userId = "non-existent-user";
        
        GetItemResponse response = GetItemResponse.builder()
                .item(new HashMap<>())
                .build();
        
        when(clientWrapper.getItem(any(GetItemRequest.class))).thenReturn(response);

        // Act
        Optional<User> result = userRepository.findById(userId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("delete - ユーザーが削除される")
    void testDelete_Success() {
        // Arrange
        String userId = "user-123";
        
        when(clientWrapper.deleteItem(any(DeleteItemRequest.class)))
                .thenReturn(DeleteItemResponse.builder().build());

        // Act
        userRepository.delete(userId);

        // Assert
        ArgumentCaptor<DeleteItemRequest> captor = ArgumentCaptor.forClass(DeleteItemRequest.class);
        verify(clientWrapper).deleteItem(captor.capture());
        
        DeleteItemRequest request = captor.getValue();
        assertThat(request.tableName()).isEqualTo(TABLE_NAME);
    }

    @Test
    @DisplayName("existsById - ユーザーの存在確認が正しく動作する")
    void testExistsById_True() {
        // Arrange
        String userId = "user-123";
        Map<String, AttributeValue> item = createUserItem(userId, "test@example.com", "testuser");
        
        GetItemResponse response = GetItemResponse.builder()
                .item(item)
                .build();
        
        when(clientWrapper.getItem(any(GetItemRequest.class))).thenReturn(response);

        // Act
        boolean result = userRepository.existsById(userId);

        // Assert
        assertThat(result).isTrue();
    }

    /**
     * テスト用のDynamoDBアイテムを作成するヘルパーメソッド
     */
    private Map<String, AttributeValue> createUserItem(String userId, String email, String nickname) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("UserId", AttributeValue.builder().s(userId).build());
        item.put("Email", AttributeValue.builder().s(email).build());
        item.put("Nickname", AttributeValue.builder().s(nickname).build());
        item.put("DisplayName", AttributeValue.builder().s(nickname).build());
        item.put("PreferredLanguage", AttributeValue.builder().s("ja").build());
        item.put("CreatedAt", AttributeValue.builder().s(LocalDateTime.now().toString()).build());
        item.put("Timezone", AttributeValue.builder().s("Asia/Tokyo").build());
        item.put("MarketingOptOut", AttributeValue.builder().bool(false).build());
        return item;
    }
}
