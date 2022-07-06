package com.gabrielbauman.jwp4j;

import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.EcdsaUsingShaAlgorithm;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jws.JsonWebSignatureAlgorithm;
import org.jose4j.jws.RsaUsingShaAlgorithm;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.JoseException;

import java.security.spec.ECParameterSpec;

import static java.lang.String.format;
import static org.jose4j.jws.AlgorithmIdentifiers.*;

interface JoseUtils {

    static byte[] sign(String algorithm, PublicJsonWebKey signingKey, String signingInput) {

        byte[] result;

        if (null == algorithm || algorithm.isEmpty())
            throw new IllegalArgumentException("algorithm cannot be null or empty");
        else if (null == signingKey || null == signingKey.getPrivateKey())
            throw new IllegalArgumentException("signingKey must have a private key");
        else if (null == signingInput || signingInput.isEmpty())
            throw new IllegalArgumentException("signingInput cannot be null or empty");

        try {
            JsonWebSignature jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(algorithm);
            jws.setKey(signingKey.getPrivateKey());
            jws.setPayload(signingInput);
            jws.sign();
            // Jose4J doesn't let us get at the raw signature value, so we have to do this dance
            result = Base64Utils.decode(jws.getEncodedSignature());
        } catch (Exception e) {
            throw new IllegalStateException("Signature creation failed", e);
        }

        return result;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean verify(String algorithm, PublicJsonWebKey publicKey, String signingInput, byte[] signature) {

        boolean result;

        if (null == algorithm || algorithm.isEmpty())
            throw new IllegalArgumentException("algorithm cannot be null or empty");
        else if (null == publicKey || null == publicKey.getPublicKey())
            throw new IllegalArgumentException("signingKey must have a private key");
        else if (null == signingInput || signingInput.isEmpty())
            throw new IllegalArgumentException("signingInput cannot be null or empty");
        else if (null == signature || signature.length < 1)
            throw new IllegalArgumentException("signature cannot be null or empty");

        try {
            // This is cheesy but necessary because Jose4J doesn't let us just set a signature value on a JWS object
            JsonWebSignature jws =
                    (JsonWebSignature) JsonWebSignature
                            .fromCompactSerialization(
                                    Base64Utils.encode("{\"alg\":\"" + algorithm + "\"}") + "." +
                                            Base64Utils.encode(signingInput) + "." +
                                            Base64Utils.encode(signature));
            jws.setKey(publicKey.getPublicKey());
            result = jws.verifySignature();
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    static PublicJsonWebKey generateKeyWithJwsAlgorithm(String identifier) {
        return generateKeyWithJwsAlgorithm(resolveJwsAlgorithm(identifier));
    }

    static PublicJsonWebKey generateKeyWithJwsAlgorithm(JsonWebSignatureAlgorithm algorithm) {

        PublicJsonWebKey result;

        try {
            if (algorithm instanceof EcdsaUsingShaAlgorithm) {
                ECParameterSpec curve = EllipticCurves.getSpec(((EcdsaUsingShaAlgorithm) algorithm).getCurveName());
                result = EcJwkGenerator.generateJwk(curve);
            } else if (algorithm instanceof RsaUsingShaAlgorithm) {
                result = RsaJwkGenerator.generateJwk(2048);
            } else {
                throw new IllegalArgumentException(format("Can't generate JWK for JWS alg %s", algorithm));
            }
        } catch (JoseException e) {
            throw new IllegalStateException("Unable to generate key", e);
        }

        return result;
    }

    static JsonWebSignatureAlgorithm resolveJwsAlgorithm(String jwsAlgorithmIdentifier) {

        JsonWebSignatureAlgorithm result;

        switch (jwsAlgorithmIdentifier) {
            case ECDSA_USING_P256_CURVE_AND_SHA256:
                result = new EcdsaUsingShaAlgorithm.EcdsaP256UsingSha256();
                break;
            case ECDSA_USING_P384_CURVE_AND_SHA384:
                result = new EcdsaUsingShaAlgorithm.EcdsaP384UsingSha384();
                break;
            case ECDSA_USING_P521_CURVE_AND_SHA512:
                result = new EcdsaUsingShaAlgorithm.EcdsaP521UsingSha512();
                break;
            case RSA_USING_SHA256:
                result = new RsaUsingShaAlgorithm.RsaSha256();
                break;
            case RSA_USING_SHA384:
                result = new RsaUsingShaAlgorithm.RsaSha384();
                break;
            case RSA_USING_SHA512:
                result = new RsaUsingShaAlgorithm.RsaSha512();
                break;
            default:
                throw new IllegalStateException(format("Unsupported JWS algorithm %s", jwsAlgorithmIdentifier));
        }

        return result;
    }

}
