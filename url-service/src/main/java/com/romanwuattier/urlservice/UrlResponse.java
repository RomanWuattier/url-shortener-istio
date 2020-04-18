package com.romanwuattier.urlservice;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class UrlResponse {
    String redirectUrl;
}
