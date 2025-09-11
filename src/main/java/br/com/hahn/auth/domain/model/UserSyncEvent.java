package br.com.hahn.auth.domain.model;

import lombok.Data;

@Data
public class UserSyncEvent {
    private String uuid;
    private String applicationCode;
}
