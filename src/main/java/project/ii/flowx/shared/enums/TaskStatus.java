package project.ii.flowx.shared.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {
    TO_DO("to_do"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String status;

    TaskStatus(String status) {
        this.status = status;
    }
}
