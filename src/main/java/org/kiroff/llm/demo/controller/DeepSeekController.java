package org.kiroff.llm.demo.controller;

import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deepseek")
@CrossOrigin(origins = "*")
public class DeepSeekController {

    private final DeepSeekChatModel model;

    public DeepSeekController(DeepSeekChatModel model) {
        this.model = model;
    }

    @GetMapping("/{message}")
    public ResponseEntity<String> getAnswer(@PathVariable("message") String message) {
        final String res = model.call(message);
        return ResponseEntity.ok(res);
    }
}
