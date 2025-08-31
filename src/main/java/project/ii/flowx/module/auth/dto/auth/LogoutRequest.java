package project.ii.flowx.module.auth.dto.auth;


import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class LogoutRequest {
    String token;
}
