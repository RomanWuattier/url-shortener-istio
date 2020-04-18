package com.romanwuattier.urlservice;

import com.romanwuattier.urlservice.upstream.KeyGeneratorUpstreamService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
class UrlGeneratorService {
    private final KeyGeneratorUpstreamService keyGeneratorUpstreamService;

    public CompletableFuture<UrlResponse> generate() {
        return keyGeneratorUpstreamService
            .getKey()
            .thenApply(response -> UrlResponse.builder()
                                              .redirectUrl("http://".concat(response.getKey()))
                                              .build());
    }
}
