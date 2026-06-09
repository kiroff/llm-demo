package org.kiroff.llm.demo.config;

import org.springframework.boot.http.client.HttpComponentsClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.JdkClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.autoconfigure.ClientHttpRequestFactoryBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class OllamaClientConfiguration {

    private static final Duration OLLAMA_READ_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration OLLAMA_CONNECTION_TIMEOUT = Duration.ofSeconds(30);

    @Bean
    ClientHttpRequestFactoryBuilderCustomizer<HttpComponentsClientHttpRequestFactoryBuilder> ollamaHttpComponentsTimeoutCustomizer() {
        return builder -> builder.withCustomizer(factory -> {
            factory.setConnectionRequestTimeout(OLLAMA_CONNECTION_TIMEOUT);
            factory.setReadTimeout(OLLAMA_READ_TIMEOUT);
        });
    }

    @Bean
    ClientHttpRequestFactoryBuilderCustomizer<JdkClientHttpRequestFactoryBuilder> ollamaJdkTimeoutCustomizer() {
        return builder -> builder.withCustomizer(factory -> factory.setReadTimeout(OLLAMA_READ_TIMEOUT));
    }
}
