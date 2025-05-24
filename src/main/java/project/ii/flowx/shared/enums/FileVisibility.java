package project.ii.flowx.shared.enums;

import lombok.Getter;

@Getter
public enum FileVisibility {
    PUBLIC("public"),
    PRIVATE("private"),
    RESTRICTED("restricted"),;

    private final String visibility;

    FileVisibility(String visibility) {
        this.visibility = visibility;
    }

}
