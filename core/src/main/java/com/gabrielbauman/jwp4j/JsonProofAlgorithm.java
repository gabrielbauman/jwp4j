package com.gabrielbauman.jwp4j;

public interface JsonProofAlgorithm {

    /**
     * Return true if the {@link JsonProofAlgorithm} implementation can handle JWPs with the given alg identifier.
     *
     * @param alg a string identifier for a JSON proof algorithm.
     * @return true if the implementation can handle the identified JPA, false otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean handles(String alg);

    /**
     * Verify that the proof value of a {@link JsonWebProof} is valid according to the proof algorithm, taking into
     * account the JWP's form (issued or presentation).
     *
     * @param jwp the {@link JsonWebProof} to be validated
     * @throws UnverifiableProofException if the proof cannot be verified (due to algorithm configuration, etc)
     * @throws InvalidProofException      if the proof is invalid
     */
    void verify(JsonWebProof jwp);

    /**
     * Create an issued-form JWP with a valid proof.
     *
     * @param payloads payloads to include in the JWP.
     * @return an issued-form JWP with the given payloads
     */
    JsonWebProof issue(Object... payloads);

    /**
     * Derive a presentation-form JWP with a valid proof from an issued-form JWP.
     *
     * @param issuedFormJwp         The issued-form JWP to derive the presentation-form JWP from.
     * @param includePayloadIndexes indexes of payloads in the issued-form JWP to include in the presentation-form JWP
     * @return a presentation-form JWP with a valid proof
     */
    JsonWebProof derive(JsonWebProof issuedFormJwp, int... includePayloadIndexes);


}
