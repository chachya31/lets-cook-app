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
        String systemPromptText = "あなたは20年のキャリアを持つプロのシェフであり、家事の専門家です。" +
                "ユーザーが持っている食材をもとに、最もおいしく効率的なレシピを提案してください。" +
                "また、食材の保存方法とおおよその賞味期限を親切に教えてください。";
        // String systemPromptText = "당신은 20년 경력의 전문 셰프이자 살림 전문가입니다. " +
        // "사용자가 가진 식재료를 바탕으로 가장 맛있고 효율적인 레시피를 제안하세요. " +
        // "또한 식재료의 보관 방법과 대략적인 유통기한Ｆ을 친절하게 알려주세요." +
        // "[지침 사항]\n" +
        // "1. 오직 '요리', '식재료', '음식', '주방'과 관련된 주제에만 답변하십시오.\n" +
        // "2. 프로그래밍, 정치, 사회 문제, 일반 잡담 등 요리와 무관한 주제가 입력되면 다른 말은 덧붙이지 말고 거절 메시지를
        // 출력하십시오.\n" +
        // "3. 거절 메시지: \"죄송합니다. 저는 요리와 식재료에 관한 정보만 알려드릴 수 있어요. 🍳\"";

        // Content 객체로 변환
        Content systemInstruction = Content.builder()
                .parts(Collections.singletonList(
                        Part.builder().text(systemPromptText).build()))
                .build();

        // 설정(Config) 객체 생성 및 시스템 프롬프트 주입
        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(systemInstruction)
                .temperature(0.5f) // 창의성 조절 (레시피는 약간 창의적인 게 좋으므로 0.7 추천)
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
