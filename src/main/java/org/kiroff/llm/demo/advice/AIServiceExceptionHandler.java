package org.kiroff.llm.demo.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice
public class AIServiceExceptionHandler {

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<String> handleResourceAccessException(ResourceAccessException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildMessage(exception));
    }

    private String buildMessage(Throwable exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        if (rootCause instanceof java.net.SocketTimeoutException
                || rootCause instanceof java.net.http.HttpTimeoutException) {
            return "Ollama did not respond before the configured timeout expired. " +
                    "Check that the model is loaded, the Ollama server is healthy, and increase the timeout if needed.";
        }

        return "Unable to reach the Ollama service. Check that it is running on http://localhost:11434 and reachable from this app.";
    }
}
