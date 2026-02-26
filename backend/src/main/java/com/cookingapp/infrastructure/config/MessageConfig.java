package com.cookingapp.infrastructure.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * メッセージ国際化設定
 */
@Configuration
public class MessageConfig {

    /**
     * メッセージソースの設定
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.JAPANESE);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    /**
     * ロケールリゾルバーの設定
     * Accept-Languageヘッダーからロケールを取得
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.JAPANESE);
        return localeResolver;
    }
}
