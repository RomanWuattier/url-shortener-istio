package com.romanwuattier.urlservice;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.romanwuattier.urlservice.UrlGeneratorService.UrlResponse;
import com.romanwuattier.urlservice.UrlGeneratorService.DelResponse;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/url")
@AllArgsConstructor
public class UrlController {
    private final UrlGeneratorService urlGeneratorService;

    @RequestMapping(value = "/create", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<UrlResponse>> create(
        @RequestParam("original_url") String url,
        @RequestParam("expire_date_time") @Nullable LocalDateTime expireDateTime) {
        return urlGeneratorService.generate(url, expireDateTime)
                                  .thenApply(ResponseEntity::ok);
    }

    @RequestMapping(value = "/del", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<DelResponse>> del(@RequestParam("hash") String hash) {
        return urlGeneratorService.del(hash)
                                  .thenApply(delResponse -> delResponse.isDel()
                                      ? ResponseEntity.ok(delResponse)
                                      : ResponseEntity.badRequest().body(delResponse));
    }
}
