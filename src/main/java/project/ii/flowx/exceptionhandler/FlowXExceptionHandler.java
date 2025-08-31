package project.ii.flowx.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import project.ii.flowx.dto.Response;

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
    public ResponseEntity<Response> handlingRuntimeException(RuntimeException exception) {
        log.error("RuntimeException: ", exception);
        Response response = new Response();
        response.setCode(FlowXError.UNCATEGORIZED_EXCEPTION.getCode());
        response.setMessage(exception.getMessage());
        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(value = FlowXException.class)
    ResponseEntity<Response> handlingFlowXException(FlowXException exception) {
        FlowXError flowXError = exception.getFlowXError();
        Response Response = new Response();

        Response.setCode(flowXError.getCode());
        Response.setMessage(exception.getMessage());

        return ResponseEntity.status(flowXError.getStatusCode()).body(Response);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<Response> handlingAccessDeniedException(AccessDeniedException exception) {
        FlowXError errorCode = FlowXError.ACCESS_DENIED;
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(Response.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

}
