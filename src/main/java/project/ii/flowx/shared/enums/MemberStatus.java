package project.ii.flowx.shared.enums;

import lombok.Getter;

@Getter
public enum MemberStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    PENDING("pending"),
    ;

    private final String status;

    MemberStatus(String status) {
        this.status = status;
    }

}
