package org.kiroff.llm.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "spring.ai.ollama.chat.model=llama3")
class OllamaConfigurationTests {

    @Autowired
    private Environment environment;

    @Test
    void defaultsToValidOllamaModelName() {
        assertThat(environment.getProperty("spring.ai.ollama.chat.model")).isEqualTo("llama3");
    }
}
