package com.cookingapp.infrastructure.external.ollama;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Ollamaサービス
 * ローカルLLMとの通信を担当（REST API使用）
 */
@Service
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "true")
public class OllamaService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String model;

    public OllamaService(
            RestTemplate ollamaRestTemplate,
            @Value("${ollama.baseUrl:http://localhost:11434}") String baseUrl,
            @Value("${ollama.model:llama3.2}") String model) {
        this.restTemplate = ollamaRestTemplate;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    /**
     * テキスト生成
     */
    public String generateContent(String userPrompt) {
        log.debug("Generating content with prompt length: {}", userPrompt.length());

        String systemPromptText = "あなたは20年のキャリアを持つプロのシェフであり、家事の専門家です。" +
                "ユーザーが持っている食材をもとに、最もおいしく効率的なレシピを提案してください。" +
                "また、食材の保存方法とおおよその賞味期限を親切に教えてください。";
        // "[指針事項]\n" +
        // "1. 「料理」「食材」「食べ物」「キッチン」に関連するトピックにのみ回答してください。\n" +
        // "2.
        // プログラミング、政治、社会問題、一般的な雑談など、料理と関係のないトピックが入力された場合は、他の言葉を付け加えずに拒否メッセージを出力してください。\n"
        // +
        // "3. 拒否メッセージ: \"申し訳ありません。私は料理と食材に関する情報のみお伝えできます。�\"";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPromptText),
                        Map.of("role", "user", "content", userPrompt)),
                "stream", false,
                "options", Map.of("temperature", 0.5));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = baseUrl + "/api/chat";
        log.debug("Calling Ollama API: {}", url);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        if (response == null) {
            log.warn("Ollama API returned null response");
            return "";
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) response.get("message");
        String result = message != null ? (String) message.get("content") : "";

        log.debug("Generated content length: {}", result != null ? result.length() : 0);
        return result;
    }
}
