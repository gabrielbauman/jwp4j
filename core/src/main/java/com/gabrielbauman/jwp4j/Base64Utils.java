package com.gabrielbauman.jwp4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public interface Base64Utils {

    Base64.Decoder decoder = Base64.getUrlDecoder();
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    static String encode(byte[] raw) {
        return encoder.encodeToString(raw);
    }

    static String encode(String raw) {
        return encoder.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static byte[] decode(String base64Url) {
        return decoder.decode(base64Url);
    }

    static String decodeAsString(String base64Url) {
        return new String(decode(base64Url), StandardCharsets.UTF_8);
    }

}
