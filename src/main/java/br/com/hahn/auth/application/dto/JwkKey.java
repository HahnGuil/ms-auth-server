package br.com.hahn.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwkKey {

    @JsonProperty("kty")
    private String keyType;

    @JsonProperty("kid")
    private String keyId;

    @JsonProperty("use")
    private String use;

    @JsonProperty("alg")
    private String algorithm;

    @JsonProperty("n")
    private String modulus;

    @JsonProperty("e")
    private String exponent;
}
