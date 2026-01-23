package com.cookingapp.presentation.controller;

import com.cookingapp.infrastructure.storage.S3Service;
import com.cookingapp.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeImageController")
class RecipeImageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private S3Service s3Service;

    private static final String UPLOAD_ENDPOINT = "/api/recipes/images";
    private static final String TEST_IMAGE_KEY = "images/recipe/test-image.jpg";

    @BeforeEach
    void setUp() {
        RecipeImageController controller = new RecipeImageController(s3Service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/recipes/images")
    class UploadImage {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効なJPEG画像をアップロードした場合、201 Createdとimage keyを返す")
            void shouldReturn201WithImageKeyWhenValidJpegUploaded() throws Exception {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.jpg",
                        "image/jpeg",
                        "test image content".getBytes()
                );
                when(s3Service.uploadRecipeImage(any())).thenReturn(TEST_IMAGE_KEY);

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.imageKey").value(TEST_IMAGE_KEY))
                        .andExpect(jsonPath("$.error").doesNotExist());

                verify(s3Service).uploadRecipeImage(any());
            }

            @Test
            @DisplayName("有効なPNG画像をアップロードした場合、201 Createdを返す")
            void shouldReturn201WhenValidPngUploaded() throws Exception {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.png",
                        "image/png",
                        "test image content".getBytes()
                );
                when(s3Service.uploadRecipeImage(any())).thenReturn(TEST_IMAGE_KEY);

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success").value(true));

                verify(s3Service).uploadRecipeImage(any());
            }

            @Test
            @DisplayName("大文字の拡張子(.JPG)でも正常にアップロードできる")
            void shouldReturn201WhenUppercaseExtension() throws Exception {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.JPG",
                        "image/jpeg",
                        "test image content".getBytes()
                );
                when(s3Service.uploadRecipeImage(any())).thenReturn(TEST_IMAGE_KEY);

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success").value(true));

                verify(s3Service).uploadRecipeImage(any());
            }

            @Test
            @DisplayName("ファイルサイズがちょうど5MBの場合、正常にアップロードできる")
            void shouldReturn201WhenFileSizeIsExactly5MB() throws Exception {
                // Arrange
                byte[] exactContent = new byte[5 * 1024 * 1024]; // Exactly 5MB
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "exact-5mb-image.jpg",
                        "image/jpeg",
                        exactContent
                );
                when(s3Service.uploadRecipeImage(any())).thenReturn(TEST_IMAGE_KEY);

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success").value(true));

                verify(s3Service).uploadRecipeImage(any());
            }

            @Test
            @DisplayName("日本語を含むファイル名でも正常にアップロードできる")
            void shouldReturn201WhenFilenameContainsJapanese() throws Exception {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "レシピ画像.jpg",
                        "image/jpeg",
                        "test image content".getBytes()
                );
                when(s3Service.uploadRecipeImage(any())).thenReturn(TEST_IMAGE_KEY);

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success").value(true));

                verify(s3Service).uploadRecipeImage(any());
            }

            @Test
            @DisplayName("スペースを含むファイル名でも正常にアップロードできる")
            void shouldReturn201WhenFilenameContainsSpaces() throws Exception {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "my recipe image.jpg",
                        "image/jpeg",
                        "test image content".getBytes()
                );
                when(s3Service.uploadRecipeImage(any())).thenReturn(TEST_IMAGE_KEY);

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success").value(true));

                verify(s3Service).uploadRecipeImage(any());
            }
        }

        @Nested
        @DisplayName("異常系 - バリデーションエラー")
        class ValidationFailure {

            @Test
            @DisplayName("ファイルが空の場合、400 Bad Requestを返す")
            void shouldReturn400WhenFileIsEmpty() throws Exception {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.jpg",
                        "image/jpeg",
                        new byte[0]
                );

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error").value("File is required"));

                verify(s3Service, never()).uploadRecipeImage(any());
            }

            @Test
            @DisplayName("ファイルサイズが5MBを超える場合、400 Bad Requestを返す")
            void shouldReturn400WhenFileSizeExceedsLimit() throws Exception {
                // Arrange
                byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "large-image.jpg",
                        "image/jpeg",
                        largeContent
                );

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error").value("File size exceeds maximum limit of 5MB"));

                verify(s3Service, never()).uploadRecipeImage(any());
            }

            @Test
            @DisplayName("許可されていない拡張子(.gif)の場合、400 Bad Requestを返す")
            void shouldReturn400WhenExtensionNotAllowed() throws Exception {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.gif",
                        "image/gif",
                        "test image content".getBytes()
                );

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error").value("Only JPG and PNG files are allowed"));

                verify(s3Service, never()).uploadRecipeImage(any());
            }

            @Test
            @DisplayName("許可されていない拡張子(.bmp)の場合、400 Bad Requestを返す")
            void shouldReturn400WhenBmpExtension() throws Exception {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.bmp",
                        "image/bmp",
                        "test image content".getBytes()
                );

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error").value("Only JPG and PNG files are allowed"));

                verify(s3Service, never()).uploadRecipeImage(any());
            }

            @Test
            @DisplayName("許可されていないContent-Typeの場合、400 Bad Requestを返す")
            void shouldReturn400WhenContentTypeNotAllowed() throws Exception {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.jpg",
                        "image/gif",
                        "test image content".getBytes()
                );

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error").value("Invalid file type. Only JPG and PNG images are allowed"));

                verify(s3Service, never()).uploadRecipeImage(any());
            }

            @Test
            @DisplayName("ファイル名がない場合、400 Bad Requestを返す")
            void shouldReturn400WhenFilenameIsMissing() throws Exception {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "",
                        "image/jpeg",
                        "test image content".getBytes()
                );

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error").value("File name is required"));

                verify(s3Service, never()).uploadRecipeImage(any());
            }
        }

        @Nested
        @DisplayName("異常系 - サーバーエラー")
        class ServerError {

            @Test
            @DisplayName("S3アップロードに失敗した場合、500 Internal Server Errorを返す")
            void shouldReturn500WhenS3UploadFails() throws Exception {
                // Arrange
                MockMultipartFile file = new MockMultipartFile(
                        "file",
                        "test-image.jpg",
                        "image/jpeg",
                        "test image content".getBytes()
                );
                when(s3Service.uploadRecipeImage(any())).thenThrow(new IOException("S3 upload failed"));

                // Act & Assert
                mockMvc.perform(multipart(UPLOAD_ENDPOINT)
                                .file(file))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error").value("Failed to upload image"));

                verify(s3Service).uploadRecipeImage(any());
            }
        }
    }
}
