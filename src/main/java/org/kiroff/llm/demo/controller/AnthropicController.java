package org.kiroff.llm.demo.controller;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/anthropic")
@CrossOrigin(origins = "*")
public class AnthropicController {

    private final ChatClient chatClient;

    public AnthropicController(AnthropicChatModel model) {
        this.chatClient = ChatClient.builder(model).build();
    }

    @GetMapping("/{message}")
    public ResponseEntity<String> getAnswer(@PathVariable("message") String message) {
        final String res = chatClient.prompt(message).call().content();
        return ResponseEntity.ok(res);
    }
}
