package project.ii.flowx.shared.enums;

import lombok.Getter;

@Getter
public enum PriorityLevel {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    private final String level;

    PriorityLevel(String level) {
        this.level = level;
    }
}
