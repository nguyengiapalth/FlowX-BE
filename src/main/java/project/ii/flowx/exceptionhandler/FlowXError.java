package project.ii.flowx.exceptionhandler;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public enum FlowXError {

    UNCATEGORIZED_EXCEPTION(400, "Uncategorized exception", HttpStatusCode.valueOf(400)),
    BAD_REQUEST( 400, "Bad request", HttpStatusCode.valueOf(400)),

    INVALID_PASSWORD( 401, "Invalid password", HttpStatusCode.valueOf(401)),
    INVALID_CREDENTIALS( 401, "Invalid credentials", HttpStatusCode.valueOf(401)),
    INVALID_TOKEN( 401, "Invalid token", HttpStatusCode.valueOf(401)),
    TOKEN_EXPIRED( 401, "Token expired", HttpStatusCode.valueOf(401)),
    UNAUTHORIZED( 401, "Unauthorized", HttpStatusCode.valueOf(401)),
    UNAUTHENTICATED( 401, "Unauthenticated", HttpStatusCode.valueOf(401)),

    FORBIDDEN( 403, "Forbidden", HttpStatusCode.valueOf(403)),
    ACCESS_DENIED(403, "Access denied", HttpStatusCode.valueOf(403)),

    USER_NOT_FOUND(404, "User not found", HttpStatusCode.valueOf(404)),
    NOT_FOUND( 404, "Not found", HttpStatusCode.valueOf(404)),

    USER_ALREADY_EXISTS( 409, "Email already exists", HttpStatusCode.valueOf(409)),
    ALREADY_EXISTS( 409, "Already exists", HttpStatusCode.valueOf(409)),

    INTERNAL_SERVER_ERROR( 500, "Internal server error", HttpStatusCode.valueOf(500)),
    INVALID_INPUT( 400, "Invalid input", HttpStatusCode.valueOf(400)),;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    FlowXError(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
