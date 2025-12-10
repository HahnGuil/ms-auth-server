package br.com.hahn.auth.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public enum SuccessResponses {

    SEND_RECOVERY_CODE_TO_EMAIL("A validation code has been sent to the email address you provided. Please also check your spam folder. Expect an email from noreply@toxicbet.com.br."),
    PASSWORD_SUCESSFULLY_CHANGE("Password changed successfully.");



    private final String message;


}
