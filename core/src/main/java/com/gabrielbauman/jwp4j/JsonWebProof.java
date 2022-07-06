package com.gabrielbauman.jwp4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.gabrielbauman.jwp4j.ArrayUtils.split;
import static java.lang.String.format;

/**
 * A {@link JsonWebProof} is a serializable container for a list of payloads which is protected by a cryptographic
 * proof. Proof values are calculated and verified by {@link JsonProofAlgorithm} implementations. This implementation
 * treats {@link JsonWebProof} instances as immutable and verifies their proofs at deserialization time. New
 * containers may be constructed using an initialized {@link JsonProofAlgorithm}, and proofs will be generated at
 * construction time.
 */
public final class JsonWebProof {

    final Map<String, Object> issuerHeader;
    final Map<String, Object> presentationHeader;
    final List<Object> payloads;
    final byte[] proof;
    String serializedForm;

    private String encodedIssuerHeader;
    private String encodedPresentationHeader;
    private String encodedPayloads;
    private String encodedProof;

    JsonWebProof(Map<String, Object> issuerHeader, Map<String, Object> presentationHeader, List<Object> payloads, byte[] proof) {
        this.issuerHeader = issuerHeader;
        this.presentationHeader = presentationHeader;
        this.payloads = payloads;
        this.proof = proof;
        this.serializedForm = null;
        this.encodedIssuerHeader = null;
        this.encodedPresentationHeader = null;
        this.encodedPayloads = null;
        this.encodedProof = null;
    }

    public JsonWebProof(String compactSerializedJwp, JsonProofAlgorithmResolver methodResolver) {

        // Split the compact form apart
        String[] jwpParts = split(compactSerializedJwp, '.');

        if (jwpParts.length != 3 && jwpParts.length != 4) {
            throw new JsonWebProofException(format("Expected either 3 or 4 parts in compact serialized form, got %d", jwpParts.length));
        }

        // Cache the encoded serialized parts
        this.encodedIssuerHeader = jwpParts[0];
        this.encodedPresentationHeader = (jwpParts.length == 4) ? jwpParts[1] : null;
        this.encodedPayloads = jwpParts[jwpParts.length - 2];
        this.encodedProof = jwpParts[jwpParts.length - 1];

        // Decode the proof value
        byte[] proof = Base64Utils.decode(encodedProof);

        // Decode the issuer header
        Map<String, Object> issuerHeader
                = JsonUtils.deserialize(
                        Base64Utils.decodeAsString(encodedIssuerHeader));

        if (!issuerHeader.containsKey("alg"))
            throw new JsonWebProofException("Missing 'alg' claim in issuer header; unable to determine algorithm");

        String alg = (String) issuerHeader.get("alg");

        // Resolve an algorithm instance using the "alg" claim in the issuer header
        JsonProofAlgorithm algorithm = methodResolver.resolve(alg);

        // If we couldn't resolve a proof algorithm, bail
        if (null == algorithm)
            throw new InvalidProofException("Unable to resolve proof algorithm for " + alg);

        // If the resolved proof algorithm can't handle the alg, bail
        if (!algorithm.handles(alg))
            throw new InvalidProofException("Resolved proof algorithm can't handle " + alg);

        // Decode the presentation header, if there is one
        Map<String, Object> presentationHeader =
                (null != encodedPresentationHeader)
                        ? JsonUtils.deserialize(Base64Utils.decodeAsString(encodedPresentationHeader))
                        : null;

        // Split and decode the payloads
        List<Object> payloads =
                Arrays.stream(split(encodedPayloads, '~'))
                        .map(Base64Utils::decodeAsString)
                        .map(JsonUtils::jsonValueToJavaType)
                        .collect(Collectors.toUnmodifiableList());

        // Initialize the JWP.
        this.issuerHeader = issuerHeader;
        this.presentationHeader = presentationHeader;
        this.payloads = payloads;
        this.proof = proof;
        this.serializedForm = compactSerializedJwp;
    }

    public static JsonWebProof parse(JsonProofAlgorithm method, String compactForm) {
        JsonProofAlgorithmResolver resolver = alg -> {
            if (!method.handles(alg))
                throw new InvalidProofException(format("%s cannot handle JWP alg %s", method.getClass().getName(), method));
            return method;
        };
        return parse(resolver, compactForm);
    }

    public static JsonWebProof parse(JsonProofAlgorithmResolver methodResolver, String compactForm) {
        return new JsonWebProof(compactForm, methodResolver);
    }


    public String serialize() {

        // Encode the issuer header
        if (null == encodedIssuerHeader)
            encodedIssuerHeader = Base64Utils.encode(JsonUtils.serialize(this.issuerHeader));

        // Encode the presentation header if there is one
        encodedPresentationHeader =
                this.isInPresentationForm()
                        ? Base64Utils.encode(JsonUtils.serialize(this.presentationHeader))
                        : null;

        // Encode the payloads
        StringJoiner payloadJoiner = new StringJoiner("~");
        this.payloads.stream()
                .map(JsonUtils::javaTypeToJsonValue)
                .map(Base64Utils::encode)
                .forEach(payloadJoiner::add);

        encodedPayloads = payloadJoiner.toString();

        // Encode the proof
        encodedProof = Base64Utils.encode(this.proof);

        // Construct the compact serialized JWP
        StringJoiner jwpJoiner = new StringJoiner(".");
        jwpJoiner.add(encodedIssuerHeader);
        if (null != encodedPresentationHeader)
            jwpJoiner.add(encodedPresentationHeader);
        jwpJoiner.add(encodedPayloads);
        jwpJoiner.add(encodedProof);

        // Store the serialized form in the JsonWebProof, so we never serialize this thing again
        this.serializedForm = jwpJoiner.toString();

        // Return the compact serialized JWP
        return this.serializedForm;
    }

    public boolean isInPresentationForm() {
        return presentationHeader != null;
    }

    public List<Object> getPayloads() {
        return this.payloads;
    }

    @Override
    public String toString() {
        return payloads.toString();
    }

    void setEncodedIssuerHeader(String encodedIssuerHeader) {
        this.encodedIssuerHeader = encodedIssuerHeader;
    }

    String getEncodedIssuerHeader() {
        if (null == encodedIssuerHeader)
            serialize();
        return encodedIssuerHeader;
    }

}
