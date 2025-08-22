package br.com.hahn.auth.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
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

@Configuration
public class OpenAIConfig {

    private static final String APPLICATION_JSON = "application/json";
    private static final String STRING_TYPE = "string";
    private static final String OBJECT_TYPE = "object";
    private static final String UNAUTHORIZED_CODE = "401";
    private static final String NOT_FOUND_CODE = "404";
    private static final String CONFLIT_CODE = "409";
    private static final String USER_NOT_FOUND = "User not found";
    private static final String INVALID_PASSWORD = "Invalid Password";
    private static final String MESSAGE = "message";


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

    private void authRegisterResponse(ApiResponses responses){
        responses.addApiResponse("201", new ApiResponse()
                .description("User successfully registered")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createRegisterResponseSchema()))));
        responses.addApiResponse(CONFLIT_CODE, new ApiResponse()
                .description("User already registered")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema("Email already registered. Please log in or recover your password.")))));
        responses.addApiResponse("500", new ApiResponse()
                .description("Database server exception")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema("Can't save user on database")))));

    }

    private void authLoginResponse(ApiResponses responses){
        responses.addApiResponse("201", new ApiResponse()
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createLoginResoonseSchema()))));
        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description("User block")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema("This user has been blocked. Use the password reset link.")))));
        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description("Not allowed direct login with OAuth")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema("Direct login is not allowed for users created with OAuth.")))));
        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description("Invalid credentials")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema("Invalid email or password.")))));

    }

    private void authRefreshResponse(ApiResponses responses){
        responses.addApiResponse("204", new ApiResponse()
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createRefreshSchema()))));
        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description("Invalid Refresh Token")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema("Invalid refresh token")))));
        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema(USER_NOT_FOUND)))));
    }

    private void authChangePasswordResponse(ApiResponses responses){
        responses.addApiResponse("204", new ApiResponse()
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createChangePasswordResponseSchema()))));
        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema(USER_NOT_FOUND)))));
        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description(INVALID_PASSWORD)
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema("Invalid credencials")))));

    }

    private void authForgotPassowrdResponse(ApiResponses responses){
        responses.addApiResponse("200", new ApiResponse()
                .description("Reset password message")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createOkSchema("Password reset code sent to your email.")))));
        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema("User not found. Check email and password or register a new user.")))));


    }

    private void authValidateRecorverToken(ApiResponses responses){
        responses.addApiResponse("200", new ApiResponse()
                .description("recoverToken")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createOkSchema("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")))));
        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema(USER_NOT_FOUND)))));
        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description("Expired Recover Token")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema("Recover Token is expired")))));
        responses.addApiResponse(UNAUTHORIZED_CODE, new ApiResponse()
                .description("Recorve code Invalid")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema("Invalid recovery code.")))));
    }

    private void authResetPasswordResponse(ApiResponses responses){
        responses.addApiResponse("200", new ApiResponse()
                .description("Reset password message")
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createOkSchema("Password reset successfully")))));
        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema(USER_NOT_FOUND)))));
        responses.addApiResponse(NOT_FOUND_CODE, new ApiResponse()
                .description(USER_NOT_FOUND)
                .content(new Content()
                        .addMediaType(APPLICATION_JSON,
                                new MediaType()
                                        .schema(createErrorSchema("Not found reset password for this user")))));

    }

    private Schema<Object> createChangePasswordResponseSchema(){
        Schema<Object> changePasswordSchema = new Schema<>();
        changePasswordSchema.type(OBJECT_TYPE);
        changePasswordSchema.addProperty(MESSAGE, new Schema<String>()
                .type(STRING_TYPE).example("Password successfully changed"));
        return changePasswordSchema;
    }


    private Schema<Object> createRegisterResponseSchema() {
        Schema<Object> userSchema = new Schema<>();
        userSchema.type(OBJECT_TYPE);
        userSchema.addProperty("userId", new Schema<String>()
                .type(STRING_TYPE).nullable(true));
        userSchema.addProperty("userName", new Schema<String>()
                .type(STRING_TYPE).example("userName"));
        userSchema.addProperty("email", new Schema<String>()
                .type(STRING_TYPE).example("userEmail@gmail.com"));
        userSchema.addProperty("firstName", new Schema<String>()
                .type(STRING_TYPE).nullable(true));
        userSchema.addProperty("lastName", new Schema<String>()
                .type(STRING_TYPE).nullable(true));
        userSchema.addProperty("pictureUrl", new Schema<String>()
                .type(STRING_TYPE).nullable(true));

        Schema<Object> schema = new Schema<>();
        schema.type(OBJECT_TYPE);
        schema.addProperty("user", userSchema);
        schema.addProperty(MESSAGE, new Schema<String>()
                .type(STRING_TYPE).example("User successfully registered"));

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

    private Schema<Object> createErrorSchema(String exampleMessage) {
        Schema<Object> schema = new Schema<>();
        schema.type(OBJECT_TYPE);
        schema.addProperty(MESSAGE, new Schema<String>()
                .type(STRING_TYPE).example(exampleMessage));
        schema.addProperty("timestamp", new Schema<String>()
                .type(STRING_TYPE).format("date-time"));
        return schema;
    }

    private Schema<Object> createOkSchema(String message){
        Schema<Object> okSchema = new Schema<>();
        okSchema.type(OBJECT_TYPE);
        okSchema.addProperty(MESSAGE, new Schema<String>()
                .type(STRING_TYPE).example(message));

        return okSchema;
    }
}
