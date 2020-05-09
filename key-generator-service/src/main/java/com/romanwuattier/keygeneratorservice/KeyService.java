package com.romanwuattier.keygeneratorservice;

import com.romanwuattier.keygeneratorservice.repository.MongoDbRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.romanwuattier.keygeneratorservice.ThreadPoolUtility.CPU_FIXED_EXECUTOR;

@Component
@AllArgsConstructor
class KeyService {
    private static final int INITIAL_NUMBER_OF_KEYS = 128;
    private static final int KEY_LENGTH = 6;

    private final MongoDbRepository mongoDbRepository;

    public CompletableFuture<Void> initializeKeys() {
        return CompletableFuture.supplyAsync(() -> {
            Set<String> uniqKeys = new HashSet<>();
            while (uniqKeys.size() < INITIAL_NUMBER_OF_KEYS) {
                uniqKeys.add(generateRandomB62Key());
            }
            return uniqKeys;
        }, CPU_FIXED_EXECUTOR).thenCompose(mongoDbRepository::saveBulk).thenApply(__ -> null);
    }

    public CompletableFuture<KeyResponse> get() {
        return mongoDbRepository.getAndSetToUsed()
                                .thenApply(key -> key.map(k -> KeyResponse.builder()
                                                                          .key(k)
                                                                          .build())
                                                     .orElseThrow(() -> new RuntimeException("No key available")));
    }

    public CompletableFuture<ReleaseResponse> release(String id) {
        return mongoDbRepository.release(id)
                                .thenApply(key -> key.orElseThrow(() -> new IllegalArgumentException(
                                    String.format("Key not found: %s", key)
                                )))
                                .thenApply(key -> ReleaseResponse.builder().released(true).key(key).build())
                                .exceptionally(throwable -> ReleaseResponse.builder()
                                                                           .released(false)
                                                                           .errorMessage(throwable.getMessage())
                                                                           .build());
    }

    /**
     * Generate a Base62 ([A-Z, a-z, 0-9]) 6 digits long key.
     * Using Base62 encoding, a 6 digits long key would result in 62^6 ~= 57 billion possible strings.
     */
    private String generateRandomB62Key() {
        return RandomStringUtils.random(KEY_LENGTH, true, true);
    }

    @Value
    @Builder
    static class KeyResponse {
        String key;
    }

    @Value
    @Builder
    static class ReleaseResponse {
        boolean released;
        @Nullable
        String key;
        @Nullable
        String errorMessage;
    }
}
