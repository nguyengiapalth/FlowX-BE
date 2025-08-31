//package project.ii.flowx.controller.rest;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//import project.ii.flowx.module.identify.auth.RoleService;
//import project.ii.flowx.module.identify.entity.Role;
//import project.ii.flowx.model.dto.Response;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("api/role")
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@RequiredArgsConstructor
//@Slf4j
//@CrossOrigin(origins = "*")
//@Tag(name = "Role", description = "Role API")
//@SecurityRequirement(name = "bearerAuth")
//public class RoleController {
//    RoleService roleService;
//
//    @Operation(
//            summary = "Get all roles",
//            description = "Retrieves a list of all roles in the system.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "Roles retrieved successfully"
//                    )
//            }
//    )
//    @GetMapping()
//    public Response<List<Role>> getAllRoles() {
//        log.info("Fetching all roles from the database");
//        return Response.<List<Role>>builder()
//                .data(roleService.getAllRoles())
//                .message("Roles retrieved successfully")
//                .code(200)
//                .build();
//    }
//}
