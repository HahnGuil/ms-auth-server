package br.com.hahn.auth.domain.enums;

public enum ScopeToken {
    LOGIN_TOKEN(1, "login_token"),
    REGISTER_TOKEN(2, "register_token"),
    RECOVER_CODE(3, "recoverCode"),
    REFRESH_TOKEN(4, "refresh_token");

    private final int code;
    private final String value;

    ScopeToken(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static ScopeToken fromCode(int code) {
        for (ScopeToken scope : ScopeToken.values()) {
            if (scope.code == code) {
                return scope;
            }
        }
        throw new IllegalArgumentException("Invalid code for ScopeToken: " + code);
    }
}
