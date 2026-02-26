package com.cookingapp.property;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.valueobject.Language;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ユーザー登録のプロパティベーステスト
 * 
 * Feature: cooking-support-app, Property 1: アカウント作成の成功
 * すべての有効なメールアドレスとパスワード要件を満たすパスワードに対して、
 * アカウント作成は成功し、Cognitoにユーザーが登録される
 * 
 * Validates: Requirements 1.1
 * 
 * このテストは、ユーザーエンティティの作成とパスワードバリデーションに焦点を当てています。
 * 実際のCognito統合やリポジトリ操作は、統合テストで検証されます。
 */
class UserRegistrationPropertyTest {

    /**
     * Property 1: アカウント作成の成功
     * 
     * すべての有効なメールアドレス、パスワード、ニックネーム、言語に対して、
     * ユーザーエンティティが正しく作成されることを検証する
     */
    @Property(tries = 100)
    void accountCreationSucceedsForValidCredentials(
            @ForAll("validEmail") String email,
            @ForAll("validPassword") String password,
            @ForAll("validNickname") String nickname,
            @ForAll Language language
    ) {
        // Arrange & Act: ユーザーエンティティを作成
        // パスワードバリデーションが成功することを確認
        boolean isPasswordValid = User.validatePassword(password);
        assertThat(isPasswordValid).isTrue();

        // ユーザーエンティティを作成
        User user = new User(email, nickname, language);

        // Assert: ユーザーが正しく作成されたことを確認
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getDisplayName()).isEqualTo(nickname); // デフォルトでnicknameと同じ
        assertThat(user.getPreferredLanguage()).isEqualTo(language);
        assertThat(user.getUserId()).isNotNull();
        assertThat(user.getUserId()).isNotEmpty();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getTimezone()).isEqualTo("Asia/Tokyo"); // デフォルトタイムゾーン
        assertThat(user.isMarketingOptOut()).isFalse(); // デフォルトでfalse
    }

    /**
     * Property 2: パスワードバリデーション
     * 
     * すべての有効なパスワードに対して、バリデーションが成功することを検証する
     */
    @Property(tries = 100)
    void passwordValidationSucceedsForValidPasswords(
            @ForAll("validPassword") String password
    ) {
        // Act: パスワードバリデーション
        boolean isValid = User.validatePassword(password);

        // Assert: バリデーションが成功する
        assertThat(isValid).isTrue();
    }

    /**
     * Property 3: 無効なパスワードの拒否
     * 
     * すべての無効なパスワードに対して、バリデーションが失敗することを検証する
     */
    @Property(tries = 100)
    void passwordValidationFailsForInvalidPasswords(
            @ForAll("invalidPassword") String password
    ) {
        // Act: パスワードバリデーション
        boolean isValid = User.validatePassword(password);

        // Assert: バリデーションが失敗する
        assertThat(isValid).isFalse();
    }

    /**
     * 有効なメールアドレスを生成するArbitrary
     */
    @Provide
    Arbitrary<String> validEmail() {
        Arbitrary<String> localPart = Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(1)
                .ofMaxLength(20);
        
        Arbitrary<String> domain = Arbitraries.of(
                "example.com",
                "test.co.jp",
                "mail.com",
                "gmail.com",
                "yahoo.co.jp"
        );

        return Combinators.combine(localPart, domain)
                .as((local, dom) -> local + "@" + dom);
    }

    /**
     * 有効なパスワードを生成するArbitrary
     * 要件: 少なくとも8文字で、大文字、小文字、数字、特殊文字を含む
     */
    @Provide
    Arbitrary<String> validPassword() {
        // 各要件を満たす文字を生成
        Arbitrary<Character> lowercase = Arbitraries.chars().range('a', 'z');
        Arbitrary<Character> uppercase = Arbitraries.chars().range('A', 'Z');
        Arbitrary<Character> digit = Arbitraries.chars().range('0', '9');
        Arbitrary<Character> special = Arbitraries.of('!', '@', '#', '$', '%', '&', '*');

        // 追加の文字（パスワードを8文字以上にするため）
        Arbitrary<String> additionalChars = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .withChars('!', '@', '#', '$', '%', '&', '*')
                .ofMinLength(4)
                .ofMaxLength(12);

        return Combinators.combine(lowercase, uppercase, digit, special, additionalChars)
                .as((l, u, d, s, additional) -> 
                    "" + l + u + d + s + additional
                );
    }

    /**
     * 有効なニックネームを生成するArbitrary
     * 要件: 1-50文字
     */
    @Provide
    Arbitrary<String> validNickname() {
        return Arbitraries.strings()
                .alpha().numeric().withChars('_', '-')
                .ofMinLength(1)
                .ofMaxLength(50);
    }

    /**
     * 無効なパスワードを生成するArbitrary
     * 以下のいずれかの条件を満たさないパスワード:
     * - 8文字未満
     * - 大文字がない
     * - 小文字がない
     * - 数字がない
     * - 特殊文字がない
     */
    @Provide
    Arbitrary<String> invalidPassword() {
        return Arbitraries.oneOf(
                // 7文字以下（短すぎる）
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .withCharRange('A', 'Z')
                        .withCharRange('0', '9')
                        .withChars('!', '@', '#')
                        .ofMinLength(1)
                        .ofMaxLength(7),
                
                // 大文字がない
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .withCharRange('0', '9')
                        .withChars('!', '@', '#')
                        .ofMinLength(8)
                        .ofMaxLength(20),
                
                // 小文字がない
                Arbitraries.strings()
                        .withCharRange('A', 'Z')
                        .withCharRange('0', '9')
                        .withChars('!', '@', '#')
                        .ofMinLength(8)
                        .ofMaxLength(20),
                
                // 数字がない
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .withCharRange('A', 'Z')
                        .withChars('!', '@', '#')
                        .ofMinLength(8)
                        .ofMaxLength(20),
                
                // 特殊文字がない
                Arbitraries.strings()
                        .withCharRange('a', 'z')
                        .withCharRange('A', 'Z')
                        .withCharRange('0', '9')
                        .ofMinLength(8)
                        .ofMaxLength(20)
        );
    }
}
