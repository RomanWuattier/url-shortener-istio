package com.romanwuattier.keygeneratorservice;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/key")
@AllArgsConstructor
public class KeyGeneratorController {
    private final KeyService keyService;

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<KeyResponse> get() {
        return keyService.generateRandomB62Key();
    }
}
