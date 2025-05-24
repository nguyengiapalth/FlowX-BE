package project.ii.flowx.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import project.ii.flowx.model.entity.Task;
import project.ii.flowx.model.dto.task.TaskCreateRequest;
import project.ii.flowx.model.dto.task.TaskResponse;
import project.ii.flowx.model.dto.task.TaskUpdateRequest;

import java.util.List;

/**
 * Mapper interface for converting between Task entity and Task DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskResponse toTaskResponse(Task task);

    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "department.id", source = "departmentId")
    @Mapping(target = "assigner.id", source = "assignerId")
    @Mapping(target = "assignee.id", source = "assigneeId")
    Task toTask(TaskCreateRequest taskCreateRequest);

    @Mapping(target = "project.id", source = "projectId")
    @Mapping(target = "department.id", source = "departmentId")
    @Mapping(target = "assignee.id", source = "assigneeId")
    void updateTaskFromRequest(@MappingTarget Task task, TaskUpdateRequest taskUpdateRequest);

    List<TaskResponse> toTaskResponseList(List<Task> tasks);
}