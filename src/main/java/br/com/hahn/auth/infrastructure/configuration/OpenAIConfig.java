package br.com.hahn.auth.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenAIConfig {

    // Constants
    private static final String APPLICATION_JSON = "application/json";
    private static final String STRING_TYPE = "string";
    private static final String OBJECT_TYPE = "object";
    private static final String UNAUTHORIZED_CODE = "401";
    private static final String NOT_FOUND_CODE = "404";
    private static final String CONFLICT_CODE = "409";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String MESSAGE = "message";
    private static final String TIMESTAMP = "timestamp";
    private static final String USER_NAME = "userName";
    private static final String PASSWORD_CHANGED = "Password successfully changed";
    private static final String USER_REGISTERED = "User successfully registered";


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ms-auth-service")
                        .version("1.0")
                        .description("Servidor de autenticação OAuth2 para aplicações Java Springboot")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Guilherme Hahn")
                                .email("guilherme.f.h@hotmail.com")
                                .url("https://github.com/HahnGuil")
                        )
                ).servers(List.of(
                        new Server().url("hostname:2310/auth-server").description("Desenvolvimento"),
                        new Server().url("hostname:2300/auth-server").description("Ambiente - Docker")
                ));
    }

    @Bean
    public OperationCustomizer specificResponsesCustomizer() {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            String methodName = handlerMethod.getMethod().getName();

            if (operation.getParameters() != null) {
                operation.getParameters().clear();
            }

            switch (methodName) {
                case "register":
                    authRegisterResponse(responses);
                    break;
                case "login":
                    authLoginResponse(responses);
                    break;
                case "refreshToken":
                    authRefreshResponse(responses);
                    break;
                case "changePassword":
                    authChangePasswordResponse(responses);
                    break;
                case "forgotPassword":
                    authForgotPassowrdResponse(responses);
                    break;
                case "validateToken":
                    authValidateRecorverToken(responses);
                    break;
                case "resetePassword":
                    authResetPasswordResponse(responses);
                    break;
                default:
            }

            return operation;
        };
    }

    private void authRegisterResponse(ApiResponses responses) {
        responses.addApiResponse("201", new ApiResponse()
                .description("User successfully registered")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createRegisterResponseSchema()))));

        MediaType conflictMediaType = new MediaType()
                .schema(createErrorSchema("Email already registered. Please log in or recover your password."));
        conflictMediaType.addExamples("alreadyRegistered", new Example()
                .description("User already exist")
                .value(Map.of(
                        MESSAGE, "Email already registered. Please log in or recover your password.",
                        TIMESTAMP, "2024-01-15T10:37:00Z"
                ))
        );
        responses.addApiResponse(CONFLICT_CODE, new ApiResponse()
                .description("User already registered")
                .content(new Content().addMediaType(APPLICATION_JSON, conflictMediaType)));

        MediaType errorMediaType = new MediaType()
                .schema(createErrorSchema("Can't save user on database"));
        errorMediaType.addExamples("dbError", new Example()
                .description("Error saving user to database")
                .value(Map.of(
                        MESSAGE, "Can't save user on database",
                        TIMESTAMP, "2024-01-15T10:31:00Z"
                ))
        );
        responses.addApiResponse("500", new ApiResponse()
                .description("Database server exception")
                .content(new Content().addMediaType(APPLICATION_JSON, errorMediaType)));
    }


    private void authLoginResponse(ApiResponses responses){
        responses.addApiResponse("201", new ApiResponse()
                .description("Login successful")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createLoginResoonseSchema()))));

        MediaType unauthorized = new MediaType()
                .schema(createErrorSchema("Error message"));

        unauthorized.addExamples("blocked",
                new Example()
                        .description("User blocked, password reset required")
                        .value(Map.of(
                                MESSAGE, "This user has been blocked. Use the password reset link.",
                                TIMESTAMP, "2024-01-15T10:40:00Z"
                        ))
        );

        unauthorized.addExamples("oauth",
                new Example()
                        .description("Direct login for OAuth user is not allowed.")
                        .value(Map.of(
                                MESSAGE, "Direct login is not allowed for users created with OAuth.",
                                TIMESTAMP, "2024-01-15T10:31:00Z"
                        ))
        );

        unauthorized.addExamples("invalidCredentials",
                new Example()
                        .description("Credentials invalid")
                        .value(Map.of(
                                MESSAGE, "Invalid email or password.",
                                TIMESTAMP, "2024-01-15T10:30:00Z"
                        ))
        );

        MediaType userNotFound = new MediaType()
                .schema(createErrorSchema("CCC"));

        userNotFound.addExamples("User not Found",
                new Example()
                        .description(USER_NOT_FOUND)
                        .value(Map.of(
                                MESSAGE, "User not found. Check email and password or register a new user.",
                                TIMESTAMP, "2024-01-15T10:30:32Z"
                        ))
        );

        userNotFound.addExamples("User not Found for this application",
                new Example()
                        .description(USER_NOT_FOUND)
                        .value(Map.of(
                                MESSAGE, "User not found for this application",
                                TIMESTAMP, "2024-01-15T10:30:02Z"
                        ))
        );

        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description("Authentication failed")
                .content(new Content().addMediaType(APPLICATION_JSON, unauthorized)));

        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content().addMediaType(APPLICATION_JSON, userNotFound)));
    }

    private void authRefreshResponse(ApiResponses responses){
        responses.addApiResponse("204", new ApiResponse()
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createRefreshSchema()))));

        MediaType unauthorized = new MediaType()
                .schema(createErrorSchema("BBBB"));

        unauthorized.addExamples("Invalid refresh token",
                new Example()
                        .description("Refresh token is invalid")
                        .value(Map.of(
                                MESSAGE, "Invalid refresh token",
                                TIMESTAMP, "2024-01-15T10:30:00Z"
                        ))
        );

        MediaType userNotFound = new MediaType()
                .schema(createErrorSchema("AAA"));

        userNotFound.addExamples("User not found",
                new Example()
                        .description("User not found for application")
                        .value(Map.of(
                                MESSAGE, "User not found for this application",
                                TIMESTAMP, "2024-01-15T10:30:02Z"
                        ))
        );


        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description("Authentication failed")
                .content(new Content().addMediaType(APPLICATION_JSON, unauthorized)));

        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content().addMediaType(APPLICATION_JSON, userNotFound)));
    }

    private void authChangePasswordResponse(ApiResponses responses){
        responses.addApiResponse("200", new ApiResponse()
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createChangePasswordResponseSchema()))));

        MediaType userNotFound = new MediaType()
                .schema(createErrorSchema("CCC"));

        userNotFound.addExamples("User not found",
                new Example()
                        .description(USER_NOT_FOUND)
                        .value(Map.of(
                                MESSAGE, "User not found. Check email and password or register a new user.",
                                TIMESTAMP, "2024-01-15T10:30:32Z"
                        ))
        );

        MediaType unauthorized = new MediaType()
                .schema(createErrorSchema("BB"));

        unauthorized.addExamples("Invalid credentials",
                new Example()
                        .description("Credentials invalid")
                        .value(Map.of(
                                MESSAGE, "Invalid email or password.",
                                TIMESTAMP, "2024-01-15T10:30:00Z"
                        ))
        );

        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description("Authentication failed")
                .content(new Content().addMediaType(APPLICATION_JSON, unauthorized)));

        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content().addMediaType(APPLICATION_JSON, userNotFound)));

    }

    private void authForgotPassowrdResponse(ApiResponses responses){
        responses.addApiResponse("200", new ApiResponse()
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createOkSchema("Reset code send to email")))));

        MediaType userNotFound = new MediaType()
                .schema(createErrorSchema("CCC"));

        userNotFound.addExamples("User not found",
                new Example()
                        .description(USER_NOT_FOUND)
                        .value(Map.of(
                                MESSAGE, "User not found. Check email and password or register a new user.",
                                TIMESTAMP, "2024-01-15T10:30:32Z"
                        ))
        );

        MediaType unauthorized = new MediaType()
                .schema(createErrorSchema("BB"));

        unauthorized.addExamples("Invalid Credentials",
                new Example()
                        .description("Credentials invalid")
                        .value(Map.of(
                                MESSAGE, "Invalid email or password.",
                                TIMESTAMP, "2024-01-15T10:30:00Z"
                        ))
        );

        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description("Authentication failed")
                .content(new Content().addMediaType(APPLICATION_JSON, unauthorized)));

        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content().addMediaType(APPLICATION_JSON, userNotFound)));

    }

    private void authValidateRecorverToken(ApiResponses responses){
        responses.addApiResponse("200", new ApiResponse()
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createRefreshSchema()))));

        MediaType userNotFound = new MediaType()
                .schema(createErrorSchema("CCC"));

        userNotFound.addExamples("User not found",
                new Example()
                        .description(USER_NOT_FOUND)
                        .value(Map.of(
                                MESSAGE, "User not found. Check email and password or register a new user.",
                                TIMESTAMP, "2024-01-15T10:30:32Z"
                        ))
        );

        userNotFound.addExamples("Reset password request not found",
                new Example()
                        .description("Reset password not found")
                        .value(Map.of(
                                MESSAGE, "Not found reset password for this user",
                                TIMESTAMP, "2024-02-15T10:30:32Z"
                        ))
        );

        MediaType unauthorized = new MediaType()
                .schema(createErrorSchema("BF"));

        unauthorized.addExamples("Invalid recover token",
                new Example()
                        .description("Invalid recover token")
                        .value(Map.of(
                                MESSAGE, "Recover Token is expired",
                                TIMESTAMP, "2024-01-15T10:30:50Z"
                        ))
        );

        unauthorized.addExamples("Invalid recover code",
                new Example()
                        .description("Invalid recover code")
                        .value(Map.of(
                                MESSAGE, "Invalid recover code",
                                TIMESTAMP, "2024-01-15T10:30:53Z"
                        ))
        );


        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description("Authentication failed")
                .content(new Content().addMediaType(APPLICATION_JSON, unauthorized)));

        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content().addMediaType(APPLICATION_JSON, userNotFound)));

    }


    private void authResetPasswordResponse(ApiResponses responses){
        responses.addApiResponse("200", new ApiResponse()
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createOkSchema("Password reset successfully")))));

        MediaType userNotFound = new MediaType()
                .schema(createErrorSchema("CCC"));

        userNotFound.addExamples("User not found",
                new Example()
                        .description(USER_NOT_FOUND)
                        .value(Map.of(
                                MESSAGE, "User not found. Check email and password or register a new user.",
                                TIMESTAMP, "2024-01-15T10:30:32Z"
                        ))
        );

        userNotFound.addExamples("Reset password not found",
                new Example()
                        .description("Reset password not found")
                        .value(Map.of(
                                MESSAGE, "Not found reset password for this user",
                                TIMESTAMP, "2024-02-15T10:30:32Z"
                        ))
        );


        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content().addMediaType(APPLICATION_JSON, userNotFound)));


    }

    private Schema<Object> createErrorSchema(String message) {
        Schema<Object> schema = new Schema<>();
        schema.type(OBJECT_TYPE);
        schema.addProperty(MESSAGE, new Schema<String>().type(STRING_TYPE).example(message));
        schema.addProperty(TIMESTAMP, new Schema<String>().type(STRING_TYPE).example("2024-01-15T10:30:00Z"));
        return schema;
    }

    private Schema<Object> createRegisterResponseSchema() {
        Schema<Object> userSchema = new Schema<>();
        userSchema.type(OBJECT_TYPE);
        userSchema.addProperty("userId", new Schema<String>().type(STRING_TYPE).nullable(true));
        userSchema.addProperty(USER_NAME, new Schema<String>().type(STRING_TYPE).example(USER_NAME));
        userSchema.addProperty("email", new Schema<String>().type(STRING_TYPE).example("userEmail@gmail.com"));
        userSchema.addProperty("firstName", new Schema<String>().type(STRING_TYPE).nullable(true));
        userSchema.addProperty("lastName", new Schema<String>().type(STRING_TYPE).nullable(true));
        userSchema.addProperty("pictureUrl", new Schema<String>().type(STRING_TYPE).nullable(true));

        Schema<Object> schema = new Schema<>();
        schema.type(OBJECT_TYPE);
        schema.addProperty("user", userSchema);
        schema.addProperty(MESSAGE, new Schema<String>().type(STRING_TYPE).example(USER_REGISTERED));
        return schema;
    }

    private Schema<Object> createChangePasswordResponseSchema() {
        Schema<Object> schema = new Schema<>();
        schema.type(OBJECT_TYPE);
        schema.addProperty(MESSAGE, new Schema<String>().type(STRING_TYPE).example(PASSWORD_CHANGED));
        return schema;
    }

    private Schema<Object> createLoginResoonseSchema(){
        Schema<Object> loginSchema = new Schema<>();
        loginSchema.type(OBJECT_TYPE);
        loginSchema.addProperty("email", new Schema<String>()
                .type(STRING_TYPE)).example("user-email@email.com");
        loginSchema.addProperty("token", new Schema<String>()
                .type(STRING_TYPE)).example("mXnfgwqwj_V6gBh-PLyUiCfU0eYqSZTO8xcWTgegDx3AHWbw...");
        loginSchema.addProperty("refreshToken", new Schema<String>()
                .type(STRING_TYPE)).example("mXnfgwqwj_V6gBh-PLyUiCfU0eYqSZTO8xcWTgegDx3AHWbh...");

        return loginSchema;

    }

    private Schema<Object> createRefreshSchema(){
        Schema<Object> refreshSchema = new Schema<>();
        refreshSchema.type(OBJECT_TYPE);
        refreshSchema.addProperty("refresnToken", new Schema<String>()
                .type(STRING_TYPE)).example("mXnfgwqwj_V6gBh-PLyUiCfU0eYqSZTO8xcWTgegDx3AHWb...");
        return refreshSchema;
    }


    private Schema<Object> createOkSchema(String message){
        Schema<Object> okSchema = new Schema<>();
        okSchema.type(OBJECT_TYPE);
        okSchema.addProperty(MESSAGE, new Schema<String>()
                .type(STRING_TYPE).example(message));

        return okSchema;
    }
}
