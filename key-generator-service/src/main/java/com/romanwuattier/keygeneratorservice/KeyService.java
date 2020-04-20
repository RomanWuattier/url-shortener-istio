package com.romanwuattier.keygeneratorservice;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
class KeyService {
    private static final int KEY_LENGTH = 6;

    /**
     * Generate a Base62 ([A-Z, a-z, 0-9]) 6 digits long key.
     * Using Base62 encoding, a 6 digits long key would result in 62^6 ~= 57 billion possible strings.
     */
    public CompletableFuture<KeyResponse> generateRandomB62Key() {
        return CompletableFuture.supplyAsync(() ->
            KeyResponse.builder()
                       .key(RandomStringUtils.random(KEY_LENGTH, true, true))
                       .build());
    }
}
