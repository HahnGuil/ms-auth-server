package br.com.hahn.auth.application.dto.response;

import br.com.hahn.auth.application.dto.JwkKey;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwksResponse {
    private List<JwkKey> keys;
}
