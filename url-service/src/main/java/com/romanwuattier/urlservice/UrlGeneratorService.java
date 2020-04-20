package com.romanwuattier.urlservice;

import com.romanwuattier.urlservice.upstream.KeyGeneratorUpstreamService;
import com.romanwuattier.urlservice.repository.MongoDbRepository;
import lombok.AllArgsConstructor;
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
}
