package com.gabrielbauman.jwp4j;

import org.jose4j.jwk.PublicJsonWebKey;
import org.junit.jupiter.api.Test;

import static com.gabrielbauman.jwp4j.JoseUtils.generateKeyWithJwsAlgorithm;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.jose4j.jws.AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256;

public class SingleUseJsonProofAlgorithmTest {

    @Test
    public void testRoundTrip() {

        PublicJsonWebKey issuerKey = generateKeyWithJwsAlgorithm(ECDSA_USING_P256_CURVE_AND_SHA256);
        PublicJsonWebKey holderKey = generateKeyWithJwsAlgorithm(ECDSA_USING_P256_CURVE_AND_SHA256);

        JsonProofAlgorithm algorithm =
                new SingleUseJsonProofAlgorithm(
                        ECDSA_USING_P256_CURVE_AND_SHA256,
                        issuerKey,
                        holderKey);

        JsonWebProof proof = algorithm.issue("Gabriel", "Bauman");

        assertThat(proof.isInPresentationForm())
                .isFalse();
        assertThat(proof.issuerHeader)
                .isNotEmpty()
                .containsKeys("proof_jwk", "presentation_jwk", "alg");
        assertThat(proof.presentationHeader)
                .isNull();
        assertThat(proof.proof)
                .isNotEmpty()
                .hasSize(192);
        assertThat(proof.getPayloads())
                .hasSize(2)
                .containsExactly("Gabriel", "Bauman");

        JsonWebProof deserialized = JsonWebProof.parse(algorithm, proof.serialize());
        assertThat(deserialized.isInPresentationForm())
                .isFalse();
        assertThat(deserialized.issuerHeader)
                .isNotEmpty()
                .containsKeys("proof_jwk", "presentation_jwk", "alg");
        assertThat(deserialized.presentationHeader)
                .isNull();
        assertThat(deserialized.proof)
                .isNotEmpty()
                .hasSize(192);
        assertThat(deserialized.getPayloads())
                .hasSize(2)
                .containsExactly("Gabriel", "Bauman");

        JsonWebProof presentation = algorithm.derive(deserialized, 1);
        assertThat(presentation.isInPresentationForm())
                .isTrue();
        assertThat(presentation.issuerHeader)
                .isNotEmpty()
                .containsKeys("proof_jwk", "presentation_jwk", "alg");
        assertThat(presentation.presentationHeader)
                .isNotNull()
                .containsKeys("nonce");
        assertThat(presentation.proof)
                .isNotEmpty()
                .hasSize(192);
        assertThat(presentation.getPayloads())
                .hasSize(2)
                .containsExactly(null, "Bauman");

        assertThatNoException().isThrownBy(() -> algorithm.verify(presentation));

    }

}
