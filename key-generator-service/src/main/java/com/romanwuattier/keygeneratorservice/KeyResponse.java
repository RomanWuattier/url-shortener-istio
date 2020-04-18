package com.romanwuattier.keygeneratorservice;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class KeyResponse {
    String key;
}
