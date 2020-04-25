package com.romanwuattier.urlservice;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

public interface RestConverter {

    @Component
    class URIConverter implements Converter<String, URI> {
        @Override
        public URI convert(@NotNull String source) {
            try {
                return new URI(source);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(String.format("Invalid argument: %s", source));
            }
        }
    }
}
