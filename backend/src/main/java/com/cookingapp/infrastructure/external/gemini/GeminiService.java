package com.cookingapp.infrastructure.external.gemini;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

/**
 * Gemini APIサービス
 * Gemini APIとの通信を担当
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private static final String MODEL = "gemini-2.0-flash";

    private final Client geminiClient;

    public GeminiService(Client geminiClient) {
        this.geminiClient = geminiClient;
    }

    /**
     * テキスト生成
     */
    public String generateContent(String prompt) {
        log.debug("Generating content with prompt length: {}", prompt.length());

        GenerateContentResponse response = geminiClient.models.generateContent(
                MODEL,
                prompt,
                null);

        String result = response.text();
        log.debug("Generated content length: {}", result != null ? result.length() : 0);

        return result;
    }
}
