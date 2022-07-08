package com.gabrielbauman.jwp4j;

import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.JsonWebSignatureAlgorithm;
import org.jose4j.lang.JoseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.gabrielbauman.jwp4j.ArrayUtils.split;
import static com.gabrielbauman.jwp4j.JoseUtils.*;
import static com.gabrielbauman.jwp4j.JsonUtils.*;
import static java.lang.String.format;

public final class SingleUseJsonProofAlgorithm implements JsonProofAlgorithm {

    private final PublicJsonWebKey issuerKey;
    private final PublicJsonWebKey presentationKey;
    private final String jwsAlgorithmIdentifier;

    public SingleUseJsonProofAlgorithm(String jwsAlgorithmIdentifier, PublicJsonWebKey issuerKey, PublicJsonWebKey presentationKey) {
        this.issuerKey = issuerKey;
        this.presentationKey = presentationKey;
        this.jwsAlgorithmIdentifier = jwsAlgorithmIdentifier;
    }

    private static byte[] extractSignatureFromProof(byte[] proofValue, int signatureSize, int signatureIndex) {
        byte[] result = new byte[signatureSize];
        System.arraycopy(proofValue, signatureIndex * signatureSize, result, 0, result.length);
        return result;
    }

    @Override
    public boolean handles(String alg) {
        return alg != null && alg.startsWith("SU-");
    }

    @Override
    public void verify(JsonWebProof jwp) {

        // Ensure that the JWP can be verified by this JWP
        final String jpaIdentifier = (String) jwp.issuerHeader.get("alg");

        // Bail if we can't handle the JWP's alg.
        if (!this.handles(jpaIdentifier)) {
            throw new UnverifiableProofException(
                    format("SingleUseJsonProofAlgorithm cannot handle alg %s", jpaIdentifier));
        }

        // Depending on the JWP's form, some payloads will be null and have no corresponding signature in the proof.
        // Build a list of actually-present signed payloads to use when calculating proof's length.
        final List<Object> signedPayloads = (jwp.isInPresentationForm())
                ? jwp.payloads.stream().filter(Objects::nonNull).collect(Collectors.toList())
                : jwp.payloads;

        // Calculate the expected length of the proof in bytes.
        final int signatureCount = signedPayloads.size() + (jwp.isInPresentationForm() ? 2 : 1);
        final int signatureSize = jwp.proof.length / signatureCount;

        // Bail if the proof value has an impossible length
        if (jwp.proof.length % signatureCount != 0)
            throw new InvalidProofException(
                    format("The length of this JWP's proof value must be evenly divisible by %d",
                            signatureCount));

        // Get the ephemeral and presentation keys for the JWP from the issuer header; bail if we can't.
        PublicJsonWebKey ephemeralKey;
        PublicJsonWebKey presentationKey;
        try {
            ephemeralKey = PublicJsonWebKey.Factory.newPublicJwk(
                    Objects.requireNonNull(
                            getJSONObject(jwp.issuerHeader, "proof_jwk")));

            presentationKey = PublicJsonWebKey.Factory.newPublicJwk(
                    Objects.requireNonNull(
                            getJSONObject(jwp.issuerHeader, "presentation_jwk")));

        } catch (NullPointerException | JoseException e) {
            throw new InvalidProofException(e);
        }

        // Get the compact serialized form of the JWP so that we can validate the signatures in the proof. We do it
        // this way so that we're always operating on the original payload - re-serializing json headers would not
        // necessarily generate identical JSON to what was originally signed, and toCompactSerialization returns the
        // json that was originally deserialized (if it exists, and we aren't operating on a freshly issued JWP)
        final String[] jwpParts = split(jwp.serialize(), '.');

        // Pull the JWS algorithm identifier out of the JPA identifier
        final String jwsAlgorithm = jpaIdentifier.substring(3);

        // Keep the index of the current signature
        int signatureIndex = 0;

        // Verify the issuer header's signature
        if (!JoseUtils.verify(
                jwsAlgorithm,
                this.issuerKey,
                Base64Utils.decodeAsString(jwpParts[0]), extractSignatureFromProof(jwp.proof, signatureSize, signatureIndex)
        ))
            throw new InvalidProofException("The issuer header was signed incorrectly; this JWP is invalid.");

        // Verify the presentation header if it exists
        if (jwp.isInPresentationForm() && !JoseUtils.verify(
                jwsAlgorithm,
                presentationKey,
                Base64Utils.decodeAsString(jwpParts[1]), extractSignatureFromProof(jwp.proof, signatureSize, ++signatureIndex)
        ))
            throw new InvalidProofException("The presentation header was signed incorrectly; this JWP is invalid.");

        // Verify the payload signatures
        for (Object payload : signedPayloads) {
            if (!JoseUtils.verify(
                    jwsAlgorithm,
                    ephemeralKey,
                    javaTypeToJsonValue(payload), extractSignatureFromProof(jwp.proof, signatureSize, ++signatureIndex)
            ))
                throw new InvalidProofException("A payload was signed incorrectly; this JWP is invalid.");
        }

        // The JWP's proof is valid!
    }

    public JsonWebProof issue(Object... payloads) {

        if (null == payloads || payloads.length < 1)
            throw new JsonWebProofException("Unable to issue JWP: payloads required");

        // Resolve the JWS algorithm from the JPA alg in the issuer header.
        JsonWebSignatureAlgorithm algorithm = resolveJwsAlgorithm(jwsAlgorithmIdentifier);

        // Generate an ephemeral key using the algorithm
        PublicJsonWebKey ephemeralKey = generateKeyWithJwsAlgorithm(algorithm);

        // Build the issuer header
        Map<String, Object> issuerHeader = Map.of(
                "alg", format("SU-%s", jwsAlgorithmIdentifier),
                "proof_jwk", ephemeralKey.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY),
                "presentation_jwk", this.presentationKey.toParams(JsonWebKey.OutputControlLevel.PUBLIC_ONLY));

        try {
            // Prepare to start building the proof
            ByteArrayOutputStream proof = new ByteArrayOutputStream();

            // Sign the issuer header with the issuer signing key; append signature to the proof.
            proof.write(sign(jwsAlgorithmIdentifier, this.issuerKey, serialize(issuerHeader)));

            // Sign each payload's string form with the ephemeral key and append each signature to the proof.
            for (Object payload : payloads) {
                proof.write(sign(jwsAlgorithmIdentifier, ephemeralKey, javaTypeToJsonValue(payload)));
            }

            // Return a new JWP.
            return new JsonWebProof(issuerHeader, null, List.of(payloads), proof.toByteArray());

        } catch (IOException e) {
            throw new JsonWebProofException("Unable to issue JWP", e);
        }
    }

    public JsonWebProof derive(JsonWebProof issuedFormJwp, int... includePayloadIndexes) {

        if (null == issuedFormJwp)
            throw new IllegalArgumentException("issuedFormJwp cannot be null");
        else if (issuedFormJwp.isInPresentationForm())
            throw new IllegalArgumentException("The JWP is already in presentation form");
        else if (null == issuedFormJwp.issuerHeader || issuedFormJwp.issuerHeader.isEmpty())
            throw new IllegalArgumentException("The JWP's issuer header is empty");
        else if (!handles((String) issuedFormJwp.issuerHeader.get("alg")))
            throw new IllegalArgumentException("The JWP specifies some other JPA");
        else if (null == issuedFormJwp.proof)
            throw new IllegalArgumentException("The JWP's issued-form proof is not present");
        else if (null == this.presentationKey || null == this.presentationKey.getPrivateKey())
            throw new IllegalStateException("The JPA was not configured with the presentation private key");

        List<Object> originalPayloads = issuedFormJwp.getPayloads();
        List<Object> sparsePayloads = new ArrayList<>();

        // Build a sparse list of payloads
        for (int i = 0; i < originalPayloads.size(); i++) {
            int finalI = i;
            sparsePayloads.add(
                    Arrays.stream(includePayloadIndexes).anyMatch(value -> (value == finalI))
                            ? originalPayloads.get(i)
                            : null);
        }

        // Build a presentation header with a random nonce
        Map<String, Object> presentationHeader = Map.of("nonce", UUID.randomUUID().toString());
        String presentationHeaderJson = JsonUtils.serialize(presentationHeader);

        // Calculate the expected length of the proof in bytes.
        final int signatureCount = sparsePayloads.size() + 1;
        final int signatureSize = issuedFormJwp.proof.length / signatureCount;

        // Build the presentation proof using signatures from the issued form
        try {

            // Prepare to start building the proof
            ByteArrayOutputStream proof = new ByteArrayOutputStream();

            // Copy the issuer header signature into the proof
            proof.write(extractSignatureFromProof(issuedFormJwp.proof, signatureSize, 0));

            // Sign the presentation header and add it to the proof
            proof.write(JoseUtils.sign(jwsAlgorithmIdentifier, presentationKey, presentationHeaderJson));

            // Append signatures for each non-null payload to the proof
            for (int i = 0; i < sparsePayloads.size(); i++) {
                if (null != sparsePayloads.get(i)) {
                    proof.write(extractSignatureFromProof(issuedFormJwp.proof, signatureSize, i + 1));
                }
            }

            // Build a new compact serialized form.
            JsonWebProof result =  new JsonWebProof(issuedFormJwp.issuerHeader, presentationHeader, sparsePayloads, proof.toByteArray());
            result.setEncodedIssuerHeader(issuedFormJwp.getEncodedIssuerHeader());
            return result;

        } catch (IOException e) {
            throw new JsonWebProofException("Unable to present JWP", e);
        }
    }

}
