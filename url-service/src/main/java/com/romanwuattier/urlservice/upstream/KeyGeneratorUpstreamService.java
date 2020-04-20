package com.romanwuattier.urlservice.upstream;

import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.adapter.java8.Java8CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

import java.util.concurrent.CompletableFuture;

public interface KeyGeneratorUpstreamService {

    @GET("key/get")
    CompletableFuture<KeyResponse> getKey();

    @Data
    @Builder
    class KeyResponse {
        String key;
    }

    @Configuration
    class KeyGeneratorUpstreamServiceConfiguration {
        @Bean
        public KeyGeneratorUpstreamService provideKeyGeneratorUpstreamService(
            @Value("${infrastructure.upstream-service.key-generator.host}") String host,
            @Value("${infrastructure.upstream-service.key-generator.port}") String port) {
            return new Retrofit.Builder()
                .baseUrl("http://".concat(host).concat(":").concat(port))
                .addCallAdapterFactory(Java8CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(KeyGeneratorUpstreamService.class);
        }
    }
}
