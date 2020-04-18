package com.romanwuattier.keygeneratorservice;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.concurrent.CompletableFuture;

@Component
class KeyService {
    private static final Encoder B64_ENCODER = Base64.getUrlEncoder();
    private static final int KEY_LENGTH = 6;

    public CompletableFuture<KeyResponse> generateRandomB64Key() {
        return CompletableFuture.supplyAsync(() -> {
            byte[] key = RandomStringUtils.random(KEY_LENGTH, true, true).getBytes();
            return KeyResponse.builder()
                              .key(B64_ENCODER.encodeToString(key))
                              .build();
        });
    }
}
