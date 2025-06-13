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
import project.ii.flowx.applications.service.communicate.ContentService;
import project.ii.flowx.model.dto.FlowXResponse;
import project.ii.flowx.model.dto.content.ContentCreateRequest;
import project.ii.flowx.model.dto.content.ContentResponse;
import project.ii.flowx.model.dto.content.ContentUpdateRequest;
import project.ii.flowx.shared.enums.ContentTargetType;

import java.util.List;

@RestController
@RequestMapping("api/content")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Content", description = "Content API")
public class ContentController {
    ContentService contentService;

    @Operation(
            summary = "Create a new content",
            description = "Creates a new content in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Content created successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid content details provided"
                    )
            }
    )
    @PostMapping("/create")
    public FlowXResponse<ContentResponse> createContent(@RequestBody ContentCreateRequest request) {
        log.info("Creating content with body: {}", request.getBody());
        return FlowXResponse.<ContentResponse>builder()
                .data(contentService.createContent(request))
                .message("Content created successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update content",
            description = "Updates the details of an existing content.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the content to be updated")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Content updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Content not found"
                    )
            }
    )
    @PutMapping("/update/{id}")
    public FlowXResponse<ContentResponse> updateContent(@PathVariable Long id, @RequestBody ContentUpdateRequest request) {
        return FlowXResponse.<ContentResponse>builder()
                .data(contentService.updateContent(id, request))
                .message("Content updated successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Delete content",
            description = "Deletes a content by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Content deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Content not found"
                    )
            }
    )
    @DeleteMapping("/delete/{id}")
    public FlowXResponse<Void> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return FlowXResponse.<Void>builder()
                .message("Content deleted successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get content by ID",
            description = "Retrieves a content by its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Content retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Content not found"
                    )
            }
    )
    @GetMapping("/get/{id}")
    public FlowXResponse<ContentResponse> getContentById(@PathVariable Long id) {
        return FlowXResponse.<ContentResponse>builder()
                .data(contentService.getContentById(id))
                .message("Content retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get all contents",
            description = "Retrieves a list of all contents in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of contents retrieved successfully"
                    )
            }
    )
    @GetMapping("/get-all")
    public FlowXResponse<List<ContentResponse>> getAllContents() {
        return FlowXResponse.<List<ContentResponse>>builder()
                .data(contentService.getAllContents())
                .message("List of contents retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get contents by target",
            description = "Retrieves a list of contents associated with a specific target type and ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of contents retrieved successfully"
                    )
            }
    )
    @GetMapping("/target/{contentTargetType}/{targetId}")
    public FlowXResponse<List<ContentResponse>> getContentsByTarget(
            @PathVariable ContentTargetType contentTargetType,
            @PathVariable Long targetId) {
        return FlowXResponse.<List<ContentResponse>>builder()
                .data(contentService.getContentsByTargetTypeAndId(contentTargetType, targetId))
                .message("List of target contents retrieved successfully")
                .code(200)
                .build();
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get contents by user",
            description = "Retrieves a list of contents created by a specific user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of contents retrieved successfully"
                    )
            }
    )
    public FlowXResponse<List<ContentResponse>> getContentsByUser(@PathVariable Long userId) {
        return FlowXResponse.<List<ContentResponse>>builder()
                .data(contentService.getContentsByUser(userId))
                .message("List of contents by user retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Get contents by parent",
            description = "Retrieves a list of contents that are replies to a specific parent content.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of contents retrieved successfully"
                    )
            }
    )
    @GetMapping("/parent/{parentId}")
    public FlowXResponse<List<ContentResponse>> getContentsByParent(@PathVariable Long parentId) {
        return FlowXResponse.<List<ContentResponse>>builder()
                .data(contentService.getContentsByParent(parentId))
                .message("List of reply contents retrieved successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Update hasFile flag for content",
            description = "Updates the hasFile flag based on actual files associated with the content. This should be called after uploading or deleting files.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the content")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "HasFile flag updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Content not found"
                    )
            }
    )
    @PutMapping("/{id}/sync-files")
    public FlowXResponse<Void> syncContentFiles(@PathVariable Long id) {
        contentService.updateHasFileFlag(id);
        return FlowXResponse.<Void>builder()
                .message("Content files synchronized successfully")
                .code(200)
                .build();
    }

    @Operation(
            summary = "Share content",
            description = "Shares a content with specified users or groups.",
            parameters = {
                    @Parameter(name = "contentId", description = "ID of the content to share")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Content shared successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Content not found"
                    )
            }
    )
    @PostMapping("/{contentId}/share")
    public FlowXResponse<Void> shareContent(
            @PathVariable Long contentId, 
            @RequestParam String sharedWith) {
        contentService.shareContent(contentId, sharedWith);
        return FlowXResponse.<Void>builder()
                .message("Content shared successfully")
                .code(200)
                .build();
    }
}