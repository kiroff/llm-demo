package org.kiroff.llm.demo.controller;

import org.kiroff.llm.demo.dto.ChunkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/ollama")
@CrossOrigin(origins = "*")
public class OllamaController {

    private static final Logger log = LoggerFactory.getLogger(OllamaController.class.getName());

    private final EmbeddingModel embeddingModel;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory = MessageWindowChatMemory.builder().build();

    public OllamaController(OllamaChatModel chatModel, @Qualifier("mistralAiEmbeddingModel") EmbeddingModel embeddingModel) {
       this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/embeddings")
    public float[] getEmbeddings(
            @RequestParam(name = "message", required = true) String message,
            @RequestParam(name = "conversationId", required = false) String conversationId) {

        return embeddingModel.embed(message);
    }

    @GetMapping("/questions/{message}")
    public ResponseEntity<String> getAnswer(
            @PathVariable("message") String message,
            @RequestParam(name = "conversationId", required = false) String conversationId) {

        return respond(conversationId, message);
    }


    @GetMapping("/recommedations")
    public ResponseEntity<String> getAnswer(
            @RequestParam("city") String city,
            @RequestParam("year") String year,
            @RequestParam("lang") String lang,
            @RequestParam(name = "conversationId", required = false) String conversationId) {

        String template = switch (lang) {
            case "bg" -> """
                    Намери всички активни счетоводни фирми в {city}, които са работили през {year}. За всяка фирма предостави:
                    
                    име на фирмата;
                    ЕИК (ако е публично достъпен);
                    адрес;
                    телефон и имейл;
                    уебсайт;
                    година на регистрация;
                    основни счетоводни услуги;
                    брой служители (ако е наличен);
                    клиентски отзиви и рейтинг;
                    информация дали фирмата е активна към {year}.
                    Подреди резултатите в таблица и включи източници на информация.
                    """;
            case "en" -> """
                    Find all active accounting firms in {city} that were in business in {year}. For each firm, provide:
                    
                    firm name;
                    UIC (if publicly available);
                    address;
                    phone and email;
                    website;
                    year of incorporation;
                    primary accounting services;
                    number of employees (if available);
                    client reviews and ratings;
                    information about whether the firm was active in {year}.
                    Arrange the results in a table and include sources of information.
                    """;
            default -> "Be a helpful assistant. Answer in {lang}.";
        };
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(template)
                .variables(Map.of("year", year, "city", city))
                .build();

        Prompt prompt = promptTemplate.create();
        return respond(conversationId, prompt);

    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChunkResponse> streamChat(@RequestParam(name = "conversationId", required = true) String conversationId, @RequestParam(name = "question", required = false) String question) {
        final String effectiveConversationId = Optional.ofNullable(conversationId).orElseGet(() -> UUID.randomUUID().toString());
        return chatClient
                .prompt()
                .user(question)
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, effectiveConversationId))
                .stream()
                .content()
                .map(ChunkResponse::new);
    }

    private ResponseEntity<String> respond(String conversationId, String message)
    {
        final String effectiveConversationId = Optional.ofNullable(conversationId).orElseGet(() -> UUID.randomUUID().toString());

        final ChatResponse res = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, effectiveConversationId))
                .messages(chatMemory.get(effectiveConversationId))
                .user(message)
                .call()
                .chatResponse();

        return respond(effectiveConversationId, res, message);
    }

    private ResponseEntity<String> respond(String conversationId, Prompt prompt)
    {
        final String effectiveConversationId = Optional.ofNullable(conversationId).orElseGet(() -> UUID.randomUUID().toString());

        final ChatResponse res = chatClient.prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, effectiveConversationId))
                .messages(chatMemory.get(effectiveConversationId))
                .call()
                .chatResponse();

        return respond(effectiveConversationId, res, prompt.getContents());
    }

    private ResponseEntity<String> respond(String conversationId, ChatResponse res, String content)
    {

        if (res == null || res.getResult() == null) {
            return ResponseEntity.noContent().build();
        }

        persistConversation(conversationId, content, res.getResult().getOutput());

        log.info("Tokens used: {}", res.getMetadata().getUsage().getTotalTokens());

        return ResponseEntity.ok(res.getResult().getOutput().getText());
    }

    private void persistConversation(String conversationId, String userText, AssistantMessage assistantMessage) {
        chatMemory.add(conversationId, new UserMessage(userText));
        chatMemory.add(conversationId, assistantMessage);
    }
}
