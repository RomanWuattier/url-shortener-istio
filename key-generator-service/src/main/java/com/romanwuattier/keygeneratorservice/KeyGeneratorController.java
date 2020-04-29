package com.romanwuattier.keygeneratorservice;

import com.romanwuattier.keygeneratorservice.KeyService.KeyResponse;
import com.romanwuattier.keygeneratorservice.KeyService.ReleaseResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/key")
@AllArgsConstructor
public class KeyGeneratorController {
    private final KeyService keyService;

    @RequestMapping(value = "/init", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> initializeKeys() {
        return keyService.initializeKeys()
                         .thenApply(__ -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<KeyResponse>> get() {
        return keyService.get()
                         .thenApply(ResponseEntity::ok);
    }

    @RequestMapping(value = "/release", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<ReleaseResponse>> del(@RequestParam("key") String id) {
        return keyService.release(id)
                         .thenApply(releaseResponse -> releaseResponse.isReleased()
                             ? ResponseEntity.ok(releaseResponse)
                             : ResponseEntity.badRequest().body(releaseResponse));
    }
}
