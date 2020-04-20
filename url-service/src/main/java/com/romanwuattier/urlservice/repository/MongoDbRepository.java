package com.romanwuattier.urlservice.repository;

import com.mongodb.client.MongoClients;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class MongoDbRepository {
    private final MongoOperations urlOperations;

    public CompletableFuture<String> save(String hash, String url, @Nullable LocalDateTime expireDateTime) {
        return CompletableFuture.supplyAsync(() -> {
            UrlEntity entity = UrlEntity.builder()
                                        .hash(hash)
                                        .url(url)
                                        .expireDateTime(expireDateTime)
                                        .build();
            return urlOperations.insert(entity);
        }).thenApply(UrlEntity::getHash);
    }

    @lombok.Value
    @Builder
    @Document("url")
    static class UrlEntity {
        @Id
        String hash;
        String url;
        @Nullable
        LocalDateTime expireDateTime;
    }

    @Configuration
    static class MongoDbConfiguration {
        @Bean
        public MongoOperations urlOperations(
            @Value("${infrastructure.upstream-service.mongodb.host}") String host,
            @Value("${infrastructure.upstream-service.mongodb.port}") int port,
            @Value("${infrastructure.upstream-service.mongodb.user}") String user,
            @Value("${infrastructure.upstream-service.mongodb.password}") String password,
            @Value("${infrastructure.upstream-service.mongodb.db}") String dbName) {
            String connection = String.format(
                "mongodb://%s:%s@%s:%s/?authMechanism=SCRAM-SHA-1", user, password, host, port);
            return new MongoTemplate(MongoClients.create(connection), dbName);
        }

        @Bean
        public MongoDbRepository mongoDbRepository(MongoOperations urlOperations) {
            return new MongoDbRepository(urlOperations);
        }
    }
}
