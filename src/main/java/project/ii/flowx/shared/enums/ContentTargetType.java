package project.ii.flowx.shared.enums;

import lombok.Getter;

@Getter
public enum ContentTargetType {
    PROJECT("project"),
    DEPARTMENT("department"),
    TASK("task"),
    GLOBAL("global"),
    USER("user");

    private final String type;

    ContentTargetType(String type) {
        this.type = type;
    }
}

