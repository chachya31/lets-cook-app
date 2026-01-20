package com.cookingapp.infrastructure.repository;

import com.cookingapp.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DynamoDBUserRepository")
class DynamoDBUserRepositoryTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<User> userTable;

    private DynamoDBUserRepository repository;

    private static final String TABLE_NAME = "Users";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(enhancedClient.table(eq(TABLE_NAME), any(TableSchema.class))).thenReturn(userTable);
        repository = new DynamoDBUserRepository(enhancedClient, TABLE_NAME);
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("メールアドレスに一致するユーザーが存在する場合、そのユーザーを返す")
        @SuppressWarnings("unchecked")
        void shouldReturnUserWhenEmailExists() {
            // Arrange
            User expectedUser = createTestUser();

            PageIterable<User> pageIterable = mock(PageIterable.class);
            SdkIterable<User> sdkIterable = createSdkIterable(List.of(expectedUser));

            when(pageIterable.items()).thenReturn(sdkIterable);
            when(userTable.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);

            // Act
            Optional<User> result = repository.findByEmail(TEST_EMAIL);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.get().getEmail()).isEqualTo(TEST_EMAIL);

            verify(userTable).scan(any(ScanEnhancedRequest.class));
        }

        @Test
        @DisplayName("メールアドレスに一致するユーザーが存在しない場合、空のOptionalを返す")
        @SuppressWarnings("unchecked")
        void shouldReturnEmptyOptionalWhenEmailNotFound() {
            // Arrange
            PageIterable<User> pageIterable = mock(PageIterable.class);
            SdkIterable<User> sdkIterable = createSdkIterable(Collections.emptyList());

            when(pageIterable.items()).thenReturn(sdkIterable);
            when(userTable.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);

            // Act
            Optional<User> result = repository.findByEmail("nonexistent@example.com");

            // Assert
            assertThat(result).isEmpty();

            verify(userTable).scan(any(ScanEnhancedRequest.class));
        }

        @Test
        @DisplayName("スキャンリクエストに正しいフィルタ式が含まれている")
        @SuppressWarnings("unchecked")
        void shouldUseCorrectFilterExpression() {
            // Arrange
            PageIterable<User> pageIterable = mock(PageIterable.class);
            SdkIterable<User> sdkIterable = createSdkIterable(Collections.emptyList());

            when(pageIterable.items()).thenReturn(sdkIterable);
            when(userTable.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);

            // Act
            repository.findByEmail(TEST_EMAIL);

            // Assert
            ArgumentCaptor<ScanEnhancedRequest> requestCaptor = ArgumentCaptor.forClass(ScanEnhancedRequest.class);
            verify(userTable).scan(requestCaptor.capture());

            ScanEnhancedRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.filterExpression()).isNotNull();
            assertThat(capturedRequest.filterExpression().expression()).isEqualTo("Email = :email");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("IDに一致するユーザーが存在する場合、そのユーザーを返す")
        void shouldReturnUserWhenIdExists() {
            // Arrange
            User expectedUser = createTestUser();
            when(userTable.getItem(any(Key.class))).thenReturn(expectedUser);

            // Act
            Optional<User> result = repository.findById(TEST_USER_ID);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.get().getEmail()).isEqualTo(TEST_EMAIL);

            verify(userTable).getItem(any(Key.class));
        }

        @Test
        @DisplayName("IDに一致するユーザーが存在しない場合、空のOptionalを返す")
        void shouldReturnEmptyOptionalWhenIdNotFound() {
            // Arrange
            when(userTable.getItem(any(Key.class))).thenReturn(null);

            // Act
            Optional<User> result = repository.findById("nonexistent-id");

            // Assert
            assertThat(result).isEmpty();

            verify(userTable).getItem(any(Key.class));
        }

        @Test
        @DisplayName("正しいキーでDynamoDBにアクセスする")
        void shouldUseCorrectKey() {
            // Arrange
            when(userTable.getItem(any(Key.class))).thenReturn(null);

            // Act
            repository.findById(TEST_USER_ID);

            // Assert
            ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
            verify(userTable).getItem(keyCaptor.capture());

            Key capturedKey = keyCaptor.getValue();
            assertThat(capturedKey.partitionKeyValue().s()).isEqualTo(TEST_USER_ID);
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("ユーザーを保存し、同じユーザーを返す")
        void shouldSaveUserAndReturnIt() {
            // Arrange
            User userToSave = createTestUser();

            // Act
            User result = repository.save(userToSave);

            // Assert
            assertThat(result).isEqualTo(userToSave);
            verify(userTable).putItem(userToSave);
        }

        @Test
        @DisplayName("DynamoDBのputItemメソッドが正しいユーザーで呼ばれる")
        void shouldCallPutItemWithCorrectUser() {
            // Arrange
            User userToSave = createTestUser();

            // Act
            repository.save(userToSave);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userTable).putItem(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(capturedUser.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(capturedUser.getNickname()).isEqualTo("testuser");
        }
    }

    private User createTestUser() {
        return User.builder()
                .userId(TEST_USER_ID)
                .email(TEST_EMAIL)
                .nickname("testuser")
                .displayName("Test User")
                .profileImageUrl("https://example.com/image.jpg")
                .preferredLanguage("ja")
                .timezone("Asia/Tokyo")
                .createdAt("2024-01-01T00:00:00Z")
                .marketingOptOut(false)
                .build();
    }

    private <T> SdkIterable<T> createSdkIterable(List<T> items) {
        return new SdkIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return items.iterator();
            }

            @Override
            public Stream<T> stream() {
                return items.stream();
            }
        };
    }
}
