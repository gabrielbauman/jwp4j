package com.gabrielbauman.jwp4j;

/**
 * This interface is used by JsonWebProof during deserialization to find a JsonWebAlgorithm implementation that can
 * handle the "alg" claim specified by its issuer header.
 */
public interface JsonProofAlgorithmResolver {

    /**
     * Resolve a {@link JsonProofAlgorithm} instance using an 'alg' identifier.
     * @param alg the string identifier representing a {@link JsonProofAlgorithm}.
     * @return a usable instance of a {@link JsonProofAlgorithm} that can handle JWPs with the specified alg value.
     */
    JsonProofAlgorithm resolve(String alg);
}
