package com.cookingapp.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3Service")
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedGetObjectRequest presignedGetObjectRequest;

    private S3Service s3Service;

    private static final String BUCKET_NAME = "test-bucket";
    private static final int PRESIGNED_URL_EXPIRATION_MINUTES = 60;
    private static final String TEST_PRESIGNED_URL = "https://test-bucket.s3.amazonaws.com/images/recipe/test.jpg?presigned";

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client, s3Presigner, BUCKET_NAME, PRESIGNED_URL_EXPIRATION_MINUTES);
    }

    @Nested
    @DisplayName("uploadRecipeImage")
    class UploadRecipeImage {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("正しいバケット名とキーでPutObjectRequestが発行される")
            void shouldIssuePutObjectRequestWithCorrectBucketAndKey() throws IOException {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.jpg",
                        "image/jpeg",
                        "test image content".getBytes()
                );
                when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                        .thenReturn(PutObjectResponse.builder().build());

                // Act
                String result = s3Service.uploadRecipeImage(file);

                // Assert
                ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
                verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

                PutObjectRequest capturedRequest = requestCaptor.getValue();
                assertThat(capturedRequest.bucket()).isEqualTo(BUCKET_NAME);
                assertThat(capturedRequest.key()).startsWith("images/recipe/");
                assertThat(capturedRequest.key()).endsWith(".jpg");
                assertThat(capturedRequest.contentType()).isEqualTo("image/jpeg");
                assertThat(capturedRequest.contentLength()).isEqualTo(file.getSize());

                assertThat(result).startsWith("images/recipe/");
                assertThat(result).endsWith(".jpg");
            }

            @Test
            @DisplayName("PNG画像の場合、正しい拡張子でアップロードされる")
            void shouldUploadPngWithCorrectExtension() throws IOException {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.png",
                        "image/png",
                        "test image content".getBytes()
                );
                when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                        .thenReturn(PutObjectResponse.builder().build());

                // Act
                String result = s3Service.uploadRecipeImage(file);

                // Assert
                ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
                verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

                PutObjectRequest capturedRequest = requestCaptor.getValue();
                assertThat(capturedRequest.key()).endsWith(".png");
                assertThat(capturedRequest.contentType()).isEqualTo("image/png");

                assertThat(result).endsWith(".png");
            }

            @Test
            @DisplayName("大文字の拡張子が小文字に変換される")
            void shouldConvertUppercaseExtensionToLowercase() throws IOException {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.JPG",
                        "image/jpeg",
                        "test image content".getBytes()
                );
                when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                        .thenReturn(PutObjectResponse.builder().build());

                // Act
                String result = s3Service.uploadRecipeImage(file);

                // Assert
                assertThat(result).endsWith(".jpg");
            }

            @Test
            @DisplayName("一意のファイル名（UUID）が生成される")
            void shouldGenerateUniqueFilename() throws IOException {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.jpg",
                        "image/jpeg",
                        "test image content".getBytes()
                );
                when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                        .thenReturn(PutObjectResponse.builder().build());

                // Act
                String result1 = s3Service.uploadRecipeImage(file);
                String result2 = s3Service.uploadRecipeImage(file);

                // Assert
                assertThat(result1).isNotEqualTo(result2);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("S3クライアントが例外をスローした場合、IOExceptionがスローされる")
            void shouldThrowIOExceptionWhenS3ClientFails() {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.jpg",
                        "image/jpeg",
                        "test image content".getBytes()
                );
                when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                        .thenThrow(new RuntimeException("S3 error"));

                // Act & Assert
                assertThatThrownBy(() -> s3Service.uploadRecipeImage(file))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("S3 error");
            }
        }
    }

    @Nested
    @DisplayName("generatePresignedUrl")
    class GeneratePresignedUrl {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("GetObjectPresignRequestが正しいバケットとキーで呼ばれる")
            void shouldCallPresignGetObjectWithCorrectBucketAndKey() throws Exception {
                // Arrange
                String key = "images/recipe/test-image.jpg";
                URL mockUrl = new URL(TEST_PRESIGNED_URL);
                when(presignedGetObjectRequest.url()).thenReturn(mockUrl);
                when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                        .thenReturn(presignedGetObjectRequest);

                // Act
                String result = s3Service.generatePresignedUrl(key);

                // Assert
                ArgumentCaptor<GetObjectPresignRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
                verify(s3Presigner).presignGetObject(requestCaptor.capture());

                GetObjectPresignRequest capturedRequest = requestCaptor.getValue();
                assertThat(capturedRequest.getObjectRequest().bucket()).isEqualTo(BUCKET_NAME);
                assertThat(capturedRequest.getObjectRequest().key()).isEqualTo(key);
                assertThat(capturedRequest.signatureDuration()).isEqualTo(Duration.ofMinutes(PRESIGNED_URL_EXPIRATION_MINUTES));

                assertThat(result).isEqualTo(TEST_PRESIGNED_URL);
            }

            @Test
            @DisplayName("設定された有効期限でURLが生成される")
            void shouldGenerateUrlWithConfiguredExpiration() throws Exception {
                // Arrange
                String key = "images/recipe/test-image.jpg";
                URL mockUrl = new URL(TEST_PRESIGNED_URL);
                when(presignedGetObjectRequest.url()).thenReturn(mockUrl);
                when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                        .thenReturn(presignedGetObjectRequest);

                // Act
                s3Service.generatePresignedUrl(key);

                // Assert
                ArgumentCaptor<GetObjectPresignRequest> requestCaptor = ArgumentCaptor.forClass(GetObjectPresignRequest.class);
                verify(s3Presigner).presignGetObject(requestCaptor.capture());

                GetObjectPresignRequest capturedRequest = requestCaptor.getValue();
                assertThat(capturedRequest.signatureDuration()).isEqualTo(Duration.ofMinutes(60));
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("キーがnullの場合、nullを返す")
            void shouldReturnNullWhenKeyIsNull() {
                // Act
                String result = s3Service.generatePresignedUrl(null);

                // Assert
                assertThat(result).isNull();
                verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
            }

            @Test
            @DisplayName("キーが空文字の場合、nullを返す")
            void shouldReturnNullWhenKeyIsEmpty() {
                // Act
                String result = s3Service.generatePresignedUrl("");

                // Assert
                assertThat(result).isNull();
                verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest.class));
            }
        }
    }
}
