package com.romanwuattier.urlservice.repository;

import com.mongodb.client.MongoClients;
import com.mongodb.client.result.DeleteResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.romanwuattier.urlservice.ThreadPoolUtility.IO_FIXED_EXECUTOR;

@AllArgsConstructor
public class MongoDbRepository {
    private static final String URL_ID_KEY = "_id";

    private final MongoOperations urlOperations;

    public CompletableFuture<String> save(String hash, String url, @Nullable LocalDateTime expireDateTime) {
        return CompletableFuture.supplyAsync(() -> {
            UrlEntity entity = UrlEntity.builder()
                                        .hash(hash)
                                        .url(url)
                                        .expireDateTime(expireDateTime)
                                        .build();
            return urlOperations.insert(entity);
        }, IO_FIXED_EXECUTOR).thenApply(UrlEntity::getHash);
    }

    public CompletableFuture<Boolean> del(String hash) {
        return CompletableFuture.supplyAsync(() -> {
            Query query = new Query(Criteria.where(URL_ID_KEY).is(hash));
            DeleteResult result = urlOperations.remove(query, UrlEntity.class);
            return result.wasAcknowledged() && result.getDeletedCount() > 0;
        }, IO_FIXED_EXECUTOR);
    }

    public CompletableFuture<Optional<String>> get(String hash) {
        return CompletableFuture.supplyAsync(() -> {
            Query query = new Query(Criteria.where(URL_ID_KEY).is(hash));
            return Optional.ofNullable(urlOperations.findOne(query, UrlEntity.class))
                           .map(UrlEntity::getUrl);
        }, IO_FIXED_EXECUTOR);
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
