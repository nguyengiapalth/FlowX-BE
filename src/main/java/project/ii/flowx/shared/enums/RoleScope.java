package project.ii.flowx.shared.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum RoleScope {
    GLOBAL, DEPARTMENT, PROJECT, TASK, CONTENT, MESSAGE;
}
