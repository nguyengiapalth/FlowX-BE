package project.ii.flowx.shared.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    PENDING("pending"),
    DELETED("deleted");

    final String status;

    UserStatus(String status) {
        this.status = status;
    }

}
