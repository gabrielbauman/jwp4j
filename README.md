# JSON Web Proofs for Java

A JSON Web Proof (JWP) is a container format similar in purpose to a JSON Web Signature (JWS). The intent of JSON Web 
Proofs is to establish a common container format for multiple payloads that can be integrity-verified against a
cryptographic proof value also in the container. JSON Proof Algorithms (JPAs) abstract signing methods away from the JWP
container format.

Whereas a JWS can only contain a single signed payload, a JWP can contain multiple signed payloads. A JWP's proof can
contain much more than a single cryptographic signature for payloads.

The JWP container format aims to support newer algorithms and cryptographic techniques which establish the role of a
_prover_, which has limited capabilities to derive new forms of a signed message which can still be cryptographically
verified.

This is an early implementation of the draft standard, which can be found [here][1].

## Usage

To use this implementation, add the following to your `pom.xml`:

```xml

<dependencies>
    <!-- The core library -->
    <dependency>
        <groupId>com.gabrielbauman.jwp4j</groupId>
        <artifactId>jwp-core</artifactId>
    </dependency>
    <!-- The Single Use Json Proof Algorithm -->
    <dependency>
        <groupId>com.gabrielbauman.jwp4j</groupId>
        <artifactId>jwp-algorithm-su</artifactId>
    </dependency>
</dependencies>
```

## Examples

Here's an example of the full lifecycle, making use of `SingleUseJsonProofAlgorithm`.

```java
// Generate some keys needed by SingleUseJsonProofAlgorithm
PublicJsonWebKey issuerKey = generateKeyWithJwsAlgorithm(ECDSA_USING_P256_CURVE_AND_SHA256);
PublicJsonWebKey holderKey = generateKeyWithJwsAlgorithm(ECDSA_USING_P256_CURVE_AND_SHA256);

// Initialize the JPA
JsonProofAlgorithm algorithm =
    new SingleUseJsonProofAlgorithm(
        ECDSA_USING_P256_CURVE_AND_SHA256,
        issuerKey,
        holderKey);

// Create a new immutable JsonWebProof in issued form, ready for storage by a holder.
JsonWebProof issuedForm = algorithm.issue("Gabriel", "Bauman");

// Verify the proof.
algorithm.verify(issuedForm);

// Derive an immutable JsonWebProof in presentation form, ready for presentation to a verifier.
// Selectively disclose only the payload at index 1 in the issuedForm JWP. 
JsonWebProof presentationForm = algorithm.present(issuedForm, 1);

// Verify the proof.
algorithm.verify(presentationForm);
```

## Caveat

This library will change rapidly as the draft standard evolves and should not be relied on in any production system. 

[1]:https://json-web-proofs.github.io/json-web-proofs/draft-jmiller-json-web-proof.html#name-jwp-format
