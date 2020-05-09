package com.romanwuattier.keygeneratorservice.repository;

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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.romanwuattier.keygeneratorservice.ThreadPoolUtility.IO_FIXED_EXECUTOR;

@AllArgsConstructor
public class MongoDbRepository {
    private final MongoOperations keyOperations;

    public CompletableFuture<List<String>> saveBulk(Collection<String> keys) {
        return CompletableFuture.supplyAsync(() -> {
            List<KeyEntity> bulkEntities = keys.stream()
                                               .map(k -> KeyEntity.builder()
                                                                  .key(k)
                                                                  .used(false)
                                                                  .build())
                                               .collect(Collectors.toUnmodifiableList());

            return keyOperations.insert(bulkEntities, KeyEntity.class);
        }, IO_FIXED_EXECUTOR).thenApply(keyEntities -> keyEntities.stream()
                                                                  .map(KeyEntity::getKey)
                                                                  .collect(Collectors.toUnmodifiableList()));
    }

    public CompletableFuture<Optional<String>> getAndSetToUsed() {
        return CompletableFuture.supplyAsync(() -> {
            Query unUsed = new Query(Criteria.where("used").is(false));
            Update updateToUsed = new Update().set("used", true);
            return Optional.ofNullable(keyOperations.findAndModify(unUsed, updateToUsed, KeyEntity.class))
                           .map(KeyEntity::getKey);
        }, IO_FIXED_EXECUTOR);
    }

    public CompletableFuture<Optional<String>> release(String id) {
        return CompletableFuture.supplyAsync(() -> {
            Query findById = new Query(Criteria.where("_id").is(id));
            Update setToUnused = new Update().set("used", false);
            return Optional.ofNullable(keyOperations.findAndModify(findById, setToUnused, KeyEntity.class))
                           .map(KeyEntity::getKey);
        }, IO_FIXED_EXECUTOR);
    }

    @lombok.Value
    @Builder
    @Document("key")
    static class KeyEntity {
        @Id
        String key;
        boolean used;
    }

    @Configuration
    static class MongoDbConfiguration {
        @Bean
        public MongoOperations keyOperations(
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
        public MongoDbRepository mongoDbRepository(MongoOperations keyOperations) {
            return new MongoDbRepository(keyOperations);
        }
    }
}
