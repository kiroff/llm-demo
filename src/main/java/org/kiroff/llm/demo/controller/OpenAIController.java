package org.kiroff.llm.demo.controller;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/openai")
@CrossOrigin(origins = "*")
public class OpenAIController {

    private final OpenAiChatModel model;

    public OpenAIController(OpenAiChatModel model) {
        this.model = model;
    }

    @GetMapping("/{message}")
    public ResponseEntity<String> getAnswer(@PathVariable("message") String message) {
        final String res = model.call(message);
        return ResponseEntity.ok(res);
    }
}
