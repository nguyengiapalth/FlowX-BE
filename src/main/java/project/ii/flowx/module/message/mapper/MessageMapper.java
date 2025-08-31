package project.ii.flowx.module.message.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import project.ii.flowx.module.message.dto.message.MessageCreateRequest;
import project.ii.flowx.module.message.dto.message.MessageUpdateRequest;
import project.ii.flowx.module.message.dto.message.MessageResponse;
import project.ii.flowx.module.message.entity.Message;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    MessageResponse toMessageResponse(Message message);
    List<MessageResponse> toMessageResponseList(List<Message> messages);
    Message toMessage(MessageCreateRequest request);
    void updateMessageFromRequest(@MappingTarget Message message, MessageUpdateRequest request);
} 