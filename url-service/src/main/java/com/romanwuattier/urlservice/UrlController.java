package com.romanwuattier.urlservice;

import com.romanwuattier.urlservice.UrlService.DelResponse;
import com.romanwuattier.urlservice.UrlService.UrlResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/url")
@AllArgsConstructor
public class UrlController {
    private final UrlService urlService;

    @RequestMapping(value = "/create", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<UrlResponse>> create(
        @RequestParam("original_url") URI url,
        @RequestParam("expire_date_time") @Nullable LocalDateTime expireDateTime) {
        return urlService.generate(url.toString(), expireDateTime)
                         .thenApply(ResponseEntity::ok);
    }

    @RequestMapping(value = "/del", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<DelResponse>> del(@RequestParam("hash") String hash) {
        return urlService.del(hash)
                         .thenApply(delResponse -> delResponse.isDel()
                                      ? ResponseEntity.ok(delResponse)
                                      : ResponseEntity.badRequest().body(delResponse));
    }

    @RequestMapping(value = "/redirect/{hash}", method = RequestMethod.GET)
    public CompletableFuture<Void> redirect(
        @PathVariable("hash") String hash, HttpServletResponse httpServletResponse) {
        return urlService.get(hash)
                         .thenApply(originalUrl -> {
                                      httpServletResponse.setHeader("Location", originalUrl);
                                      httpServletResponse.setStatus(302);
                                      return null;
                                  });
    }
}
