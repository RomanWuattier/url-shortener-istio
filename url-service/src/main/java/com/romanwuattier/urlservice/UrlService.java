package com.romanwuattier.urlservice;

import com.romanwuattier.urlservice.repository.MongoDbRepository;
import com.romanwuattier.urlservice.upstream.KeyGeneratorUpstreamService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
class UrlService {
    private final KeyGeneratorUpstreamService keyGeneratorUpstreamService;
    private final MongoDbRepository mongoDbRepository;

    public CompletableFuture<UrlResponse> generate(String url, @Nullable LocalDateTime expireDateTime) {
        return keyGeneratorUpstreamService
            .getKey()
            .thenCompose(response -> mongoDbRepository.save(response.getKey(), url, expireDateTime))
            .thenApply(key -> UrlResponse.builder()
                                         .redirectUrl(String.format(
                                             "http://%s:%s/url/redirect/%s", "{host}", "{port}", key))
                                         .build());
    }

    // Note: This function isn't safe. When the hash is removed from the `mongoDbRepository` and an error occurs
    // while releasing the key in the `keyGeneratorUpstreamService`, then the key is lost forever.
    // There are multiple options solving the problem; one could store the key in a queue and the
    // key-generator-service will listen events.
    public CompletableFuture<DelResponse> del(String hash) {
        return mongoDbRepository
            .del(hash)
            .thenCompose(isDel -> isDel
                ? keyGeneratorUpstreamService.releaseKey(hash)
                : CompletableFuture.failedFuture(new RuntimeException(String.format("Unable to delete: %s", hash)))
            )
            .thenApply(releaseKey -> {
                if (releaseKey.isReleased()) {
                    return DelResponse.builder().isDel(true).hash(releaseKey.getKey()).build();
                } else {
                    throw new RuntimeException(releaseKey.getErrorMessage());
                }
            })
            .exceptionally(throwable -> DelResponse.builder()
                                                   .isDel(false)
                                                   .errorMessage(throwable.getMessage())
                                                   .build());
    }

    public CompletableFuture<String> get(String hash) {
        return mongoDbRepository.get(hash)
                                .thenApply(originalUrl -> originalUrl.orElseThrow(
                                    () -> new IllegalArgumentException(String.format("Unknown hash: %s", hash))
                                ));
    }

    @Value
    @Builder
    public static class UrlResponse {
        String redirectUrl;
    }

    @Value
    @Builder
    public static class DelResponse {
        boolean isDel;
        @Nullable
        String hash;
        @Nullable
        String errorMessage;
    }
}
