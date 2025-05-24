package project.ii.flowx.shared.enums;

import lombok.Getter;

@Getter
public enum EntityType {
    TASK("task"),
    MESSAGE("message"),
    CONTENT("content"),
    NOTIFICATION("notification");

    private final String type;

    EntityType(String type) {
        this.type = type;
    }
}
