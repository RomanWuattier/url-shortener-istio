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
class UrlGeneratorService {
    private final KeyGeneratorUpstreamService keyGeneratorUpstreamService;
    private final MongoDbRepository mongoDbRepository;

    public CompletableFuture<UrlResponse> generate(String url, @Nullable LocalDateTime expireDateTime) {
        return keyGeneratorUpstreamService
            .getKey()
            .thenCompose(response -> mongoDbRepository.save(response.getKey(), url, expireDateTime))
            .thenApply(key -> UrlResponse.builder()
                                         .redirectUrl("http://".concat(key))
                                         .build());
    }

    public CompletableFuture<DelResponse> del(String hash) {
        return mongoDbRepository.del(hash)
                                .thenApply(success -> success
                                    ? DelResponse.builder().isDel(true).hash(hash).build()
                                    : DelResponse.builder()
                                                 .isDel(false)
                                                 .errorMessage(String.format("Unknown hash: %s", hash))
                                                 .build());
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
