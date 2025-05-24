package project.ii.flowx.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum RoleScope {
    GLOBAL("global"),
    DEPARTMENT("department"),
    PROJECT("project"),
    USER("user");
    private final String scope;
    RoleScope(String scope) {
        this.scope = scope;
    }
}
