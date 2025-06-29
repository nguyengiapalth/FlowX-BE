package project.ii.flowx.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import project.ii.flowx.model.dto.FlowXResponse;

/**
 * Global exception handler for FlowX application.
 * Handles various exceptions and returns appropriate HTTP responses.
 * Most of the error handling is in service logic.
 */
@ControllerAdvice
@Slf4j
public class FlowXExceptionHandler {
    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<FlowXResponse> handlingRuntimeException(RuntimeException exception) {
        log.error("RuntimeException: ", exception);
        FlowXResponse response = new FlowXResponse();
        response.setCode(FlowXError.UNCATEGORIZED_EXCEPTION.getCode());
        response.setMessage(exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(value = FlowXException.class)
    ResponseEntity<FlowXResponse> handlingFlowXException(FlowXException exception) {
        FlowXError flowXError = exception.getFlowXError();
        FlowXResponse FlowXResponse = new FlowXResponse();

        FlowXResponse.setCode(flowXError.getCode());
        FlowXResponse.setMessage(exception.getMessage());

        return ResponseEntity.status(flowXError.getStatusCode()).body(FlowXResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<FlowXResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        FlowXError errorCode = FlowXError.ACCESS_DENIED;
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(FlowXResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

}
