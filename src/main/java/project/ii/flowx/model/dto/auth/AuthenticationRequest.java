package project.ii.flowx.model.dto.auth;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @Schema(description = "Email of the user", example = "nguyengiapnf5@gmail.com")
    String email;
    @Schema(description = "Password of the user", example = "admin")
    String password;
}
