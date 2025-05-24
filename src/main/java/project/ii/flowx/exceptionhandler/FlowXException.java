package project.ii.flowx.exceptionhandler;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowXException extends RuntimeException {
    public FlowXException(FlowXError flowXError, String message) {
        super(flowXError.getMessage());
        this.flowXError = flowXError;
        this.message = message;
    }

    private FlowXError flowXError;
    private String message;
}
