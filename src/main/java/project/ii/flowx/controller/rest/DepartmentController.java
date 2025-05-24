package project.ii.flowx.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.ii.flowx.applications.service.manage.DepartmentService;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.department.DepartmentCreateRequest;
import project.ii.flowx.model.dto.department.DepartmentResponse;
import project.ii.flowx.model.dto.department.DepartmentUpdateRequest;

import java.util.List;

@RestController
@RequestMapping("api/department")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Department", description = "Department API")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {
    DepartmentService departmentService;

    @Operation(
            summary = "Get all departments",
            description = "Retrieves a list of all departments in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Departments retrieved successfully"
                    )
            }
    )
    @GetMapping("/getall")
    public FlowXResponse<List<DepartmentResponse>> getAllDepartments() {
        return FlowXResponse.<List<DepartmentResponse>>builder()
                .data(departmentService.getAllDepartments())
                .message("Departments retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get department by ID",
            description = "Retrieves a department by its ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Department retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Department not found"
                    )
            }
    )
    @GetMapping("/get/{id}")
    public FlowXResponse<DepartmentResponse> getDepartmentById(@PathVariable Long id) {
        return FlowXResponse.<DepartmentResponse>builder()
                .data(departmentService.getDepartmentById(id))
                .message("Department retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Create a new department",
            description = "Creates a new department in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Department created successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input"
                    )
            }
    )
    @PostMapping("/create")
    public FlowXResponse<DepartmentResponse> createDepartment(@RequestBody DepartmentCreateRequest request) {
        return FlowXResponse.<DepartmentResponse>builder()
                .data(departmentService.createDepartment(request))
                .message("Department created successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update a department",
            description = "Updates an existing department in the system.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the department to be updated")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Department updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Department not found"
                    )
            }
    )
    @PutMapping("/update/{id}")
    public FlowXResponse<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentUpdateRequest request) {

        return FlowXResponse.<DepartmentResponse>builder()
                .data(departmentService.updateDepartment(id, request))
                .message("Department updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Delete a department",
            description = "Deletes a department from the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Department deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Department not found"
                    )
            }
    )
    @DeleteMapping("/delete/{id}")
    public FlowXResponse<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return FlowXResponse.<Void>builder()
                .message("Department deleted successfully")
                .code(200)
                .build();
    }
}
