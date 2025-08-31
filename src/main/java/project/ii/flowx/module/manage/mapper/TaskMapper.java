package project.ii.flowx.module.manage.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import project.ii.flowx.module.manage.entity.Task;
import project.ii.flowx.module.manage.dto.task.TaskCreateRequest;
import project.ii.flowx.module.manage.dto.task.TaskResponse;
import project.ii.flowx.module.manage.dto.task.TaskUpdateRequest;

import java.util.List;

/**
 * Mapper interface for converting between Task entity and Task DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

    /**
     * Convert Task entity to TaskResponse DTO
     * Files will be populated separately in service for better performance
     */
    @Mapping(target = "files", ignore = true) // Will be populated separately in service
    TaskResponse toTaskResponse(Task task);

    /**
     * Convert TaskCreateRequest to Task entity
     * Ignores all system-managed fields and relationships
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignerId", ignore = true) // Will be set by service
    @Mapping(target = "assigneeId", ignore = true) // Will be set by service  
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hasFiles", ignore = true) // Will be updated when files are attached
    @Mapping(target = "completedDate", ignore = true) // Will be set when task is completed
    Task toTask(TaskCreateRequest taskCreateRequest);

    /**
     * Update Task entity from TaskUpdateRequest
     * Only updates modifiable fields, ignores system-managed fields
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignerId", ignore = true)
    @Mapping(target = "assigneeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hasFiles", ignore = true)
    void updateTaskFromRequest(@MappingTarget Task task, TaskUpdateRequest taskUpdateRequest);

    /**
     * Convert list of Task entities to list of TaskResponse DTOs
     */
    List<TaskResponse> toTaskResponseList(List<Task> tasks);
}