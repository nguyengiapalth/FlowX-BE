package project.ii.flowx.model.dto.auth;


import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class LogoutRequest {
    String token;
}
