package com.cookingapp.infrastructure.external.gemini;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cookingapp.domain.service.SubscriptionService;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

/**
 * Gemini APIサービス
 * Gemini APIとの通信を担当
 * クレジット管理と多言語対応を含む
 */
@Service
public class GeminiService {

        private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
        private static final String MODEL = "gemini-1.5-flash";

        private final Client geminiClient;
        private final SubscriptionService subscriptionService;

        public GeminiService(Client geminiClient, SubscriptionService subscriptionService) {
                this.geminiClient = geminiClient;
                this.subscriptionService = subscriptionService;
        }

        /**
         * テキスト生成（デフォルト言語: ja）
         */
        public String generateContent(String prompt, String userId) {
                return generateContent(prompt, userId, "ja");
        }

        /**
         * テキスト生成（多言語対応・クレジット管理付き）
         * 
         * @param prompt            ユーザーのプロンプト
         * @param userId            ユーザーID
         * @param preferredLanguage 優先言語コード（"ja", "ko"など）
         * @return 生成されたテキスト
         */
        public String generateContent(String prompt, String userId, String preferredLanguage) {
                log.debug("Generating content for user {} with prompt length: {}", userId, prompt.length());

                // クレジットをチェックして消費（QuotaExceededExceptionがスローされる可能性あり）
                int remainingCredits = subscriptionService.checkAndConsumeCredit(userId);
                log.debug("Credit consumed for user {}, remaining: {}", userId, remainingCredits);

                // 言語に応じたシステムプロンプトを取得
                String systemPromptText = getSystemPrompt(preferredLanguage);

                // Content オブジェクトに変換
                Content systemInstruction = Content.builder()
                                .parts(Collections.singletonList(
                                                Part.builder().text(systemPromptText).build()))
                                .build();

                // 設定(Config) オブジェクト生成およびシステムプロンプト注入
                GenerateContentConfig config = GenerateContentConfig.builder()
                                .systemInstruction(systemInstruction)
                                .temperature(0.5f)
                                .build();

                GenerateContentResponse response = geminiClient.models.generateContent(
                                MODEL,
                                prompt,
                                config);

                String result = response.text();
                log.debug("Generated content length: {}", result != null ? result.length() : 0);

                return result;
        }

        /**
         * 言語に応じたシステムプロンプトを取得
         */
        private String getSystemPrompt(String preferredLanguage) {
                String basePrompt;
                String refusalMessage;

                if ("ko".equalsIgnoreCase(preferredLanguage)) {
                        // 韓国語プロンプト
                        basePrompt = "당신은 20년 경력의 전문 셰프이자 살림 전문가입니다. " +
                                        "사용자가 가진 식재료를 바탕으로 가장 맛있고 효율적인 레시피를 제안하세요. " +
                                        "또한 식재료의 보관 방법과 대략적인 유통기한을 친절하게 알려주세요.";
                        refusalMessage = "죄송합니다. 저는 요리와 식재료에 관한 정보만 알려드릴 수 있어요. 🍳";
                } else {
                        // 日本語プロンプト（デフォルト）
                        basePrompt = "あなたは20年のキャリアを持つプロのシェフであり、家事の専門家です。" +
                                        "ユーザーが持っている食材をもとに、最も美味しく効率的なレシピを提案してください。" +
                                        "また、食材の保存方法とおおよその賞味期限を親切に教えてください。";
                        refusalMessage = "申し訳ありません。私は料理と食材に関する情報のみお伝えできます。🍳";
                }

                // 共通の指針（言語に応じて）
                String guidelines;
                if ("ko".equalsIgnoreCase(preferredLanguage)) {
                        guidelines = "[지침 사항]\n" +
                                        "1. 오직 '요리', '식재료', '음식', '주방'과 관련된 주제에만 답변하십시오.\n" +
                                        "2. 프로그래밍, 정치, 사회 문제, 일반 잡담 등 요리와 무관한 주제가 입력되면 다른 말은 덧붙이지 말고 거절 메시지를 출력하십시오.\n"
                                        +
                                        "3. 거절 메시지: \"" + refusalMessage + "\"\n" +
                                        "4. 사용자의 입력 언어에 맞춰 답변하십시오.";
                } else {
                        guidelines = "[ガイドライン]\n" +
                                        "1. 「料理」「食材」「食べ物」「キッチン」に関連するトピックにのみ回答してください。\n" +
                                        "2. プログラミング、政治、社会問題、雑談など、料理と無関係なトピックが入力された場合は、他の言葉を付け加えずに拒否メッセージを出力してください。\n" +
                                        "3. 拒否メッセージ: \"" + refusalMessage + "\"\n" +
                                        "4. ユーザーの入力言語に合わせて回答してください。";
                }

                return basePrompt + "\n" + guidelines;
        }
}
