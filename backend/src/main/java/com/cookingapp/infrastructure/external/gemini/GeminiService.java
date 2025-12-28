package com.cookingapp.infrastructure.external.gemini;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

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

        // 셰프의 역할과 답변 스타일 정의
        String systemPromptText = "당신은 20년 경력의 전문 셰프이자 살림 전문가입니다. " +
                "사용자가 가진 식재료를 바탕으로 가장 맛있고 효율적인 레시피를 제안하세요. " +
                "또한 식재료의 보관 방법과 대략적인 유통기한을 친절하게 알려주세요.";

        // Content 객체로 변환
        Content systemInstruction = Content.builder()
                .parts(Collections.singletonList(
                        Part.builder().text(systemPromptText).build()))
                .build();

        // 설정(Config) 객체 생성 및 시스템 프롬프트 주입
        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(systemInstruction)
                .temperature(0.7f) // 창의성 조절 (레시피는 약간 창의적인 게 좋으므로 0.7 추천)
                .build();

        GenerateContentResponse response = geminiClient.models.generateContent(
                MODEL,
                prompt,
                config);

        String result = response.text();
        log.debug("Generated content length: {}", result != null ? result.length() : 0);

        return result;
    }
}
