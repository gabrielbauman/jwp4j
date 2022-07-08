package com.gabrielbauman.jwp4j;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonWebProofTest {

    @Test
    public void testSerializationRoundTrip() {

        final String ALG_VALUE = "DUMMY";

        // Construct a dummy algorithm
        JsonProofAlgorithm algorithm = new JsonProofAlgorithm() {

            @Override
            public boolean handles(String alg) {
                return alg.equals(ALG_VALUE);
            }

            @Override
            public JsonWebProof issue(Object... payloads) {
                return new JsonWebProof(
                        Map.of("alg",ALG_VALUE),
                        null,
                        Arrays.asList(payloads),
                        ALG_VALUE.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public JsonWebProof derive(JsonWebProof issuedFormJwp, int... includePayloadIndexes) {
                return new JsonWebProof(
                        Map.of("alg",ALG_VALUE),
                        Map.of("nonce", "DUMMY"),
                        issuedFormJwp.payloads,
                        ALG_VALUE.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public void verify(JsonWebProof jwp) {
                if (!Arrays.equals(jwp.proof, ALG_VALUE.getBytes(StandardCharsets.UTF_8)))
                    throw new InvalidProofException();
            }
        };

        // Construct a JWP using the algorithm
        JsonWebProof constructed = algorithm.issue("", true, false, 1, 2, 3);

        // Make sure it's sane
        assertThat(constructed.issuerHeader.get("alg"))
                .isNotNull()
                .isEqualTo(ALG_VALUE);

        assertThat(constructed.proof)
                .isNotNull()
                .isEqualTo(ALG_VALUE.getBytes(StandardCharsets.UTF_8));

        assertThat(constructed.getPayloads())
                .isNotNull()
                .hasSize(6)
                .containsExactly("", true, false, 1, 2, 3);

        // Serialize and then deserialize it
        JsonWebProof deserialized = JsonWebProof.parse(algorithm, constructed.serialize());

        // Make sure payloads are identically typed and in the same order.
        assertThat(deserialized.getPayloads())
                .containsExactlyElementsOf(constructed.getPayloads());
    }

}
